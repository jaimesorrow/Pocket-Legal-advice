package com.pocketlegal.advice.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.pocketlegal.advice.data.local.entity.ActionStepEntity

@Dao
interface ActionStepDao {
    @Query("SELECT * FROM action_steps WHERE violationKey = :violationKey")
    suspend fun getActionStepsByKey(violationKey: String): ActionStepEntity?
}
