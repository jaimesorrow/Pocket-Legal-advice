package com.pocketlegal.advice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Vetted, verified content for a single violation key. This is the single
 * source of truth for any text shown to the user — never populated from the
 * remote API's raw response.
 */
@Entity(tableName = "action_steps")
data class ActionStepEntity(
    @PrimaryKey val violationKey: String,
    val actionSteps: List<String>,
    val description: String
)

class ActionStepsConverter {
    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
