package com.pocketlegal.advice.data.local.dao

import com.pocketlegal.advice.data.local.entity.ActionStepEntity

interface ActionStepDao {
    suspend fun getActionStepsByKey(violationKey: String): ActionStepEntity?
}
