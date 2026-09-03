package com.pocketlawbook.alaska.data.repository

import com.pocketlawbook.alaska.data.chat.CandidatePassage
import com.pocketlawbook.alaska.data.chat.LanguageModelFramingService
import com.pocketlawbook.alaska.data.local.dao.ActionStepDao
import com.pocketlawbook.alaska.data.remote.api.LegalApiService
import com.pocketlawbook.alaska.ui.model.ChatAnswer
import com.pocketlawbook.alaska.ui.model.VerifiedActionStep

/**
 * The chat pipeline described in docs/screen-map.html: retrieval and corpus
 * lookup happen on-device and never fail closed-for-content (they're the same
 * [LegalApiService] and [ActionStepDao] the analysis screen already uses); the
 * language model is an optional second pass that may narrow the candidates and
 * add one framing sentence, never author legal text.
 *
 * When [framingService] is unavailable, fails, or its response doesn't pass
 * validation, every retrieved candidate is returned unfiltered with no framing
 * - the same answer the app would give with no network at all. A working model
 * call can only ever narrow [ChatAnswer.matches] to a subset of what retrieval
 * already found and vetted; it can never add anything retrieval didn't already
 * surface.
 */
class ChatRepository(
    private val retriever: LegalApiService,
    private val actionStepDao: ActionStepDao,
    private val framingService: LanguageModelFramingService
) {
    suspend fun ask(query: String): ChatAnswer {
        val candidateKeys = retriever.analyzeLegalSituation(query).violations.map { it.key }
        val candidates = candidateKeys.mapNotNull { key ->
            actionStepDao.getActionStepsByKey(key)?.let { entity -> key to entity }
        }

        if (candidates.isEmpty()) {
            return ChatAnswer(framingSentence = null, matches = emptyList())
        }

        val framingResult = try {
            framingService.frame(query, candidates.map { (key, entity) -> CandidatePassage(key, entity.description) })
        } catch (e: Exception) {
            null
        }

        val (keptKeys, framingSentence) = if (framingResult != null && framingResult.selectedKeys.isNotEmpty()) {
            framingResult.selectedKeys.toSet() to framingResult.framingSentence
        } else {
            candidates.map { it.first }.toSet() to null
        }

        val matches = candidates
            .filter { (key, _) -> key in keptKeys }
            .map { (key, entity) ->
                VerifiedActionStep(violationKey = key, steps = entity.actionSteps, description = entity.description)
            }

        return ChatAnswer(framingSentence = framingSentence, matches = matches)
    }
}
