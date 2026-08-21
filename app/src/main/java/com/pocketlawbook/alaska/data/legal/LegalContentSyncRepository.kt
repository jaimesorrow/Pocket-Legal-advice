package com.pocketlawbook.alaska.data.legal

/**
 * Trust-boundary contract for refreshing legal content.
 *
 * Implementations must download only a versioned, verified dataset. They must
 * never expose remote/LLM prose directly to the UI.
 */
interface LegalContentSyncRepository {
    suspend fun sync(): LegalContentSyncResult
    suspend fun currentVersion(): LegalContentVersion?
}

sealed interface LegalContentSyncResult {
    data class Updated(val version: LegalContentVersion) : LegalContentSyncResult
    data class AlreadyCurrent(val version: LegalContentVersion?) : LegalContentSyncResult
    data class Failed(val reason: SyncFailureReason) : LegalContentSyncResult
}

enum class SyncFailureReason {
    NETWORK_UNAVAILABLE,
    INVALID_MANIFEST,
    INVALID_JURISDICTION,
    INTEGRITY_CHECK_FAILED,
    UNSUPPORTED_SCHEMA,
    DOWNLOAD_FAILED,
    DATABASE_WRITE_FAILED,
    UNKNOWN,
}
