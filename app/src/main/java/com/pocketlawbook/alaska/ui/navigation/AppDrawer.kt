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
import com.pocketlawbook.alaska.data.account.hasPremiumAccess
import com.pocketlawbook.alaska.ui.component.SectionLabel

/**
 * The left navigation drawer.
 *
 * Currently just Alaska law, federal law, and Legal & privacy — the whole free
 * tier, and the whole app. Accounts, subscriptions, case law, and AI chat are
 * not shipped; DrawerSections.library entries can carry a [PremiumFeature] and
 * this composable still honors [DrawerEntry.requires] via [onLockedClick] so
 * those can come back once there's a real backend, but nothing sets that field
 * today. See CLAUDE.md.
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
