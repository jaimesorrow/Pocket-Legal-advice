package com.pocketlawbook.alaska.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.R
import com.pocketlawbook.alaska.data.account.AccountState
import com.pocketlawbook.alaska.data.account.SubscriptionPlan
import com.pocketlawbook.alaska.data.account.hasPremiumAccess
import com.pocketlawbook.alaska.ui.component.SectionLabel

/**
 * The left navigation drawer.
 *
 * Entries appear in a fixed order: the three account controls, then Alaska law,
 * federal law, Alaska case law, federal case law, and AI chat. The last three
 * carry a lock until the user is both signed in and subscribed. They stay visible
 * and tappable while locked on purpose — hiding them would leave no way to
 * discover what the subscription buys.
 */
@Composable
fun AppDrawer(
    accountState: AccountState,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onLockedClick: (DrawerEntry) -> Unit
) {
    val unlocked = accountState.hasPremiumAccess

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(start = 28.dp, top = 28.dp, end = 20.dp, bottom = 18.dp)) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.app_tagline),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            SectionLabel(
                text = "Account",
                modifier = Modifier.padding(start = 28.dp, top = 18.dp, bottom = 6.dp)
            )
            DrawerSections.account.forEach { entry ->
                val label = when {
                    entry.route == Routes.SIGN_IN && accountState is AccountState.SignedIn -> "Sign out"
                    else -> entry.label
                }
                DrawerRow(
                    label = label,
                    selected = currentRoute == entry.route,
                    locked = false,
                    onClick = { onNavigate(entry.route) }
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()

            SectionLabel(
                text = "Law",
                modifier = Modifier.padding(start = 28.dp, top = 18.dp, bottom = 6.dp)
            )
            DrawerSections.library.forEach { entry ->
                val locked = entry.requires != null && !unlocked
                DrawerRow(
                    label = entry.label,
                    selected = currentRoute == entry.route,
                    locked = locked,
                    onClick = {
                        if (locked) onLockedClick(entry) else onNavigate(entry.route)
                    }
                )
            }

            Spacer(Modifier.height(10.dp))
            HorizontalDivider()

            SectionLabel(
                text = "About",
                modifier = Modifier.padding(start = 28.dp, top = 18.dp, bottom = 6.dp)
            )
            DrawerSections.about.forEach { entry ->
                DrawerRow(
                    label = entry.label,
                    selected = currentRoute == entry.route,
                    locked = false,
                    onClick = { onNavigate(entry.route) }
                )
            }

            if (!unlocked) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider()
                Column(modifier = Modifier.padding(start = 28.dp, top = 16.dp, end = 24.dp)) {
                    SectionLabel(text = "Subscription")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${SubscriptionPlan.FULL_DISPLAY} unlocks Alaska and federal case " +
                            "law and the AI chat.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerRow(
    label: String,
    selected: Boolean,
    locked: Boolean,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    color = if (locked) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface
                )
                if (locked) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Requires a subscription",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
    )
}
