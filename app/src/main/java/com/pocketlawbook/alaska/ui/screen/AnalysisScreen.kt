package com.pocketlawbook.alaska.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.ui.component.JurisdictionChip
import com.pocketlawbook.alaska.ui.component.SectionLabel
import com.pocketlawbook.alaska.ui.model.LegalAnalysisUiState
import com.pocketlawbook.alaska.ui.model.VerifiedActionStep

/**
 * Describe a situation, get back the rights it touches.
 *
 * This is the screen the existing pipeline was built for: it renders whatever
 * [LegalAnalysisUiState] the view model emits and nothing else. Every string on
 * screen came out of the verified store.
 */
@Composable
fun AnalysisScreen(
    uiState: LegalAnalysisUiState,
    onAnalyze: (String) -> Unit,
    onOpenSteps: (String) -> Unit,
    jurisdictionFor: (String) -> Jurisdiction,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "What happened?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Describe it in your own words. Nothing you type leaves your phone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().height(140.dp),
            placeholder = { Text("For example: I was arrested and held overnight, and nobody read me my rights.") }
        )

        Text(
            text = "This matches your description against a short list of common situations. It is " +
                "not exhaustive: a match below is real, but no match, or a partial one, does not " +
                "mean nothing was wrong. When in doubt, talk to a lawyer.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = { onAnalyze(query) },
            enabled = query.isNotBlank() && uiState !is LegalAnalysisUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Find my rights")
        }

        Spacer(Modifier.height(4.dp))

        when (uiState) {
            is LegalAnalysisUiState.Idle -> Unit

            is LegalAnalysisUiState.Loading -> Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
            }

            is LegalAnalysisUiState.Success -> {
                if (uiState.verifiedActionSteps.isEmpty()) {
                    EmptyResult()
                } else {
                    SectionLabel("${uiState.verifiedActionSteps.size} matched")
                    uiState.verifiedActionSteps.forEach { step ->
                        ResultCard(
                            step = step,
                            jurisdiction = jurisdictionFor(step.violationKey),
                            onClick = { onOpenSteps(step.violationKey) }
                        )
                    }
                }
            }

            is LegalAnalysisUiState.NoVerifiedData -> EmptyResult()

            is LegalAnalysisUiState.Error -> Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = uiState.message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ResultCard(
    step: VerifiedActionStep,
    jurisdiction: Jurisdiction,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JurisdictionChip(jurisdiction)
            // Verified description, straight from the store, unmodified.
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${step.steps.size} steps you can take",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyResult() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Nothing verified matches that yet",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rather than guess, this app says nothing. Try describing what happened in " +
                    "different words, or talk to Alaska Legal Services Corporation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
