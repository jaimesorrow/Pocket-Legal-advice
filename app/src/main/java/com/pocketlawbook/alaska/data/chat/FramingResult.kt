package com.pocketlawbook.alaska.data.chat

/**
 * What the language model is trusted to produce, and nothing more.
 *
 * [selectedKeys] narrows the candidates it was given down to the ones it judged
 * relevant - it can never introduce a key it wasn't shown. [framingSentence] is
 * the model's one piece of free-text writing; it is null whenever it was empty,
 * missing, or looked like it might contain a citation (see
 * [FramingResponseParser]) - never surfaced to the user unfiltered.
 */
data class FramingResult(
    val framingSentence: String?,
    val selectedKeys: List<String>
)
