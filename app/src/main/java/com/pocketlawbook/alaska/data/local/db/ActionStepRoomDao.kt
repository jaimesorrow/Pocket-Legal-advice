package com.pocketlawbook.alaska.data.local.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Room-generated DAO for [ActionStepRow]. Not exposed outside this package —
 * [com.pocketlawbook.alaska.data.local.dao.RoomActionStepDao] is what the rest
 * of the app depends on, and it's the only caller of this interface.
 */
@Dao
internal interface ActionStepRoomDao {
    @Query("SELECT * FROM action_steps WHERE violation_key = :violationKey")
    suspend fun getByKey(violationKey: String): ActionStepRow?
}
