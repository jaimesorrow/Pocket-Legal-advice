package com.pocketlawbook.alaska.data.local.dao

import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity

interface ActionStepDao {
    suspend fun getActionStepsByKey(violationKey: String): ActionStepEntity?
}
