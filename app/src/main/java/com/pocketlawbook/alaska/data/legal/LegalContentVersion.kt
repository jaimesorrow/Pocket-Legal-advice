package com.pocketlawbook.alaska.data.legal

/** Immutable metadata describing one verified legal-content dataset. */
data class LegalContentVersion(
    val version: String,
    val jurisdiction: Set<LegalJurisdiction>,
    val publishedAtEpochMillis: Long,
    val lastVerifiedAtEpochMillis: Long,
    val manifestSha256: String,
    val sourceCount: Int,
    val status: LegalContentStatus = LegalContentStatus.VERIFIED,
)

enum class LegalJurisdiction {
    ALASKA,
    FEDERAL,
}

enum class LegalContentStatus {
    VERIFIED,
    REVOKED,
}
