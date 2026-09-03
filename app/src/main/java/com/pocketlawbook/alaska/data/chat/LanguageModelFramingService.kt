package com.pocketlawbook.alaska.data.chat

/**
 * The optional, network-dependent half of the chat pipeline.
 *
 * Implementations are trusted only to select among the [CandidatePassage]s
 * they are given and to write one framing sentence - never to author legal
 * text or a citation that reaches the user directly. See
 * docs/screen-map.html, "The chat, without breaking your guarantee".
 *
 * Returns null (never throws to the caller) when the model is unavailable,
 * the network is down, or its response fails validation - callers must treat
 * null as "no framing available" and fall back to showing every retrieved
 * candidate unfiltered, exactly like the fully offline analysis pipeline.
 */
interface LanguageModelFramingService {
    suspend fun frame(query: String, candidates: List<CandidatePassage>): FramingResult?
}
