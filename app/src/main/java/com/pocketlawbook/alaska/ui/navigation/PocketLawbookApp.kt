package com.pocketlawbook.alaska.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pocketlawbook.alaska.data.account.AccountState
import com.pocketlawbook.alaska.data.account.hasPremiumAccess
import com.pocketlawbook.alaska.data.account.lockReason
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.di.AppContainer
import com.pocketlawbook.alaska.ui.component.DisclaimerBar
import com.pocketlawbook.alaska.ui.screen.LegalConsentScreen
import com.pocketlawbook.alaska.ui.screen.LegalDocumentScreen
import com.pocketlawbook.alaska.ui.screen.LegalIndexScreen
import com.pocketlawbook.alaska.ui.screen.AccountScreen
import com.pocketlawbook.alaska.ui.screen.ActionStepsScreen
import com.pocketlawbook.alaska.ui.screen.AiChatScreen
import com.pocketlawbook.alaska.ui.screen.AnalysisScreen
import com.pocketlawbook.alaska.ui.screen.CaseLawScreen
import com.pocketlawbook.alaska.ui.screen.CredentialsScreen
import com.pocketlawbook.alaska.ui.screen.LawBrowseScreen
import com.pocketlawbook.alaska.ui.screen.PaywallScreen
import com.pocketlawbook.alaska.ui.screen.WelcomeScreen
import com.pocketlawbook.alaska.viewmodel.AccountViewModel
import com.pocketlawbook.alaska.viewmodel.ChatViewModel
import com.pocketlawbook.alaska.viewmodel.LegalAnalysisViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketLawbookApp(container: AppContainer) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val accountViewModel: AccountViewModel = viewModel(
        factory = AccountViewModel.Factory(container.accountRepository)
    )
    val accountState by accountViewModel.state.collectAsStateWithLifecycle()
    val accountError by accountViewModel.errorMessage.collectAsStateWithLifecycle()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val hasAccepted by container.consentRepository.hasAcceptedCurrent.collectAsStateWithLifecycle()

    fun closeDrawer() = scope.launch { drawerState.close() }

    fun go(route: String) {
        closeDrawer()
        navController.navigate(route) { launchSingleTop = true }
    }

    // The terms gate everything. Until they are accepted for the current version
    // there is no drawer and no other destination — continued use is not consent.
    if (!hasAccepted) {
        var reading by rememberSaveable { mutableStateOf<String?>(null) }
        val currentlyReading = reading
        if (currentlyReading == null) {
            LegalConsentScreen(
                onAccept = { container.consentRepository.accept() },
                onOpenDocument = { reading = it }
            )
        } else {
            BackHandler { reading = null }
            LegalDocumentScreen(documentId = currentlyReading)
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                accountState = accountState,
                currentRoute = currentRoute,
                onNavigate = { route ->
                    // The sign-in row doubles as sign-out once signed in.
                    if (route == Routes.SIGN_IN && accountState is AccountState.SignedIn) {
                        accountViewModel.signOut()
                        closeDrawer()
                    } else {
                        go(route)
                    }
                },
                onLockedClick = { go(Routes.PAYWALL) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(titleForRoute(currentRoute)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open navigation")
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                DisclaimerBar()
                AppNavHost(
                    navController = navController,
                    container = container,
                    accountViewModel = accountViewModel,
                    accountError = accountError,
                    accountState = accountState,
                    onNavigate = ::go
                )
            }
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    container: AppContainer,
    accountViewModel: AccountViewModel,
    accountError: String?,
    accountState: AccountState,
    onNavigate: (String) -> Unit
) {
    val unlocked = accountState.hasPremiumAccess
    val lockReason = accountState.lockReason

    NavHost(navController = navController, startDestination = Routes.WELCOME) {

        composable(Routes.WELCOME) {
            WelcomeScreen(
                onAnalyze = { onNavigate(Routes.ANALYZE) },
                onBrowseAlaska = { onNavigate(Routes.ALASKA_LAW) },
                onBrowseFederal = { onNavigate(Routes.FEDERAL_LAW) },
                onSeeSubscription = { onNavigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.ANALYZE) {
            val vm: LegalAnalysisViewModel = viewModel(
                factory = container.legalAnalysisViewModelFactory
            )
            val state by vm.uiState.collectAsStateWithLifecycle()
            AnalysisScreen(
                uiState = state,
                onAnalyze = vm::analyzeSituation,
                onOpenSteps = { key -> onNavigate(Routes.actionSteps(key)) },
                jurisdictionFor = container::jurisdictionOf
            )
        }

        composable(Routes.ACTION_STEPS) { entry ->
            val key = entry.arguments?.getString("violationKey").orEmpty()
            ActionStepsScreen(entity = container.verifiedEntry(key))
        }

        composable(Routes.ALASKA_LAW) {
            LawBrowseScreen(
                jurisdiction = Jurisdiction.ALASKA,
                onOpenSteps = { key -> onNavigate(Routes.actionSteps(key)) }
            )
        }

        composable(Routes.FEDERAL_LAW) {
            LawBrowseScreen(
                jurisdiction = Jurisdiction.FEDERAL,
                onOpenSteps = { key -> onNavigate(Routes.actionSteps(key)) }
            )
        }

        composable(Routes.ALASKA_CASE_LAW) {
            CaseLawScreen(
                jurisdiction = Jurisdiction.ALASKA,
                unlocked = unlocked,
                lockReason = lockReason,
                onUnlock = { onNavigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.FEDERAL_CASE_LAW) {
            CaseLawScreen(
                jurisdiction = Jurisdiction.FEDERAL,
                unlocked = unlocked,
                lockReason = lockReason,
                onUnlock = { onNavigate(Routes.PAYWALL) }
            )
        }

        composable(Routes.AI_CHAT) {
            val vm: ChatViewModel = viewModel(factory = container.chatViewModelFactory)
            val turns by vm.turns.collectAsStateWithLifecycle()
            val isAsking by vm.isAsking.collectAsStateWithLifecycle()
            AiChatScreen(
                turns = turns,
                isAsking = isAsking,
                onAsk = vm::ask,
                onOpenSteps = { key -> onNavigate(Routes.actionSteps(key)) },
                jurisdictionFor = container::jurisdictionOf
            )
        }

        composable(Routes.SIGN_IN) {
            CredentialsScreen(
                isSignUp = false,
                errorMessage = accountError,
                onSubmit = { email, password ->
                    accountViewModel.signIn(email, password) { onNavigate(Routes.ACCOUNT) }
                },
                onSwitchMode = {
                    accountViewModel.clearError()
                    onNavigate(Routes.SIGN_UP)
                }
            )
        }

        composable(Routes.SIGN_UP) {
            CredentialsScreen(
                isSignUp = true,
                errorMessage = accountError,
                onSubmit = { email, password ->
                    accountViewModel.signUp(email, password) { onNavigate(Routes.PAYWALL) }
                },
                onSwitchMode = {
                    accountViewModel.clearError()
                    onNavigate(Routes.SIGN_IN)
                }
            )
        }

        composable(Routes.ACCOUNT) {
            AccountScreen(
                state = accountState,
                onSignIn = { onNavigate(Routes.SIGN_IN) },
                onSignUp = { onNavigate(Routes.SIGN_UP) },
                onSignOut = accountViewModel::signOut,
                onSubscribe = { accountViewModel.subscribe() },
                onCancel = accountViewModel::cancelSubscription
            )
        }

        composable(Routes.LEGAL) {
            LegalIndexScreen(
                onOpenDocument = { id -> onNavigate(Routes.legalDocument(id)) }
            )
        }

        composable(Routes.LEGAL_DOCUMENT) { entry ->
            LegalDocumentScreen(
                documentId = entry.arguments?.getString("documentId").orEmpty()
            )
        }

        composable(Routes.PAYWALL) {
            PaywallScreen(
                state = accountState,
                onSubscribe = { accountViewModel.subscribe() },
                onSignUp = { onNavigate(Routes.SIGN_UP) },
                onSignIn = { onNavigate(Routes.SIGN_IN) }
            )
        }
    }
}

private fun titleForRoute(route: String?): String = when (route) {
    Routes.ANALYZE -> "What happened?"
    Routes.ALASKA_LAW -> "Alaska law"
    Routes.FEDERAL_LAW -> "Federal law"
    Routes.ALASKA_CASE_LAW -> "Alaska case law"
    Routes.FEDERAL_CASE_LAW -> "Federal case law"
    Routes.AI_CHAT -> "AI chat"
    Routes.SIGN_IN -> "Sign in"
    Routes.SIGN_UP -> "Sign up"
    Routes.ACCOUNT -> "Your account"
    Routes.PAYWALL -> "Subscription"
    Routes.LEGAL -> "Legal & privacy"
    Routes.LEGAL_DOCUMENT -> "Legal"
    Routes.ACTION_STEPS -> "Action steps"
    else -> "Alaska's Pocket Lawbook"
}
