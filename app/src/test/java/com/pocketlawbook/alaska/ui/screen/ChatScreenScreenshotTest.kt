package com.pocketlawbook.alaska.ui.screen

import app.cash.paparazzi.Paparazzi
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.ui.model.ChatAnswer
import com.pocketlawbook.alaska.ui.model.ChatTurn
import com.pocketlawbook.alaska.ui.model.VerifiedActionStep
import com.pocketlawbook.alaska.ui.theme.PocketLawbookTheme
import org.junit.Rule
import org.junit.Test

/**
 * Headless screenshots of AiChatScreen via Paparazzi (layoutlib rendering, no
 * emulator or device) - this environment has no hardware-accelerated emulator
 * available, so this is the way to see the screen render for real without one.
 */
class ChatScreenScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi()

    private val jurisdictionFor: (String) -> Jurisdiction = { key ->
        if (key == "VIOLATION_RIGHT_TO_RECORD_POLICE") Jurisdiction.FEDERAL else Jurisdiction.ALASKA
    }

    @Test
    fun `chat with a framed answer and a verified match`() {
        val turns = listOf(
            ChatTurn(
                query = "The officer told me to stop filming and took my phone",
                answer = ChatAnswer(
                    framingSentence = "This looks like it may touch on your right to record police.",
                    matches = listOf(
                        VerifiedActionStep(
                            violationKey = "VIOLATION_RIGHT_TO_RECORD_POLICE",
                            description = "Fordyce v. City of Seattle, 55 F.3d 436 (9th Cir. 1995): the " +
                                "Ninth Circuit (which includes Alaska) recognized a First Amendment " +
                                "right to film matters of public interest, including police officers " +
                                "carrying out their duties in a public place.",
                            steps = listOf(
                                "If you can do so safely, without touching the officer or blocking " +
                                    "their movement, you may record police performing their duties " +
                                    "in a public place.",
                                "Do not physically interfere or ignore a lawful order to step back."
                            )
                        )
                    )
                )
            )
        )

        paparazzi.snapshot {
            PocketLawbookTheme {
                AiChatScreen(
                    turns = turns,
                    isAsking = false,
                    onAsk = {},
                    onOpenSteps = {},
                    jurisdictionFor = jurisdictionFor
                )
            }
        }
    }

    @Test
    fun `chat with no verified match and one still awaiting an answer`() {
        val turns = listOf(
            ChatTurn(
                query = "What time does the DMV open on Saturdays",
                answer = ChatAnswer(framingSentence = null, matches = emptyList())
            )
        )

        paparazzi.snapshot {
            PocketLawbookTheme {
                AiChatScreen(
                    turns = turns,
                    isAsking = true,
                    onAsk = {},
                    onOpenSteps = {},
                    jurisdictionFor = jurisdictionFor
                )
            }
        }
    }
}
