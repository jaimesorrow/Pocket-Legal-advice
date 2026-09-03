package com.pocketlawbook.alaska.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.ui.component.SectionLabel
import com.pocketlawbook.alaska.ui.component.VerifiedMatchCard
import com.pocketlawbook.alaska.ui.model.ChatTurn

/**
 * A chat over the same verified library the analysis screen uses, per the
 * hybrid design in docs/screen-map.html: a language model, when configured,
 * may narrow the retrieved candidates and write one framing sentence, but
 * every citation and every word of legal text on screen comes from the local
 * store. Ungated for now - see CLAUDE.md on why accounts/billing weren't
 * re-wired alongside this.
 */
@Composable
fun AiChatScreen(
    turns: List<ChatTurn>,
    isAsking: Boolean,
    onAsk: (String) -> Unit,
    onOpenSteps: (String) -> Unit,
    jurisdictionFor: (String) -> Jurisdiction,
    modifier: Modifier = Modifier
) {
    var query by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
        Text(
            text = "AI chat",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ask a question about your situation. A model may help point to the right " +
                "answer, but every citation and every word of legal text you see comes from the " +
                "vetted library, never from the model.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            turns.forEach { turn -> ChatTurnCard(turn, jurisdictionFor, onOpenSteps) }
            if (isAsking) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask a question") }
            )
            Button(
                onClick = {
                    onAsk(query)
                    query = ""
                },
                enabled = query.isNotBlank() && !isAsking
            ) {
                Text("Ask")
            }
        }
    }
}

@Composable
private fun ChatTurnCard(
    turn: ChatTurn,
    jurisdictionFor: (String) -> Jurisdiction,
    onOpenSteps: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = turn.query,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        when {
            turn.error != null -> Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Text(
                    text = turn.error,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            turn.answer != null -> {
                turn.answer.framingSentence?.let { sentence ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        SectionLabel(text = "AI framing, not a legal claim")
                        Text(text = sentence, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                if (turn.answer.matches.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Text(
                            text = "Nothing verified matches that yet. Try describing it " +
                                "differently, or talk to Alaska Legal Services Corporation.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    turn.answer.matches.forEach { step ->
                        VerifiedMatchCard(
                            step = step,
                            jurisdiction = jurisdictionFor(step.violationKey),
                            onClick = { onOpenSteps(step.violationKey) }
                        )
                    }
                }
            }
        }
    }
}
