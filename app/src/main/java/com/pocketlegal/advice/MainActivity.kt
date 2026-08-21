package com.pocketlegal.advice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pocketlegal.advice.data.local.database.LegalAdviceDatabase
import com.pocketlegal.advice.data.remote.api.RetrofitClient
import com.pocketlegal.advice.data.repository.LegalAnalysisRepository
import com.pocketlegal.advice.ui.model.LegalAnalysisUiState
import com.pocketlegal.advice.viewmodel.LegalAnalysisViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: LegalAnalysisViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = LegalAdviceDatabase.getDatabase(this@MainActivity)
                val repository = LegalAnalysisRepository(
                    RetrofitClient.apiService,
                    database.actionStepDao()
                )
                @Suppress("UNCHECKED_CAST")
                return LegalAnalysisViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketLegalAdviceApp(viewModel)
        }
    }
}

@Composable
private fun PocketLegalAdviceApp(viewModel: LegalAnalysisViewModel) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            AnalysisScreen(viewModel)
        }
    }
}

@Composable
private fun AnalysisScreen(viewModel: LegalAnalysisViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Legal Situation Analyzer",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Describe your legal situation") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            minLines = 4,
            maxLines = 6,
            enabled = uiState !is LegalAnalysisUiState.Loading
        )

        Button(
            onClick = { viewModel.analyzeSituation(query) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(vertical = 8.dp),
            enabled = query.isNotBlank() && uiState !is LegalAnalysisUiState.Loading
        ) {
            Text("Analyze")
        }

        when (uiState) {
            is LegalAnalysisUiState.Idle -> {
                Text(
                    text = "Enter your legal situation above and click Analyze to get started.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            is LegalAnalysisUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is LegalAnalysisUiState.Success -> {
                val success = uiState as LegalAnalysisUiState.Success
                if (success.verifiedActionSteps.isEmpty()) {
                    Text(
                        text = "No legal violations found in your situation.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    success.verifiedActionSteps.forEach { step ->
                        ResultCard(step)
                    }
                }
            }

            is LegalAnalysisUiState.Error -> {
                val error = uiState as LegalAnalysisUiState.Error
                Text(
                    text = "Error: ${error.message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            is LegalAnalysisUiState.NoVerifiedData -> {
                Text(
                    text = "No verified data available for the violations found.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResultCard(step: com.pocketlegal.advice.ui.model.VerifiedActionStep) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = step.violationKey,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Recommended Actions:",
                style = MaterialTheme.typography.labelMedium
            )
            step.steps.forEachIndexed { index, action ->
                Text(
                    text = "${index + 1}. $action",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
