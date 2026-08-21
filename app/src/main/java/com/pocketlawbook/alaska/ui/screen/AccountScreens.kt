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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pocketlawbook.alaska.data.account.AccountState
import com.pocketlawbook.alaska.data.account.PremiumFeature
import com.pocketlawbook.alaska.data.account.SubscriptionPlan
import com.pocketlawbook.alaska.data.account.SubscriptionStatus
import com.pocketlawbook.alaska.ui.component.SectionLabel

/** Sign in or sign up. One form, two labels — the fields are identical. */
@Composable
fun CredentialsScreen(
    isSignUp: Boolean,
    errorMessage: String?,
    onSubmit: (String, String) -> Unit,
    onSwitchMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isSignUp) "Create an account" else "Sign in",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "An account is only needed for case law and the AI chat. Alaska statutes, " +
                "federal statutes, and the situation analyzer stay free without one.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            supportingText = if (isSignUp) {
                { Text("At least 8 characters") }
            } else null,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Button(
            onClick = { onSubmit(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isSignUp) "Create account" else "Sign in")
        }

        TextButton(onClick = onSwitchMode, modifier = Modifier.fillMaxWidth()) {
            Text(
                if (isSignUp) "Already have an account? Sign in"
                else "No account yet? Create one"
            )
        }
    }
}

/** Signed-in account overview, with subscription state and controls. */
@Composable
fun AccountScreen(
    state: AccountState,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
    onSignOut: () -> Unit,
    onSubscribe: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Your account",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        when (state) {
            is AccountState.SignedOut -> {
                Text(
                    text = "You're not signed in. You don't need to be for Alaska statutes, " +
                        "federal statutes, or the situation analyzer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onSignUp, modifier = Modifier.fillMaxWidth()) {
                    Text("Create an account")
                }
                OutlinedButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign in")
                }
            }

            is AccountState.SignedIn -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SectionLabel("Signed in as")
                        Text(text = state.email, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(Modifier.height(4.dp))
                SubscriptionCard(
                    status = state.subscription,
                    onSubscribe = onSubscribe,
                    onCancel = onCancel
                )

                OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign out")
                }
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    status: SubscriptionStatus,
    onSubscribe: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SectionLabel("Subscription")
            when (status) {
                is SubscriptionStatus.Active -> {
                    Text(
                        text = "Active — ${SubscriptionPlan.FULL_DISPLAY}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Renews ${status.renewsOn}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                        Text("Cancel subscription")
                    }
                }
                is SubscriptionStatus.None -> {
                    Text(
                        text = "Not subscribed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Case law and the AI chat stay locked until you subscribe.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onSubscribe, modifier = Modifier.fillMaxWidth()) {
                        Text("Subscribe — ${SubscriptionPlan.FULL_DISPLAY}")
                    }
                }
            }
        }
    }
}

/** What the subscription includes, and what stays free. */
@Composable
fun PaywallScreen(
    state: AccountState,
    onSubscribe: () -> Unit,
    onSignUp: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionLabel("Case law extension")
        Text(
            text = SubscriptionPlan.FULL_DISPLAY,
            style = MaterialTheme.typography.headlineMedium,
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
                SectionLabel("Included")
                PremiumFeature.entries.forEach { feature ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(text = feature.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SectionLabel("Always free, no account")
                Text(
                    text = "Alaska statutes, federal statutes, and the situation analyzer. " +
                        "Knowing your rights should not be behind a paywall.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        when {
            state is AccountState.SignedIn && state.subscription is SubscriptionStatus.Active ->
                Text(
                    text = "You're subscribed. Case law and the AI chat are unlocked.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

            state is AccountState.SignedIn ->
                Button(onClick = onSubscribe, modifier = Modifier.fillMaxWidth()) {
                    Text("Subscribe — ${SubscriptionPlan.FULL_DISPLAY}")
                }

            else -> {
                Button(onClick = onSignUp, modifier = Modifier.fillMaxWidth()) {
                    Text("Create an account")
                }
                OutlinedButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
                    Text("Sign in")
                }
            }
        }
    }
}
