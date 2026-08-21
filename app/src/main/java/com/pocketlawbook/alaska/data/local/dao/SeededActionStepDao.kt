package com.pocketlawbook.alaska.data.local.dao

import com.pocketlawbook.alaska.data.local.VerifiedContentSeed
import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity

/**
 * In-memory [ActionStepDao] over [VerifiedContentSeed].
 *
 * This is the swap-in point for Room. The interface is what the pipeline and its
 * tests depend on, so replacing this with a Room-backed DAO changes nothing above
 * it. Reading from memory also means lookups work with the network off, which is
 * the behaviour the real implementation has to preserve.
 */
class SeededActionStepDao(
    private val entries: Map<String, ActionStepEntity> = VerifiedContentSeed.entries
) : ActionStepDao {

    override suspend fun getActionStepsByKey(violationKey: String): ActionStepEntity? =
        entries[violationKey]
}
