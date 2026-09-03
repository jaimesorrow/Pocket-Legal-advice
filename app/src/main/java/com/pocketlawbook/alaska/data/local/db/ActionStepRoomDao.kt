package com.pocketlawbook.alaska.data.local.db

import androidx.room.Dao
import androidx.room.Query

/**
 * Room-generated DAO for [ActionStepRow].
 * [com.pocketlawbook.alaska.data.local.dao.RoomActionStepDao] is what the rest
 * of the app depends on, and it's the only caller of this interface — it just
 * can't be `internal` itself, since it's a public constructor parameter type
 * on that public class.
 */
@Dao
interface ActionStepRoomDao {
    @Query("SELECT * FROM action_steps WHERE violation_key = :violationKey")
    suspend fun getByKey(violationKey: String): ActionStepRow?
}
