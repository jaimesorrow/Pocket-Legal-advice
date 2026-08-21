package com.pocketlawbook.alaska.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.pocketlawbook.alaska.data.legal.LegalDocuments
import com.pocketlawbook.alaska.ui.component.SectionLabel

/**
 * The legal hub: every document in one place, reachable from the drawer and from
 * Settings. Google Play requires the privacy policy to be reachable in-app, and
 * subscription terms have to be readable before purchase.
 */
@Composable
fun LegalIndexScreen(
    onOpenDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Legal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "The terms you agreed to, what this app does with your information, and " +
                "where its content comes from.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LegalDocuments.all.forEach { document ->
            Card(
                onClick = { onOpenDocument(document.id) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = document.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = "Version ${LegalDocuments.VERSION} · ${LegalDocuments.EFFECTIVE_DATE}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** One document, rendered in full. */
@Composable
fun LegalDocumentScreen(
    documentId: String,
    modifier: Modifier = Modifier
) {
    val document = LegalDocuments.byId(documentId)

    if (document == null) {
        Column(modifier = modifier.fillMaxSize().padding(20.dp)) {
            Text("That document isn't available.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = document.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = document.summary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

        document.sections.forEach { section ->
            SectionLabel(section.heading)
            Spacer(Modifier.height(2.dp))
            section.body.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Spacer(Modifier.height(6.dp))
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
        Text(
            text = "Version ${LegalDocuments.VERSION} · ${LegalDocuments.EFFECTIVE_DATE} · " +
                "Questions: ${LegalDocuments.CONTACT_EMAIL}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * First-run acceptance gate. Shown before anything else, and again whenever
 * [LegalDocuments.VERSION] is bumped.
 *
 * It requires an explicit checkbox rather than treating continued use as consent,
 * and it summarises the disclaimer on the screen rather than only linking to it —
 * "they had to scroll past a link" is a weak record of agreement.
 */
@Composable
fun LegalConsentScreen(
    onAccept: () -> Unit,
    onOpenDocument: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var checked by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Before you start",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "This app gives legal information, not legal advice.",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "It covers Alaska state law and federal law only. Using it does not " +
                        "create an attorney-client relationship, and nothing you type here is " +
                        "protected by attorney-client privilege.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "The law changes. Content may be out of date or may not apply to your " +
                        "situation. For advice about your case, talk to a licensed Alaska attorney.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "If you are in danger, call 911.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        SectionLabel("Read in full")
        LegalDocuments.all.forEach { document ->
            Text(
                text = document.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDocument(document.id) }
                    .padding(vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(checked = checked, onCheckedChange = { checked = it })
            Spacer(Modifier.width(6.dp))
            Text(
                text = "I understand this is legal information, not legal advice, and I agree " +
                    "to the terms of service and privacy policy.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Button(
            onClick = onAccept,
            enabled = checked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }

        Text(
            text = "No account is needed to use the free tier.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
