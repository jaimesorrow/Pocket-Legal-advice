package com.pocketlawbook.alaska.data.local.db

import androidx.room.TypeConverter
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction

/**
 * Room type converters for the [ActionStepRow] table.
 *
 * Action steps are joined with the ASCII unit separator (0x1F) rather than a
 * printable delimiter or JSON, since it cannot appear in transcribed legal text
 * and needs no escaping logic that could alter the stored strings — the
 * zero-hallucination pipeline requires DB-sourced strings to reach the UI
 * byte-for-byte, whitespace included.
 */
object Converters {

    private const val UNIT_SEPARATOR = "\u001F"

    @TypeConverter
    @JvmStatic
    fun fromActionStepsList(steps: List<String>): String = steps.joinToString(UNIT_SEPARATOR)

    @TypeConverter
    @JvmStatic
    fun toActionStepsList(value: String): List<String> =
        if (value.isEmpty()) emptyList() else value.split(UNIT_SEPARATOR)

    @TypeConverter
    @JvmStatic
    fun fromJurisdiction(jurisdiction: Jurisdiction): String = jurisdiction.name

    @TypeConverter
    @JvmStatic
    fun toJurisdiction(value: String): Jurisdiction = Jurisdiction.valueOf(value)
}
