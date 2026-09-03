package com.pocketlawbook.alaska.data.remote.api

import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Coverage for realistic phrasings a non-lawyer would actually type, not just
 * the exact rule words. This is the part of the pipeline most likely to
 * silently return zero matches for a real situation, so these fixtures lean
 * on how people describe things in a panic rather than legal terminology.
 */
@RunWith(JUnit4::class)
class OnDeviceLegalAnalyzerTest {

    private val analyzer = OnDeviceLegalAnalyzer()

    // =========================================================================
    // 1. Arrest / prompt-arraignment phrasing
    // =========================================================================

    @Test
    fun `matches an arrest described as being locked up`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "They locked me up and I still haven't seen a judge"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE))
    }

    @Test
    fun `matches an arrest described as being held overnight`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "I was held overnight and nobody told me what was happening"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE))
    }

    // =========================================================================
    // 2. Miranda / custodial questioning phrasing
    // =========================================================================

    @Test
    fun `matches questioning described as kept asking me things`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "The officer kept asking me things in a small room"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_MIRANDA))
    }

    @Test
    fun `matches questioning described as not being free to leave`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "I wasn't free to leave and they never mirandized me"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_MIRANDA))
    }

    // =========================================================================
    // 3. Right to counsel phrasing
    // =========================================================================

    @Test
    fun `matches asking for a lawyer being denied`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "I asked for a lawyer and they denied me a lawyer anyway"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_COUNSEL))
    }

    // =========================================================================
    // 4. Warrantless search phrasing
    // =========================================================================

    @Test
    fun `matches a vehicle search described informally`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "The cops went through my truck without asking"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_SEARCH_WITHOUT_WARRANT))
    }

    @Test
    fun `matches a pat-down described informally`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "An officer frisked me on the sidewalk, no warrant"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_SEARCH_WITHOUT_WARRANT))
    }

    // =========================================================================
    // 5. Habitability phrasing
    // =========================================================================

    @Test
    fun `matches a heat complaint described informally`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "It's been below freezing for a week and there's no heat"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_HABITABILITY_HEAT))
    }

    @Test
    fun `matches a landlord who has not fixed a known problem`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "There's black mold in the bathroom and my landlord won't fix it"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_HABITABILITY_HEAT))
    }

    // =========================================================================
    // 6. Landlord nonpayment-notice phrasing
    // =========================================================================

    @Test
    fun `matches a pay-or-quit notice described informally`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "My landlord gave me a notice to pay because I'm behind on rent"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_LANDLORD_NONPAYMENT_NOTICE))
    }

    // =========================================================================
    // 7. Recording police phrasing
    // =========================================================================

    @Test
    fun `matches an officer stopping someone from filming`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "The officer told me to stop filming and took my phone"
        ).violations.map { it.key }
        assertTrue(keys.contains(VerifiedContentSeed.KEY_RECORDING_POLICE))
    }

    // =========================================================================
    // 8. Unrelated input matches nothing
    // =========================================================================

    @Test
    fun `an unrelated query matches no violation keys`() = runTest {
        val keys = analyzer.analyzeLegalSituation(
            "What time does the DMV open on Saturdays"
        ).violations.map { it.key }
        assertEquals(emptyList<String>(), keys)
    }
}
