package com.pocketlawbook.alaska.data

import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import com.pocketlawbook.alaska.data.remote.api.OnDeviceLegalAnalyzer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

// ---------------------------------------------------------------------------
// Situations a user might actually describe, and the keys they should produce.
// ---------------------------------------------------------------------------
private const val ARREST_AND_SILENCE =
    "I was arrested and held overnight and nobody read me my rights"
private const val NO_HEAT = "My landlord will not fix the heat"
private const val CAR_SEARCH = "They searched my car without a warrant"

// ---------------------------------------------------------------------------
// Sentences that must NOT match. Naive substring matching finds legal keywords
// buried inside ordinary words -- "rent" inside "parent", "search" inside
// "research" -- and shows a distressed user law that has nothing to do with
// their situation.
// ---------------------------------------------------------------------------
private const val RELATIVE_ARRESTED = "My parent was arrested and questioned"
private const val DOING_RESEARCH = "I did some research about my case"
private const val NO_LEGAL_CONTENT = "The current situation is different"
private const val APPARENTLY = "It was apparent nobody helped me"

@RunWith(JUnit4::class)
class OnDeviceLegalAnalyzerTest {

    private val analyzer = OnDeviceLegalAnalyzer()

    private suspend fun keysFor(query: String): List<String> =
        analyzer.analyzeLegalSituation(query).violations.map { it.key }

    // =======================================================================
    // 1. TRUE POSITIVES - the situations the rules exist to catch
    // =======================================================================

    @Test
    fun `arrest with no rights read matches custody and miranda`() = runTest {
        val keys = keysFor(ARREST_AND_SILENCE)

        assertTrue(
            "expected the prompt-probable-cause key, got $keys",
            keys.contains(VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE)
        )
        assertTrue(
            "expected the Miranda key, got $keys",
            keys.contains(VerifiedContentSeed.KEY_MIRANDA)
        )
    }

    @Test
    fun `landlord not fixing heat matches habitability`() = runTest {
        assertTrue(keysFor(NO_HEAT).contains(VerifiedContentSeed.KEY_HABITABILITY_HEAT))
    }

    @Test
    fun `shut off water matches habitability`() = runTest {
        // AS 34.03.100 covers heat *and* running water, so both must reach the rule.
        assertTrue(
            keysFor("The water has been shut off for a week")
                .contains(VerifiedContentSeed.KEY_HABITABILITY_HEAT)
        )
    }

    @Test
    fun `search of a car matches the warrantless search key`() = runTest {
        assertTrue(keysFor(CAR_SEARCH).contains(VerifiedContentSeed.KEY_SEARCH_WITHOUT_WARRANT))
    }

    @Test
    fun `a word carrying a keyword as a prefix still matches`() = runTest {
        // "arrest" must still reach "arrested", "question" must reach "questioned".
        assertTrue(keysFor("I was arrested").contains(VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE))
        assertTrue(keysFor("They questioned me").contains(VerifiedContentSeed.KEY_MIRANDA))
        assertTrue(keysFor("They interrogated me").contains(VerifiedContentSeed.KEY_MIRANDA))
    }

    // =======================================================================
    // 2. FALSE POSITIVES - keywords buried inside unrelated words
    // =======================================================================

    @Test
    fun `parent does not match the rent keyword`() = runTest {
        val keys = keysFor(RELATIVE_ARRESTED)

        assertTrue(
            "\"parent\" contains \"rent\" but says nothing about housing, got $keys",
            !keys.contains(VerifiedContentSeed.KEY_HABITABILITY_HEAT)
        )
        // The genuine matches in that sentence must survive.
        assertTrue(keys.contains(VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE))
        assertTrue(keys.contains(VerifiedContentSeed.KEY_MIRANDA))
    }

    @Test
    fun `research does not match the search keyword`() = runTest {
        assertTrue(
            "\"research\" contains \"search\" but describes no search",
            !keysFor(DOING_RESEARCH).contains(VerifiedContentSeed.KEY_SEARCH_WITHOUT_WARRANT)
        )
    }

    @Test
    fun `a sentence with no legal content matches nothing`() = runTest {
        assertEquals(emptyList<String>(), keysFor(NO_LEGAL_CONTENT))
        assertEquals(emptyList<String>(), keysFor(APPARENTLY))
    }

    @Test
    fun `an unrelated complaint matches nothing`() = runTest {
        assertEquals(emptyList<String>(), keysFor("My neighbour's dog keeps barking at night"))
    }

    // =======================================================================
    // 3. RAW FIELDS - the analyzer must never supply user-visible text
    // =======================================================================

    @Test
    fun `analyzer emits keys only and leaves narrative fields empty`() = runTest {
        val violations = analyzer.analyzeLegalSituation(ARREST_AND_SILENCE).violations

        assertTrue("expected at least one match", violations.isNotEmpty())
        violations.forEach { violation ->
            assertEquals("", violation.title)
            assertEquals("", violation.description)
            assertEquals("", violation.recommendation)
        }
    }

    @Test
    fun `every key the analyzer can emit exists in the verified store`() = runTest {
        // A key with no verified entry is silently dropped downstream, so a typo
        // here would make a rule quietly dead rather than loudly broken.
        val allProbes = listOf(
            ARREST_AND_SILENCE, NO_HEAT, CAR_SEARCH,
            "I asked for a lawyer", "they searched my bag", "the furnace is broken"
        )
        allProbes.forEach { probe ->
            keysFor(probe).forEach { key ->
                assertTrue(
                    "key $key has no entry in VerifiedContentSeed",
                    VerifiedContentSeed.entries.containsKey(key)
                )
            }
        }
    }

    // =======================================================================
    // 4. EMPTY AND BLANK INPUT
    // =======================================================================

    @Test
    fun `blank query matches nothing`() = runTest {
        assertEquals(emptyList<String>(), keysFor(""))
        assertEquals(emptyList<String>(), keysFor("     "))
    }
}
