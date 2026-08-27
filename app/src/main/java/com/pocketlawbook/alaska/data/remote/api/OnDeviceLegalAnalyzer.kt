package com.pocketlawbook.alaska.data.remote.api

import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import com.pocketlawbook.alaska.data.remote.model.LegalViolationApiResponse
import com.pocketlawbook.alaska.data.remote.model.ViolationDetail

/**
 * On-device implementation of [LegalApiService].
 *
 * It matches a described situation to violation keys with local keyword rules —
 * no network, so the analysis feature works in airplane mode and in villages with
 * no signal, which is when it is most likely to be needed.
 *
 * Note what it puts in [ViolationDetail.title], [ViolationDetail.description] and
 * [ViolationDetail.recommendation]: nothing. Those fields exist on the wire model
 * because a remote LLM backend would populate them, and the pipeline's whole job
 * is to discard them. Leaving them empty here makes that contract obvious — the
 * key is the only field anything downstream is allowed to read.
 *
 * ## Why terms match at a word boundary
 *
 * Plain `contains` finds keywords buried inside unrelated words, and in this app
 * that means showing a frightened person law that has nothing to do with them:
 * "rent" sits inside "parent", "current" and "apparent", so "my parent was
 * arrested" claimed a landlord dispute; "search" sits inside "research".
 *
 * Terms therefore match only at the start of a word, but may run past the end of
 * one — a prefix match. "arrest" still reaches "arrested", "question" reaches
 * "questioned", and "interrogat" reaches both "interrogated" and
 * "interrogation", while "rent" no longer reaches "parent". Because matching is
 * prefix-based, a rule lists the shortest stem rather than each inflection.
 */
class OnDeviceLegalAnalyzer(
    rules: List<MatchRule> = DEFAULT_RULES
) : LegalApiService {

    /** A violation key and the terms that suggest it. */
    data class MatchRule(
        val violationKey: String,
        val anyOf: List<String>
    )

    /** Rules with their term patterns compiled once, not per query. */
    private val compiled: List<Pair<String, List<Regex>>> = rules.map { rule ->
        rule.violationKey to rule.anyOf.map { term ->
            Regex("\\b" + Regex.escape(term), RegexOption.IGNORE_CASE)
        }
    }

    override suspend fun analyzeLegalSituation(query: String): LegalViolationApiResponse {
        val matched = compiled
            .filter { (_, patterns) -> patterns.any { it.containsMatchIn(query) } }
            .map { (key, _) ->
                ViolationDetail(key = key, title = "", description = "", recommendation = "")
            }
        return LegalViolationApiResponse(violations = matched)
    }

    companion object {
        private val DEFAULT_RULES = listOf(
            MatchRule(
                VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE,
                listOf("arrest", "jail", "held", "booking", "judge", "arraign", "custody")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_MIRANDA,
                listOf("question", "interrogat", "my rights", "read me", "miranda", "statement")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_COUNSEL,
                listOf("lawyer", "attorney", "counsel", "public defender")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_SEARCH_WITHOUT_WARRANT,
                listOf("search", "warrant", "pulled me over", "my car", "my bag", "my phone")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_HABITABILITY_HEAT,
                listOf("landlord", "rent", "heat", "furnace", "apartment", "eviction", "water", "mold")
            )
        )
    }
}
