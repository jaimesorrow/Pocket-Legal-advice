package com.pocketlawbook.alaska.data.local.entity

/**
 * Vetted, verified content for a single violation key. This is the single
 * source of truth for any text shown to the user — never populated from the
 * remote API's raw response.
 *
 * [jurisdiction] defaults to [Jurisdiction.ALASKA] so existing construction
 * sites keep compiling, but real seeded content always sets it explicitly:
 * the user must always be able to see whether an answer rests on Alaska law
 * or federal law.
 */
data class ActionStepEntity(
    val violationKey: String,
    val actionSteps: List<String>,
    val description: String,
    val jurisdiction: Jurisdiction = Jurisdiction.ALASKA
)
