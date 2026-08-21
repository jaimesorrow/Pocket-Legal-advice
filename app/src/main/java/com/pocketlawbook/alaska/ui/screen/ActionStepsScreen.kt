package com.pocketlawbook.alaska.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity
import com.pocketlawbook.alaska.ui.component.JurisdictionChip
import com.pocketlawbook.alaska.ui.component.SectionLabel

/**
 * The verified steps for one violation key.
 *
 * Every string here is rendered exactly as stored — no trimming, no reformatting,
 * no interpolation. The test suite asserts this byte-for-byte, and it matters
 * beyond the test: rewording vetted legal content silently un-vets it.
 */
@Composable
fun ActionStepsScreen(
    entity: ActionStepEntity?,
    modifier: Modifier = Modifier
) {
    if (entity == null) {
        Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = "That entry isn't in the verified library.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        JurisdictionChip(entity.jurisdiction)

        SectionLabel("Source")
        Text(
            text = entity.description,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(6.dp))
        SectionLabel("What you can do")

        entity.actionSteps.forEachIndexed { index, step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(14.dp))
                    Text(text = step, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = "These are general steps, not advice about your case. A licensed Alaska " +
                "attorney can tell you what applies to your situation.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
