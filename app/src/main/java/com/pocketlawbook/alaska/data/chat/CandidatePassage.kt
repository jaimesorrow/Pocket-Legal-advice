package com.pocketlawbook.alaska.data.chat

/**
 * One already-vetted corpus entry surfaced by on-device retrieval, offered to
 * the language model as something it may select — never as something it may
 * write new text about. [description] is the verified DB text; the model never
 * sees or produces a citation.
 */
data class CandidatePassage(
    val violationKey: String,
    val description: String
)
