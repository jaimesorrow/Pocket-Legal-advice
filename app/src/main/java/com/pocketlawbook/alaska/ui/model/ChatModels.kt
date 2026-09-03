package com.pocketlawbook.alaska.ui.model

/**
 * One answer in the chat. [framingSentence] is null whenever the model was
 * unavailable, failed, or its sentence didn't pass [com.pocketlawbook.alaska.data.chat.FramingResponseParser] -
 * [matches] is never empty-because-of-that, since the repository falls back to
 * every retrieved candidate when the model can't be trusted to narrow them.
 */
data class ChatAnswer(
    val framingSentence: String?,
    val matches: List<VerifiedActionStep>
)

/** One question-and-answer turn in the chat thread. */
data class ChatTurn(
    val query: String,
    val answer: ChatAnswer? = null,
    val error: String? = null
)
