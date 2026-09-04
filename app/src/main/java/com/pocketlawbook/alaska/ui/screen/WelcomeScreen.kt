package com.pocketlawbook.alaska.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.R
import com.pocketlawbook.alaska.ui.component.SectionLabel

/**
 * The main welcome screen and the app's start destination.
 *
 * It leads with the free thing that helps immediately — describing what happened.
 * No subscription/case-law/AI-chat teaser here: those aren't shipped (see
 * CLAUDE.md's "Unshipped scaffolding"), so this screen doesn't offer a path to them.
 */
@Composable
fun WelcomeScreen(
    onAnalyze: () -> Unit,
    onBrowseAlaska: () -> Unit,
    onBrowseFederal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Know your rights, with sources you can check.",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Describe something that happened and see which Alaska or federal rights it " +
                "touches, along with steps you can take. No account needed.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(onClick = onAnalyze, modifier = Modifier.fillMaxWidth()) {
            Text("Describe what happened")
        }

        Spacer(Modifier.height(8.dp))
        SectionLabel("Browse the law")

        OutlinedButton(onClick = onBrowseAlaska, modifier = Modifier.fillMaxWidth()) {
            Text("Alaska law")
        }
        OutlinedButton(onClick = onBrowseFederal, modifier = Modifier.fillMaxWidth()) {
            Text("Federal law")
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.disclaimer_full),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
