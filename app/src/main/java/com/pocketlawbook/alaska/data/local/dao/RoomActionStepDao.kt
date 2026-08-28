package com.pocketlawbook.alaska.data.local.dao

import com.pocketlawbook.alaska.data.local.db.ActionStepRoomDao
import com.pocketlawbook.alaska.data.local.db.ActionStepRow
import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity

/**
 * Room-backed [ActionStepDao]. Maps [ActionStepRow] (the on-disk shape) to
 * [ActionStepEntity] (the domain shape the rest of the app depends on) without
 * touching any of the row's string content, so DB-sourced text still reaches
 * the UI byte-for-byte.
 */
class RoomActionStepDao internal constructor(
    private val roomDao: ActionStepRoomDao
) : ActionStepDao {

    override suspend fun getActionStepsByKey(violationKey: String): ActionStepEntity? =
        roomDao.getByKey(violationKey)?.toDomain()

    private fun ActionStepRow.toDomain() = ActionStepEntity(
        violationKey = violationKey,
        actionSteps = actionSteps,
        description = description,
        jurisdiction = jurisdiction
    )
}
