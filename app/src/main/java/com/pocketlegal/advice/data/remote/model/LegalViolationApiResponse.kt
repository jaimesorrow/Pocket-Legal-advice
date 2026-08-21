package com.pocketlegal.advice.data.remote.model

import kotlinx.serialization.Serializable

/**
 * Raw response from the remote/LLM legal analysis API.
 *
 * Only [ViolationDetail.key] may be trusted downstream; the other fields are
 * unverified, potentially hallucinated free text and must never reach the UI.
 */
@Serializable
data class LegalViolationApiResponse(
    val violations: List<ViolationDetail>
)

@Serializable
data class ViolationDetail(
    val key: String,
    val title: String,
    val description: String,
    val recommendation: String
)
