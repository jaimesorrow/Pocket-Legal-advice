package com.pocketlawbook.alaska.data.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * This is the safety-critical piece of the chat pipeline: it decides what a
 * model response is trusted to put in front of the user. Every case here maps
 * to one of the two guarantees FramingResponseParser exists to enforce - see
 * its doc comment.
 */
@RunWith(JUnit4::class)
class FramingResponseParserTest {

    private val candidateKeys = setOf("VIOLATION_A", "VIOLATION_B")

    // =========================================================================
    // 1. A clean, valid response passes through untouched
    // =========================================================================

    @Test
    fun `a plain framing sentence with valid keys passes through`() {
        val result = FramingResponseParser.parse(
            """{"framing": "This may involve a search issue.", "selectedKeys": ["VIOLATION_A"]}""",
            candidateKeys
        )
        assertEquals("This may involve a search issue.", result?.framingSentence)
        assertEquals(listOf("VIOLATION_A"), result?.selectedKeys)
    }

    // =========================================================================
    // 2. Citation-like framing sentences are discarded, not passed through
    // =========================================================================

    @Test
    fun `a framing sentence containing a digit is discarded`() {
        val result = FramingResponseParser.parse(
            """{"framing": "See AS 34.03.220 for details.", "selectedKeys": ["VIOLATION_A"]}""",
            candidateKeys
        )
        assertNull(result?.framingSentence)
        // The selection itself carries no fabrication risk, so it still comes through.
        assertEquals(listOf("VIOLATION_A"), result?.selectedKeys)
    }

    @Test
    fun `a framing sentence containing a section symbol is discarded`() {
        val result = FramingResponseParser.parse(
            """{"framing": "This touches on § rights.", "selectedKeys": []}""",
            candidateKeys
        )
        assertNull(result?.framingSentence)
    }

    // =========================================================================
    // 3. Keys outside the candidate set are silently dropped, never passed through
    // =========================================================================

    @Test
    fun `a selected key outside the candidate set is dropped`() {
        val result = FramingResponseParser.parse(
            """{"framing": "This may apply.", "selectedKeys": ["VIOLATION_A", "VIOLATION_INVENTED"]}""",
            candidateKeys
        )
        assertEquals(listOf("VIOLATION_A"), result?.selectedKeys)
    }

    @Test
    fun `a fully invented key list resolves to an empty selection`() {
        val result = FramingResponseParser.parse(
            """{"framing": "", "selectedKeys": ["MADE_UP_KEY"]}""",
            candidateKeys
        )
        assertNull(result)
    }

    // =========================================================================
    // 4. Malformed or empty responses fail closed to null, never throw
    // =========================================================================

    @Test
    fun `malformed JSON returns null instead of throwing`() {
        val result = FramingResponseParser.parse("not valid json at all", candidateKeys)
        assertNull(result)
    }

    @Test
    fun `an empty framing and empty selection returns null`() {
        val result = FramingResponseParser.parse(
            """{"framing": "", "selectedKeys": []}""",
            candidateKeys
        )
        assertNull(result)
    }

    @Test
    fun `a response missing the selectedKeys field still parses the framing sentence`() {
        val result = FramingResponseParser.parse(
            """{"framing": "This may be relevant."}""",
            candidateKeys
        )
        assertEquals("This may be relevant.", result?.framingSentence)
        assertTrue(result?.selectedKeys.orEmpty().isEmpty())
    }
}
