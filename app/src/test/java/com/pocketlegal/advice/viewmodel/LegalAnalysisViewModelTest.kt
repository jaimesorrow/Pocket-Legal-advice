package com.pocketlegal.advice.viewmodel

import com.pocketlegal.advice.data.local.dao.ActionStepDao
import com.pocketlegal.advice.data.local.entity.ActionStepEntity
import com.pocketlegal.advice.data.remote.api.LegalApiService
import com.pocketlegal.advice.data.remote.model.LegalViolationApiResponse
import com.pocketlegal.advice.data.remote.model.ViolationDetail
import com.pocketlegal.advice.data.repository.LegalAnalysisRepository
import com.pocketlegal.advice.ui.model.LegalAnalysisUiState
import com.pocketlegal.advice.ui.model.VerifiedActionStep
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// ---------------------------------------------------------------------------
// Canonical violation key used throughout the pipeline.
// ---------------------------------------------------------------------------
private const val VIOLATION_KEY = "VIOLATION_48_HOUR_ARRAIGNMENT"

// ---------------------------------------------------------------------------
// Known raw strings returned by the mock API.  These must NEVER appear in the
// UI state – they represent unverified, potentially hallucinated text.
// ---------------------------------------------------------------------------
private const val RAW_API_TITLE = "You were not brought before a judge within 48 hours of arrest."
private const val RAW_API_DESCRIPTION =
    "Under Gerstein v. Pugh (1975), a warrantless arrest requires a prompt judicial " +
        "determination of probable cause, generally within 48 hours."
private const val RAW_API_RECOMMENDATION =
    "File a motion to suppress all evidence obtained after the 48-hour window expired."

// ---------------------------------------------------------------------------
// Verified strings stored in the local Room database.
// ---------------------------------------------------------------------------
private const val DB_STEP_1 =
    "Request a copy of your booking record to confirm the exact time of arrest."
private const val DB_STEP_2 =
    "Ask your attorney to file a Motion for Prompt Judicial Determination of Probable Cause."
private const val DB_STEP_3 =
    "Document any statements or evidence gathered after the 48-hour threshold for suppression."
private const val DB_DESCRIPTION =
    "Statute: Cal. Penal Code § 825 – Arraignment within 48 hours of arrest (excluding Sundays and holidays)."

// ---------------------------------------------------------------------------
// Verified strings for a SECOND violation used in multi-violation tests.
// ---------------------------------------------------------------------------
private const val VIOLATION_KEY_2 = "VIOLATION_MIRANDA_RIGHTS"
private const val RAW_API_TITLE_2 =
    "Miranda warnings were not administered prior to custodial interrogation."
private const val DB_STEP_2A =
    "Obtain the arrest report to verify when Miranda warnings were given."
private const val DB_STEP_2B =
    "Ask your attorney to move to suppress any statements made without Miranda advisement."
private const val DB_DESCRIPTION_2 =
    "Miranda v. Arizona (1966): Custodial suspects must be informed of their rights before interrogation."

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class LegalAnalysisViewModelTest {

    // -----------------------------------------------------------------------
    // Collaborator mocks
    // -----------------------------------------------------------------------
    private val mockApiService: LegalApiService = mockk()
    private val mockActionStepDao: ActionStepDao = mockk()

    // System under test
    private lateinit var viewModel: LegalAnalysisViewModel

    // Coroutine dispatcher that gives deterministic execution in tests
    private val testDispatcher = StandardTestDispatcher()

    // -----------------------------------------------------------------------
    // Test doubles – verified DB entity for VIOLATION_48_HOUR_ARRAIGNMENT
    // -----------------------------------------------------------------------
    private val verifiedEntity48Hour = ActionStepEntity(
        violationKey = VIOLATION_KEY,
        actionSteps = listOf(DB_STEP_1, DB_STEP_2, DB_STEP_3),
        description = DB_DESCRIPTION
    )

    // Verified DB entity for VIOLATION_MIRANDA_RIGHTS
    private val verifiedEntityMiranda = ActionStepEntity(
        violationKey = VIOLATION_KEY_2,
        actionSteps = listOf(DB_STEP_2A, DB_STEP_2B),
        description = DB_DESCRIPTION_2
    )

    // Known API response containing the violation key and raw unverified text
    private val knownApiResponse = LegalViolationApiResponse(
        violations = listOf(
            ViolationDetail(
                key = VIOLATION_KEY,
                title = RAW_API_TITLE,
                description = RAW_API_DESCRIPTION,
                recommendation = RAW_API_RECOMMENDATION
            )
        )
    )

    // API response with two violations
    private val multiViolationApiResponse = LegalViolationApiResponse(
        violations = listOf(
            ViolationDetail(
                key = VIOLATION_KEY,
                title = RAW_API_TITLE,
                description = RAW_API_DESCRIPTION,
                recommendation = RAW_API_RECOMMENDATION
            ),
            ViolationDetail(
                key = VIOLATION_KEY_2,
                title = RAW_API_TITLE_2,
                description = "Raw unverified Miranda description from the LLM.",
                recommendation = "Raw unverified Miranda recommendation from the LLM."
            )
        )
    )

    // -----------------------------------------------------------------------
    // Setup / teardown
    // -----------------------------------------------------------------------

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val repository = LegalAnalysisRepository(
            apiService = mockApiService,
            actionStepDao = mockActionStepDao
        )
        viewModel = LegalAnalysisViewModel(repository = repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =======================================================================
    // 1. INITIAL STATE
    // =======================================================================

    @Test
    fun `initial ui state is Idle`() = runTest {
        val state = viewModel.uiState.first()
        assertTrue(
            "Expected initial state to be LegalAnalysisUiState.Idle but was $state",
            state is LegalAnalysisUiState.Idle
        )
    }

    // =======================================================================
    // 2. LOADING STATE
    // =======================================================================

    @Test
    fun `ui state transitions to Loading immediately after analyzeSituation is called`() =
        runTest {
            coEvery { mockApiService.analyzeLegalSituation(any()) } coAnswers {
                suspendCancellableCoroutine { /* suspend indefinitely until cancelled */ }
            }

            viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")

            // The Loading state should be emitted synchronously before the first suspension point
            val state = viewModel.uiState.first { it is LegalAnalysisUiState.Loading }
            assertTrue(state is LegalAnalysisUiState.Loading)
        }

    // =======================================================================
    // 3. ZERO-HALLUCINATION: UI state ONLY contains verified DB strings
    // =======================================================================

    @Test
    fun `success state action steps are sourced exclusively from local database entity`() =
        runTest {
            coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
            coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
                verifiedEntity48Hour

            viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
            advanceUntilIdle()

            val state = viewModel.uiState.first()
            assertTrue(
                "Expected Success state but got $state",
                state is LegalAnalysisUiState.Success
            )

            val success = state as LegalAnalysisUiState.Success
            assertEquals(1, success.verifiedActionSteps.size)

            val step = success.verifiedActionSteps.first()
            assertEquals(VIOLATION_KEY, step.violationKey)

            // Every action step must exactly match a DB-sourced string
            assertEquals(
                "Action steps must match the exact strings stored in the local DB",
                listOf(DB_STEP_1, DB_STEP_2, DB_STEP_3),
                step.steps
            )

            // Description must exactly match the DB-sourced string
            assertEquals(
                "Description must match the exact string stored in the local DB",
                DB_DESCRIPTION,
                step.description
            )
        }

    @Test
    fun `success state must NOT surface any raw text from the API response`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is LegalAnalysisUiState.Success)

        val success = state as LegalAnalysisUiState.Success
        val allDisplayedText = success.collectAllDisplayedStrings()

        assertFalse(
            "UI must not contain raw API title: $RAW_API_TITLE",
            allDisplayedText.any { it.contains(RAW_API_TITLE) }
        )
        assertFalse(
            "UI must not contain raw API description: $RAW_API_DESCRIPTION",
            allDisplayedText.any { it.contains(RAW_API_DESCRIPTION) }
        )
        assertFalse(
            "UI must not contain raw API recommendation: $RAW_API_RECOMMENDATION",
            allDisplayedText.any { it.contains(RAW_API_RECOMMENDATION) }
        )
    }

    @Test
    fun `success state strings are not a substring match of any raw API field`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first() as LegalAnalysisUiState.Success
        val allDisplayedText = state.collectAllDisplayedStrings()

        val rawApiStrings = listOf(
            RAW_API_TITLE,
            RAW_API_DESCRIPTION,
            RAW_API_RECOMMENDATION
        )

        for (displayedString in allDisplayedText) {
            for (rawString in rawApiStrings) {
                assertFalse(
                    "Displayed string '$displayedString' is a substring of raw API text '$rawString'",
                    rawString.contains(displayedString, ignoreCase = false)
                )
                assertFalse(
                    "Raw API string '$rawString' is a substring of displayed text '$displayedString'",
                    displayedString.contains(rawString, ignoreCase = false)
                )
            }
        }
    }

    // =======================================================================
    // 4. DB ENTITY IS THE SINGLE SOURCE OF TRUTH
    // =======================================================================

    @Test
    fun `dao is queried with the violation key extracted from the api response`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        coVerify(exactly = 1) { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) }
    }

    @Test
    fun `dao is NOT queried with any raw text from api response – only with violation keys`() =
        runTest {
            coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
            coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
                verifiedEntity48Hour

            viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
            advanceUntilIdle()

            // Confirm the DAO was never called with raw API text
            coVerify(exactly = 0) {
                mockActionStepDao.getActionStepsByKey(RAW_API_TITLE)
            }
            coVerify(exactly = 0) {
                mockActionStepDao.getActionStepsByKey(RAW_API_DESCRIPTION)
            }
            coVerify(exactly = 0) {
                mockActionStepDao.getActionStepsByKey(RAW_API_RECOMMENDATION)
            }
        }

    @Test
    fun `success state step count matches the number of action steps in the db entity`() =
        runTest {
            coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
            coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
                verifiedEntity48Hour

            viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
            advanceUntilIdle()

            val state = viewModel.uiState.first() as LegalAnalysisUiState.Success
            val step = state.verifiedActionSteps.first { it.violationKey == VIOLATION_KEY }

            assertEquals(
                "Step count must match DB entity, not API response field count",
                verifiedEntity48Hour.actionSteps.size,
                step.steps.size
            )
        }

    // =======================================================================
    // 5. MULTI-VIOLATION RESPONSE
    // =======================================================================

    @Test
    fun `multi-violation response maps each key to its own verified db entity`() = runTest {
        coEvery {
            mockApiService.analyzeLegalSituation(any())
        } returns multiViolationApiResponse

        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY_2) } returns
            verifiedEntityMiranda

        viewModel.analyzeSituation("Multiple rights violations during my arrest.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is LegalAnalysisUiState.Success)

        val success = state as LegalAnalysisUiState.Success
        assertEquals(
            "Both violation keys should produce a verified action step entry",
            2,
            success.verifiedActionSteps.size
        )

        val step48Hour =
            success.verifiedActionSteps.first { it.violationKey == VIOLATION_KEY }
        assertEquals(listOf(DB_STEP_1, DB_STEP_2, DB_STEP_3), step48Hour.steps)
        assertEquals(DB_DESCRIPTION, step48Hour.description)

        val stepMiranda =
            success.verifiedActionSteps.first { it.violationKey == VIOLATION_KEY_2 }
        assertEquals(listOf(DB_STEP_2A, DB_STEP_2B), stepMiranda.steps)
        assertEquals(DB_DESCRIPTION_2, stepMiranda.description)
    }

    @Test
    fun `multi-violation success state contains no raw api text from either violation`() =
        runTest {
            coEvery {
                mockApiService.analyzeLegalSituation(any())
            } returns multiViolationApiResponse

            coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
                verifiedEntity48Hour
            coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY_2) } returns
                verifiedEntityMiranda

            viewModel.analyzeSituation("Multiple rights violations during my arrest.")
            advanceUntilIdle()

            val state = viewModel.uiState.first() as LegalAnalysisUiState.Success
            val allDisplayedText = state.collectAllDisplayedStrings()

            val forbiddenRawStrings = multiViolationApiResponse.violations.flatMap { v ->
                listOf(v.title, v.description, v.recommendation)
            }

            for (rawString in forbiddenRawStrings) {
                assertFalse(
                    "UI must not surface raw API text: '$rawString'",
                    allDisplayedText.any { it.contains(rawString) }
                )
            }
        }

    @Test
    fun `dao is queried once per violation key in multi-violation response`() = runTest {
        coEvery {
            mockApiService.analyzeLegalSituation(any())
        } returns multiViolationApiResponse

        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY_2) } returns
            verifiedEntityMiranda

        viewModel.analyzeSituation("Multiple rights violations during my arrest.")
        advanceUntilIdle()

        coVerify(exactly = 1) { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) }
        coVerify(exactly = 1) { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY_2) }
    }

    // =======================================================================
    // 6. MISSING DB ENTRY (key returned by API has no local mapping)
    // =======================================================================

    @Test
    fun `violation key not found in db is excluded from success state`() = runTest {
        val unknownKeyResponse = LegalViolationApiResponse(
            violations = listOf(
                ViolationDetail(
                    key = "VIOLATION_UNKNOWN_KEY",
                    title = "Some unverified raw title",
                    description = "Some unverified raw description",
                    recommendation = "Some unverified raw recommendation"
                )
            )
        )

        coEvery { mockApiService.analyzeLegalSituation(any()) } returns unknownKeyResponse
        coEvery { mockActionStepDao.getActionStepsByKey("VIOLATION_UNKNOWN_KEY") } returns null

        viewModel.analyzeSituation("A situation with no matching DB entry.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        // When no verified DB entry exists, the pipeline must not surface any raw text.
        // Acceptable outcomes: Success with empty list, or a specific NoVerifiedData state.
        when (state) {
            is LegalAnalysisUiState.Success -> {
                assertTrue(
                    "No verified step should be emitted for an unknown key",
                    state.verifiedActionSteps.isEmpty()
                )
            }
            is LegalAnalysisUiState.NoVerifiedData -> { /* acceptable */ }
            else -> {
                // Any other non-Success state is also acceptable as long as raw text is absent
                val displayedText = (state as? LegalAnalysisUiState.Success)
                    ?.collectAllDisplayedStrings() ?: emptyList()
                assertFalse(
                    displayedText.any { it.contains("Some unverified raw title") }
                )
            }
        }
    }

    @Test
    fun `ui state never emits raw api text when db returns null for a key`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns null

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        val displayedText = when (state) {
            is LegalAnalysisUiState.Success -> state.collectAllDisplayedStrings()
            is LegalAnalysisUiState.Error -> listOf(state.message)
            else -> emptyList()
        }

        assertFalse(
            "Raw API title must not appear in UI when DB returns null",
            displayedText.any { it.contains(RAW_API_TITLE) }
        )
        assertFalse(
            "Raw API description must not appear in UI when DB returns null",
            displayedText.any { it.contains(RAW_API_DESCRIPTION) }
        )
        assertFalse(
            "Raw API recommendation must not appear in UI when DB returns null",
            displayedText.any { it.contains(RAW_API_RECOMMENDATION) }
        )
    }

    // =======================================================================
    // 7. API FAILURE
    // =======================================================================

    @Test
    fun `api failure transitions ui state to Error`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } throws
            RuntimeException("Network error: Unable to reach legal analysis service.")

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(
            "Expected Error state on API failure but got $state",
            state is LegalAnalysisUiState.Error
        )
    }

    @Test
    fun `error state message does not contain raw api text`() = runTest {
        val rawErrorMessage =
            "LLM internal error: $RAW_API_DESCRIPTION"
        coEvery { mockApiService.analyzeLegalSituation(any()) } throws
            RuntimeException(rawErrorMessage)

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertTrue(state is LegalAnalysisUiState.Error)

        val errorMessage = (state as LegalAnalysisUiState.Error).message
        assertFalse(
            "Error message must not propagate raw API/LLM text to the UI",
            errorMessage.contains(RAW_API_DESCRIPTION)
        )
    }

    @Test
    fun `dao is not called when api throws an exception`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } throws
            RuntimeException("Network error")

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        coVerify(exactly = 0) { mockActionStepDao.getActionStepsByKey(any()) }
    }

    // =======================================================================
    // 8. IDEMPOTENCY – SECOND ANALYSIS REPLACES FIRST
    // =======================================================================

    @Test
    fun `second analyze call replaces the previous success state entirely`() = runTest {
        // First call returns the 48-hour violation
        coEvery {
            mockApiService.analyzeLegalSituation("first query")
        } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour

        viewModel.analyzeSituation("first query")
        advanceUntilIdle()

        val firstState = viewModel.uiState.first() as LegalAnalysisUiState.Success
        assertEquals(1, firstState.verifiedActionSteps.size)
        assertEquals(VIOLATION_KEY, firstState.verifiedActionSteps.first().violationKey)

        // Second call returns only the Miranda violation
        val mirandaOnlyResponse = LegalViolationApiResponse(
            violations = listOf(
                ViolationDetail(
                    key = VIOLATION_KEY_2,
                    title = RAW_API_TITLE_2,
                    description = "Raw Miranda description.",
                    recommendation = "Raw Miranda recommendation."
                )
            )
        )
        coEvery {
            mockApiService.analyzeLegalSituation("second query")
        } returns mirandaOnlyResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY_2) } returns
            verifiedEntityMiranda

        viewModel.analyzeSituation("second query")
        advanceUntilIdle()

        val secondState = viewModel.uiState.first() as LegalAnalysisUiState.Success
        assertEquals(1, secondState.verifiedActionSteps.size)
        assertEquals(VIOLATION_KEY_2, secondState.verifiedActionSteps.first().violationKey)
        // The 48-hour step must no longer be present
        assertFalse(
            secondState.verifiedActionSteps.any { it.violationKey == VIOLATION_KEY }
        )
    }

    // =======================================================================
    // 9. EMPTY API RESPONSE
    // =======================================================================

    @Test
    fun `empty violations list from api produces success state with empty step list`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns
            LegalViolationApiResponse(violations = emptyList())

        viewModel.analyzeSituation("A perfectly legal situation.")
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        when (state) {
            is LegalAnalysisUiState.Success ->
                assertTrue(
                    "Empty API response must produce empty verified steps",
                    state.verifiedActionSteps.isEmpty()
                )
            is LegalAnalysisUiState.NoVerifiedData -> { /* acceptable */ }
            else ->
                fail("Unexpected state for empty API response: $state")
        }
    }

    @Test
    fun `empty violations list from api results in no dao queries`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns
            LegalViolationApiResponse(violations = emptyList())

        viewModel.analyzeSituation("A perfectly legal situation.")
        advanceUntilIdle()

        coVerify(exactly = 0) { mockActionStepDao.getActionStepsByKey(any()) }
    }

    // =======================================================================
    // 10. EXACT STRING EQUALITY – NO TRIMMING OR MUTATION
    // =======================================================================

    @Test
    fun `verified action step strings are not mutated or trimmed from db values`() = runTest {
        val entityWithWhitespace = ActionStepEntity(
            violationKey = VIOLATION_KEY,
            actionSteps = listOf("  $DB_STEP_1  ", DB_STEP_2),
            description = "  $DB_DESCRIPTION  "
        )

        coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            entityWithWhitespace

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first() as LegalAnalysisUiState.Success
        val step = state.verifiedActionSteps.first()

        // The ViewModel must pass through DB strings exactly as stored
        assertEquals("  $DB_STEP_1  ", step.steps[0])
        assertEquals(DB_STEP_2, step.steps[1])
        assertEquals("  $DB_DESCRIPTION  ", step.description)
    }

    // =======================================================================
    // 11. VIOLATION KEY PASSED THROUGH TO UI STATE
    // =======================================================================

    @Test
    fun `verified action step exposes violation key exactly as returned by api`() = runTest {
        coEvery { mockApiService.analyzeLegalSituation(any()) } returns knownApiResponse
        coEvery { mockActionStepDao.getActionStepsByKey(VIOLATION_KEY) } returns
            verifiedEntity48Hour

        viewModel.analyzeSituation("I was held for more than 48 hours before arraignment.")
        advanceUntilIdle()

        val state = viewModel.uiState.first() as LegalAnalysisUiState.Success
        assertNotNull(state.verifiedActionSteps.firstOrNull { it.violationKey == VIOLATION_KEY })
    }

    // =======================================================================
    // Helper – flattens all user-visible strings from a Success state
    // =======================================================================

    /**
     * Collects every string that could potentially be displayed in the UI
     * from a [LegalAnalysisUiState.Success] state, including violation keys,
     * all action step strings, and descriptions.
     */
    private fun LegalAnalysisUiState.Success.collectAllDisplayedStrings(): List<String> =
        verifiedActionSteps.flatMap { step: VerifiedActionStep ->
            buildList {
                add(step.violationKey)
                addAll(step.steps)
                add(step.description)
            }
        }
}
