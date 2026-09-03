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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.data.account.LockReason
import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.ui.component.JurisdictionChip
import com.pocketlawbook.alaska.ui.component.LockedFeatureNotice
import com.pocketlawbook.alaska.ui.component.SectionLabel

/**
 * Statute browsing for one jurisdiction. Free tier — no account required.
 *
 * It lists what is actually in the verified store rather than a hardcoded menu,
 * so the screen never promises content the app cannot show.
 */
@Composable
fun LawBrowseScreen(
    jurisdiction: Jurisdiction,
    onOpenSteps: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val entries = VerifiedContentSeed.entries.values
        .filter { it.jurisdiction == jurisdiction }
        .sortedBy { it.description }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (jurisdiction == Jurisdiction.ALASKA) "Alaska law" else "Federal law",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        if (entries.isEmpty()) {
            Text(
                text = "Nothing in the verified library for this jurisdiction yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Column
        }

        SectionLabel("${entries.size} entries")

        entries.forEach { entity ->
            Card(
                onClick = { onOpenSteps(entity.violationKey) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JurisdictionChip(entity.jurisdiction)
                    Text(text = entity.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

/**
 * Case law, locked behind the subscription.
 *
 * There is no corpus behind this yet, so the screen shows the lock rather than an
 * empty list — and would still show the lock if the corpus existed. Gating lives
 * here in the UI only for now; see AccountRepository for why that is not enough
 * for a real release.
 */
@Composable
fun CaseLawScreen(
    jurisdiction: Jurisdiction,
    unlocked: Boolean,
    lockReason: LockReason?,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title = if (jurisdiction == Jurisdiction.ALASKA) "Alaska case law" else "Federal case law"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        if (!unlocked) {
            LockedFeatureNotice(
                featureLabel = title,
                reason = lockReason ?: LockReason.NEEDS_ACCOUNT,
                onPrimaryAction = onUnlock
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Unlocked, but not yet populated",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your subscription is active. The $title corpus has not been " +
                            "loaded into this build yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// AiChatScreen has moved to ChatScreen.kt now that it has a real implementation
// (data.chat / data.repository.ChatRepository) instead of being a stub behind
// this file's subscription lock. See CLAUDE.md for why it's currently ungated.
