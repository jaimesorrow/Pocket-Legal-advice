package com.pocketlawbook.alaska.ui.screen

import app.cash.paparazzi.Paparazzi
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.ui.model.LegalAnalysisUiState
import com.pocketlawbook.alaska.ui.model.VerifiedActionStep
import com.pocketlawbook.alaska.ui.theme.PocketLawbookTheme
import org.junit.Rule
import org.junit.Test

/** Headless screenshot of AnalysisScreen's new persistent disclaimer + a matched result. */
class AnalysisScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    @Test
    fun `analysis screen with a matched result and the not-exhaustive disclaimer`() {
        val state = LegalAnalysisUiState.Success(
            verifiedActionSteps = listOf(
                VerifiedActionStep(
                    violationKey = "VIOLATION_LANDLORD_HABITABILITY",
                    description = "Alaska Uniform Residential Landlord and Tenant Act, AS 34.03.100: a " +
                        "landlord must maintain the premises in a fit and habitable condition, " +
                        "including supplying heat and running water.",
                    steps = listOf(
                        "Photograph the conditions and record the indoor temperature with the date.",
                        "Notify your landlord in writing and keep a copy of what you sent."
                    )
                )
            )
        )

        paparazzi.snapshot {
            PocketLawbookTheme {
                AnalysisScreen(
                    uiState = state,
                    onAnalyze = {},
                    onOpenSteps = {},
                    jurisdictionFor = { Jurisdiction.ALASKA }
                )
            }
        }
    }
}
