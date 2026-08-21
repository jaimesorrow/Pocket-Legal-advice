package com.pocketlawbook.alaska.data.remote.model

/**
 * Raw response from the remote/LLM legal analysis API.
 *
 * Only [ViolationDetail.key] may be trusted downstream; the other fields are
 * unverified, potentially hallucinated free text and must never reach the UI.
 */
data class LegalViolationApiResponse(
    val violations: List<ViolationDetail>
)

data class ViolationDetail(
    val key: String,
    val title: String,
    val description: String,
    val recommendation: String
)
