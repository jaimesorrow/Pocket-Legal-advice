package com.pocketlawbook.alaska.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction

/**
 * Room-persisted row for a single violation key's verified content.
 *
 * This is the on-disk shape; [com.pocketlawbook.alaska.data.local.entity.ActionStepEntity]
 * is the domain shape the rest of the app depends on. Keeping them separate means
 * Room's storage details (column types, converters) never leak past
 * [RoomActionStepDao], which is the only place that maps between the two.
 */
@Entity(tableName = "action_steps")
data class ActionStepRow(
    @PrimaryKey
    @ColumnInfo(name = "violation_key")
    val violationKey: String,
    @ColumnInfo(name = "action_steps")
    val actionSteps: List<String>,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "jurisdiction")
    val jurisdiction: Jurisdiction
)
