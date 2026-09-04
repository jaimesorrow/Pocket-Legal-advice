---
name: android-compose
description: Compose component and file structure conventions in Alaska's Pocket Lawbook. Include triggers: "create a Composable", "write a screen", "build a component", "Android UI".
---

## FILE STRUCTURE
Package root `com.pocketlawbook.alaska`, flat by role (no per-feature packages):
- `ui/screen/` — screen-level Composables, grouped several-per-file by area (`AnalysisScreen.kt`, `ActionStepsScreen.kt`, `AccountScreens.kt`, `GatedScreens.kt`, `LegalScreens.kt`, `WelcomeScreen.kt`)
- `ui/component/` — shared building blocks (`Common.kt`)
- `ui/navigation/` — `PocketLawbookApp.kt`, `AppDrawer.kt`, `Destinations.kt`
- `ui/model/` — UI-only data shapes (`LegalAnalysisUiState.kt`)
- `ui/theme/` — `Theme.kt`
- `viewmodel/`, `di/` — one file per `ViewModel`; single `AppContainer.kt`

## COMPOSABLE NAMING & ORGANIZATION
Screens are named `<Noun>Screen` (`AnalysisScreen`, `ActionStepsScreen`, `WelcomeScreen`); shared pieces are named for what they render (`JurisdictionChip`, `SectionLabel`, `DisclaimerBar` in `ui/component/Common.kt`). Screens are stateless: `ui/screen/AnalysisScreen.kt`'s `AnalysisScreen(uiState: LegalAnalysisUiState, onAnalyze: (String) -> Unit, onOpenSteps: (String) -> Unit, jurisdictionFor: (String) -> Jurisdiction, modifier: Modifier = Modifier)` takes state and callbacks only, no ViewModel reference. Every screen composable's last parameter is `modifier: Modifier = Modifier`.

## STATE MANAGEMENT
ViewModels expose `StateFlow`; `viewmodel/LegalAnalysisViewModel.kt` holds `private val _uiState = MutableStateFlow<LegalAnalysisUiState>(Idle)` / `val uiState: StateFlow<LegalAnalysisUiState>`. The nav-graph call site collects it: `val state by vm.uiState.collectAsStateWithLifecycle()` (`ui/navigation/PocketLawbookApp.kt`), then passes `state` down as a parameter. Ephemeral per-screen input stays local via `rememberSaveable { mutableStateOf(...) }` inside the screen itself (`AnalysisScreen`'s `query`). ViewModels are constructed per-route with a `ViewModelProvider.Factory` built from `AppContainer` (`container.legalAnalysisViewModelFactory`), not injected.

## NAVIGATION
Single `NavHost` in `AppNavHost` (`ui/navigation/PocketLawbookApp.kt`), routes centralized as `String` constants in `Routes` (`ui/navigation/Destinations.kt`). Argument routes use `{name}` templates plus a builder function, e.g. `const val ACTION_STEPS = "steps/{violationKey}"` / `fun actionSteps(violationKey: String)`; read back via `entry.arguments?.getString("violationKey")`. No `navDeepLink` / deep linking configured.

## TESTING PATTERNS
No Compose UI tests exist (`app/src/androidTest` is empty) and no `@Preview` composables anywhere. Only `viewmodel/LegalAnalysisViewModel.kt` has coverage, via plain JVM tests (`LegalAnalysisViewModelTest`, JUnit4 + MockK), asserting on `uiState` values directly rather than through Compose.
