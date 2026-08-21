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
 */
class OnDeviceLegalAnalyzer(
    private val rules: List<MatchRule> = DEFAULT_RULES
) : LegalApiService {

    /** A violation key and the terms that suggest it. */
    data class MatchRule(
        val violationKey: String,
        val anyOf: List<String>
    )

    override suspend fun analyzeLegalSituation(query: String): LegalViolationApiResponse {
        val haystack = query.lowercase()
        val matched = rules
            .filter { rule -> rule.anyOf.any { haystack.contains(it) } }
            .map { ViolationDetail(key = it.violationKey, title = "", description = "", recommendation = "") }
        return LegalViolationApiResponse(violations = matched)
    }

    companion object {
        private val DEFAULT_RULES = listOf(
            MatchRule(
                VerifiedContentSeed.KEY_PROMPT_PROBABLE_CAUSE,
                listOf("arrest", "arrested", "jail", "held", "booking", "judge", "arraign", "custody")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_MIRANDA,
                listOf("question", "questioned", "interrogat", "my rights", "read me", "miranda", "statement")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_COUNSEL,
                listOf("lawyer", "attorney", "counsel", "public defender")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_SEARCH_WITHOUT_WARRANT,
                listOf("search", "searched", "warrant", "pulled me over", "my car", "my bag", "my phone")
            ),
            MatchRule(
                VerifiedContentSeed.KEY_HABITABILITY_HEAT,
                listOf("landlord", "rent", "heat", "furnace", "apartment", "eviction", "water", "mold")
            )
        )
    }
}
