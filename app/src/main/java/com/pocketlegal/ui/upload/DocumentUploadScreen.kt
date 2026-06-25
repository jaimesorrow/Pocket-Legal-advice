package com.pocketlegal.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// State
// ---------------------------------------------------------------------------

sealed interface UploadState {
    data object Idle : UploadState
    data class Processing(val step: ProcessingStep) : UploadState
    data class Success(val summary: String) : UploadState
    data class Error(val kind: ErrorKind) : UploadState
}

enum class ProcessingStep(val label: String, val progress: Float) {
    SCANNING("Scanning document structure...", 0.15f),
    EXTRACTING_TEXT("Extracting document text...", 0.30f),
    IDENTIFYING_CITATIONS("Extracting statutory citations...", 0.50f),
    VERIFYING_CODES("Verifying procedural codes...", 0.65f),
    CROSS_REFERENCING("Cross-referencing case law...", 0.78f),
    GENERATING_SUMMARY("Generating legal summary...", 0.90f),
    FINALIZING("Finalizing analysis...", 0.97f),
}

sealed interface ErrorKind {
    data object ParseFailure : ErrorKind
    data object NetworkTimeout : ErrorKind
    data object UnsupportedFormat : ErrorKind
    data class Unknown(val message: String) : ErrorKind
}

data class SelectedDocument(
    val uri: Uri,
    val name: String,
    val type: DocumentType,
)

enum class DocumentType { PDF, IMAGE }

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentUploadScreen(
    onAnalysisComplete: (summary: String) -> Unit = {},
    onNavigateUp: () -> Unit = {},
) {
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }
    var selectedDocument by remember { mutableStateOf<SelectedDocument?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // PDF picker
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedDocument = SelectedDocument(
                uri = it,
                name = it.lastPathSegment?.substringAfterLast("/") ?: "document.pdf",
                type = DocumentType.PDF,
            )
            uploadState = UploadState.Idle
        }
    }

    // Image / gallery picker
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedDocument = SelectedDocument(
                uri = it,
                name = it.lastPathSegment?.substringAfterLast("/") ?: "image",
                type = DocumentType.IMAGE,
            )
            uploadState = UploadState.Idle
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Upload Document",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Header card
                HeaderCard()

                // Upload card
                UploadCard(
                    selectedDocument = selectedDocument,
                    onPickPdf = { pdfLauncher.launch("application/pdf") },
                    onPickImage = {
                        imageLauncher.launch(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    },
                    onClearSelection = {
                        selectedDocument = null
                        uploadState = UploadState.Idle
                    },
                )

                // Analyse button
                AnimatedVisibility(
                    visible = selectedDocument != null && uploadState is UploadState.Idle,
                    enter = fadeIn() + slideInVertically { it / 2 },
                    exit = fadeOut() + slideOutVertically { it / 2 },
                ) {
                    AnalyseButton(
                        onClick = {
                            scope.launch {
                                runAnalysis(
                                    onStateChange = { uploadState = it },
                                    onComplete = { summary ->
                                        uploadState = UploadState.Success(summary)
                                        onAnalysisComplete(summary)
                                    },
                                    onError = { kind -> uploadState = UploadState.Error(kind) },
                                )
                            }
                        }
                    )
                }

                // Success card
                AnimatedVisibility(
                    visible = uploadState is UploadState.Success,
                    enter = fadeIn() + slideInVertically { it / 2 },
                ) {
                    val successState = uploadState as? UploadState.Success
                    if (successState != null) {
                        SuccessCard(summary = successState.summary)
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            // Full-screen processing overlay
            AnimatedVisibility(
                visible = uploadState is UploadState.Processing,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize(),
            ) {
                val processingState = uploadState as? UploadState.Processing
                if (processingState != null) {
                    ProcessingOverlay(step = processingState.step)
                }
            }
        }
    }

    // Error dialog
    if (uploadState is UploadState.Error) {
        val errorState = uploadState as UploadState.Error
        ErrorDialog(
            kind = errorState.kind,
            onDismiss = { uploadState = UploadState.Idle },
            onRetry = {
                uploadState = UploadState.Idle
                scope.launch {
                    runAnalysis(
                        onStateChange = { uploadState = it },
                        onComplete = { summary ->
                            uploadState = UploadState.Success(summary)
                            onAnalysisComplete(summary)
                        },
                        onError = { kind -> uploadState = UploadState.Error(kind) },
                    )
                }
            },
        )
    }
}

// ---------------------------------------------------------------------------
// Header card
// ---------------------------------------------------------------------------

@Composable
private fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(40.dp),
            )
            Column {
                Text(
                    text = "Legal Document Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Upload a PDF or photo of your document. Our AI will extract key legal clauses, statutory references, and procedural codes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Upload card
// ---------------------------------------------------------------------------

@Composable
private fun UploadCard(
    selectedDocument: SelectedDocument?,
    onPickPdf: () -> Unit,
    onPickImage: () -> Unit,
    onClearSelection: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Select Document",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (selectedDocument == null) {
                // Drop zone
                UploadDropZone(onPickPdf = onPickPdf, onPickImage = onPickImage)
            } else {
                // Selected file preview
                SelectedFileRow(document = selectedDocument, onClear = onClearSelection)
            }

            // Pickers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PickerButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Description,
                    label = "Choose PDF",
                    onClick = onPickPdf,
                )
                PickerButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Image,
                    label = "Photo / Gallery",
                    onClick = onPickImage,
                )
            }

            Text(
                text = "Supported formats: PDF, JPG, PNG, HEIC",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UploadDropZone(
    onPickPdf: () -> Unit,
    onPickImage: () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "borderAlpha",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = alpha),
                    )
                ),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable { onPickPdf() },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.UploadFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = "Tap to select a file",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "or use the buttons below",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SelectedFileRow(
    document: SelectedDocument,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (document.type == DocumentType.PDF) Icons.Outlined.Description else Icons.Outlined.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = document.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
            )
            Text(
                text = if (document.type == DocumentType.PDF) "PDF Document" else "Image",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
            )
        }
        IconButton(onClick = onClear) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun PickerButton(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

// ---------------------------------------------------------------------------
// Analyse button
// ---------------------------------------------------------------------------

@Composable
private fun AnalyseButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Text(
            text = "Analyse Document",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

// ---------------------------------------------------------------------------
// Processing overlay
// ---------------------------------------------------------------------------

@Composable
private fun ProcessingOverlay(step: ProcessingStep) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
    ) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    // Spinner
                    CircularProgressIndicator(
                        modifier = Modifier.size(52.dp),
                        strokeWidth = 4.dp,
                        strokeCap = StrokeCap.Round,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    // Step label with animated transitions
                    AnimatedContent(
                        targetState = step.label,
                        transitionSpec = {
                            (fadeIn(tween(300)) + slideInVertically { it / 4 })
                                .togetherWith(fadeOut(tween(200)))
                        },
                        label = "stepLabel",
                    ) { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }

                    // Step list
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ProcessingStep.entries.forEach { s ->
                            StepRow(
                                label = s.label,
                                isDone = s.ordinal < step.ordinal,
                                isActive = s == step,
                            )
                        }
                    }

                    // Progress bar
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        LinearProgressIndicator(
                            progress = { step.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            strokeCap = StrokeCap.Round,
                        )
                        Text(
                            text = "${(step.progress * 100).toInt()}% complete",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepRow(label: String, isDone: Boolean, isActive: Boolean) {
    val textColor = when {
        isActive -> MaterialTheme.colorScheme.primary
        isDone -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }
    val prefix = when {
        isDone -> "✓ "
        isActive -> "▶ "
        else -> "○ "
    }
    Text(
        text = prefix + label,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
        color = textColor,
    )
}

// ---------------------------------------------------------------------------
// Success card
// ---------------------------------------------------------------------------

@Composable
private fun SuccessCard(summary: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Analysis Complete",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Error dialog
// ---------------------------------------------------------------------------

@Composable
private fun ErrorDialog(
    kind: ErrorKind,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    val (title, body) = when (kind) {
        is ErrorKind.ParseFailure -> Pair(
            "Unable to Parse Document",
            "The document could not be read. It may be encrypted, corrupted, or contain only scanned images without readable text. Please try a different file or a clearer photo.",
        )
        is ErrorKind.NetworkTimeout -> Pair(
            "Connection Timed Out",
            "The server did not respond within the allowed time. This may be due to a slow network or high server load. Please check your connection and try again.",
        )
        is ErrorKind.UnsupportedFormat -> Pair(
            "Unsupported File Format",
            "Only PDF documents, JPEG, PNG, and HEIC images are supported. Please convert your file to one of these formats before uploading.",
        )
        is ErrorKind.Unknown -> Pair(
            "Unexpected Error",
            "An unexpected error occurred: ${kind.message}\n\nIf this problem persists, please contact support.",
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp),
    )
}

// ---------------------------------------------------------------------------
// Simulated analysis pipeline (replace with real ViewModel/UseCase calls)
// ---------------------------------------------------------------------------

private suspend fun runAnalysis(
    onStateChange: (UploadState) -> Unit,
    onComplete: (String) -> Unit,
    onError: (ErrorKind) -> Unit,
) {
    val steps = ProcessingStep.entries
    for (step in steps) {
        onStateChange(UploadState.Processing(step))
        // Simulate variable-duration work per step
        delay(
            when (step) {
                ProcessingStep.SCANNING -> 900L
                ProcessingStep.EXTRACTING_TEXT -> 1_100L
                ProcessingStep.IDENTIFYING_CITATIONS -> 1_400L
                ProcessingStep.VERIFYING_CODES -> 1_200L
                ProcessingStep.CROSS_REFERENCING -> 1_500L
                ProcessingStep.GENERATING_SUMMARY -> 1_300L
                ProcessingStep.FINALIZING -> 600L
            }
        )
    }
    onComplete(
        "Document analysed successfully. Key findings: 3 statutory citations identified (§ 12-301, § 14-108, § 22-2001), 2 procedural code references verified, and 1 contractual clause flagged for legal review."
    )
}
