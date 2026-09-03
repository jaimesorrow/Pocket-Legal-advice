package com.pocketlawbook.alaska.data.chat

/**
 * Default implementation when no model API key is configured at build time.
 *
 * Always returns null, which [ChatRepository] treats exactly like a network
 * failure: fall back to showing every on-device-retrieved candidate, with no
 * framing sentence. The chat feature is fully usable without this - it just
 * loses the one-sentence framing and the model's narrowing of candidates.
 */
class NullFramingService : LanguageModelFramingService {
    override suspend fun frame(query: String, candidates: List<CandidatePassage>): FramingResult? = null
}
