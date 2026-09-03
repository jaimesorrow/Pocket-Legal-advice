package com.pocketlawbook.alaska.data.chat

import org.json.JSONObject

/**
 * Turns raw model output into a [FramingResult], enforcing the two guarantees
 * the model is never trusted to enforce on itself:
 *
 * 1. [FramingResult.selectedKeys] can only contain keys from [candidateKeys] -
 *    a key the model invents (or that leaked in from a different call) is
 *    silently dropped, never passed through.
 * 2. [FramingResult.framingSentence] is discarded (set to null) if it contains
 *    a digit or a section symbol - a plain framing sentence has no legitimate
 *    reason to contain either, and a citation always does. This is the
 *    structural check docs/screen-map.html calls for: "assert that the framing
 *    sentence is rejected if it contains a digit-and-section pattern."
 *
 * Pure and synchronous on purpose - no network, no coroutines - so the safety
 * logic here is testable without mocking a model call.
 */
object FramingResponseParser {

    private val citationLike = Regex("""[0-9]|§""")

    fun parse(rawModelText: String, candidateKeys: Set<String>): FramingResult? {
        val json = try {
            JSONObject(rawModelText)
        } catch (e: Exception) {
            return null
        }

        val rawFraming = json.optString("framing", "").trim()
        val framingSentence = rawFraming.takeIf { it.isNotEmpty() && !citationLike.containsMatchIn(it) }

        val selectedArray = json.optJSONArray("selectedKeys")
        val selectedKeys = buildList {
            if (selectedArray != null) {
                for (i in 0 until selectedArray.length()) {
                    add(selectedArray.optString(i))
                }
            }
        }.filter { it in candidateKeys }

        return if (framingSentence == null && selectedKeys.isEmpty()) null
        else FramingResult(framingSentence, selectedKeys)
    }
}
