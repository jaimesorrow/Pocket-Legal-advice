package com.pocketlawbook.alaska.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketlawbook.alaska.R
import com.pocketlawbook.alaska.data.account.LockReason
import com.pocketlawbook.alaska.data.account.SubscriptionPlan
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.ui.model.VerifiedActionStep
import com.pocketlawbook.alaska.ui.theme.jurisdictionColor

/**
 * Tells the reader which body of law an answer rests on. Shown wherever verified
 * content appears — an Alaska answer and a federal answer must never be
 * indistinguishable.
 */
@Composable
fun JurisdictionChip(jurisdiction: Jurisdiction, modifier: Modifier = Modifier) {
    val isFederal = jurisdiction == Jurisdiction.FEDERAL
    val color = jurisdictionColor(isFederal)
    Text(
        text = stringResource(
            if (isFederal) R.string.jurisdiction_federal else R.string.jurisdiction_alaska
        ).uppercase(),
        color = color,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.9.sp,
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(3.dp))
            .background(color.copy(alpha = 0.08f), RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

/** The standing legal-information-not-legal-advice line. */
@Composable
fun DisclaimerBar(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.disclaimer_short),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

/**
 * Shown in place of a premium feature's content.
 *
 * It states which of the two conditions is missing rather than a generic "locked",
 * because the next action differs: a signed-out user needs an account first, and a
 * signed-in user only needs to subscribe.
 */
@Composable
fun LockedFeatureNotice(
    featureLabel: String,
    reason: LockReason,
    onPrimaryAction: () -> Unit,
    modifier: Modifier = Modifier,
    onSecondaryAction: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "$featureLabel is part of the subscription",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (reason) {
                    LockReason.NEEDS_ACCOUNT ->
                        "Create an account and subscribe for ${SubscriptionPlan.FULL_DISPLAY} to " +
                            "read $featureLabel."
                    LockReason.NEEDS_SUBSCRIPTION ->
                        "You're signed in. Subscribe for ${SubscriptionPlan.FULL_DISPLAY} to " +
                            "read $featureLabel."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Alaska statutes, federal statutes, the situation analyzer, and AI chat " +
                    "stay free and need no account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onPrimaryAction) {
                    Text(
                        when (reason) {
                            LockReason.NEEDS_ACCOUNT -> "Create an account"
                            LockReason.NEEDS_SUBSCRIPTION -> "Subscribe"
                        }
                    )
                }
            }
            if (onSecondaryAction != null && reason == LockReason.NEEDS_ACCOUNT) {
                Text(
                    text = "Already have an account? Sign in",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Renders one verified match: jurisdiction, description, and step count, straight
 * from the local store. Shared by the analysis screen and the chat screen so a
 * match looks the same everywhere it's shown, regardless of which of the two
 * paths (keyword match or model-selected candidate) surfaced it.
 */
@Composable
fun VerifiedMatchCard(
    step: VerifiedActionStep,
    jurisdiction: Jurisdiction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
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

/** A small all-caps label used to head a group. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color? = null) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = color ?: MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}
