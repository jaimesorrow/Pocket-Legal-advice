package com.pocketlawbook.alaska.data.local.entity

/**
 * Vetted, verified content for a single violation key. This is the single
 * source of truth for any text shown to the user — never populated from the
 * remote API's raw response.
 */
data class ActionStepEntity(
    val violationKey: String,
    val actionSteps: List<String>,
    val description: String
)
