package com.pocketlawbook.alaska.data.legal

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Records that the user accepted the legal terms, and which version.
 *
 * Storing the version is what makes acceptance meaningful: when
 * [LegalDocuments.VERSION] is bumped because the terms materially changed, a
 * previously-accepted user is asked again rather than silently treated as having
 * agreed to something they never saw.
 *
 * The timestamp exists because "when did this user accept which version" is the
 * question that gets asked if acceptance is ever disputed.
 */
interface ConsentRepository {
    val hasAcceptedCurrent: StateFlow<Boolean>
    fun accept()
    fun acceptedVersion(): Int
    fun acceptedAtMillis(): Long
}

class SharedPrefsConsentRepository(context: Context) : ConsentRepository {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _hasAcceptedCurrent = MutableStateFlow(
        prefs.getInt(KEY_VERSION, 0) >= LegalDocuments.VERSION
    )
    override val hasAcceptedCurrent: StateFlow<Boolean> = _hasAcceptedCurrent.asStateFlow()

    override fun accept() {
        prefs.edit()
            .putInt(KEY_VERSION, LegalDocuments.VERSION)
            .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
            .apply()
        _hasAcceptedCurrent.value = true
    }

    override fun acceptedVersion(): Int = prefs.getInt(KEY_VERSION, 0)

    override fun acceptedAtMillis(): Long = prefs.getLong(KEY_TIMESTAMP, 0L)

    private companion object {
        const val PREFS_NAME = "legal_consent"
        const val KEY_VERSION = "accepted_version"
        const val KEY_TIMESTAMP = "accepted_at"
    }
}
