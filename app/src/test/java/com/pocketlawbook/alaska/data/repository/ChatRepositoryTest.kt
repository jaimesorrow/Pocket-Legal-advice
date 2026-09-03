package com.pocketlawbook.alaska.data.repository

import com.pocketlawbook.alaska.data.chat.CandidatePassage
import com.pocketlawbook.alaska.data.chat.FramingResult
import com.pocketlawbook.alaska.data.chat.LanguageModelFramingService
import com.pocketlawbook.alaska.data.local.dao.ActionStepDao
import com.pocketlawbook.alaska.data.local.entity.ActionStepEntity
import com.pocketlawbook.alaska.data.local.entity.Jurisdiction
import com.pocketlawbook.alaska.data.remote.api.LegalApiService
import com.pocketlawbook.alaska.data.remote.model.LegalViolationApiResponse
import com.pocketlawbook.alaska.data.remote.model.ViolationDetail
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

private const val KEY_A = "VIOLATION_A"
private const val KEY_B = "VIOLATION_B"

private val entityA = ActionStepEntity(
    violationKey = KEY_A,
    jurisdiction = Jurisdiction.ALASKA,
    description = "Verified description A",
    actionSteps = listOf("Step A1", "Step A2")
)

private val entityB = ActionStepEntity(
    violationKey = KEY_B,
    jurisdiction = Jurisdiction.FEDERAL,
    description = "Verified description B",
    actionSteps = listOf("Step B1")
)

@RunWith(JUnit4::class)
class ChatRepositoryTest {

    private val retriever = mockk<LegalApiService>()
    private val dao = mockk<ActionStepDao>()
    private val framingService = mockk<LanguageModelFramingService>()

    private val repository = ChatRepository(retriever, dao, framingService)

    // =========================================================================
    // 1. No retrieved candidates: framing service is never called
    // =========================================================================

    @Test
    fun `no retrieved candidates returns an empty answer without calling the framing service`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns LegalViolationApiResponse(emptyList())

        val answer = repository.ask("unrelated question")

        assertTrue(answer.matches.isEmpty())
        assertNull(answer.framingSentence)
        coVerify(exactly = 0) { framingService.frame(any(), any()) }
    }

    // =========================================================================
    // 2. Framing service unavailable/fails: fall back to every retrieved match
    // =========================================================================

    @Test
    fun `a null framing result falls back to every retrieved candidate with no framing`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns
            LegalViolationApiResponse(listOf(ViolationDetail(KEY_A, "raw", "raw", "raw")))
        coEvery { dao.getActionStepsByKey(KEY_A) } returns entityA
        coEvery { framingService.frame(any(), any()) } returns null

        val answer = repository.ask("something happened")

        assertNull(answer.framingSentence)
        assertEquals(1, answer.matches.size)
        assertEquals(KEY_A, answer.matches[0].violationKey)
        assertEquals(entityA.description, answer.matches[0].description)
        assertEquals(entityA.actionSteps, answer.matches[0].steps)
    }

    @Test
    fun `an exception from the framing service falls back instead of propagating`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns
            LegalViolationApiResponse(listOf(ViolationDetail(KEY_A, "raw", "raw", "raw")))
        coEvery { dao.getActionStepsByKey(KEY_A) } returns entityA
        coEvery { framingService.frame(any(), any()) } throws RuntimeException("network down")

        val answer = repository.ask("something happened")

        assertNull(answer.framingSentence)
        assertEquals(1, answer.matches.size)
    }

    @Test
    fun `an empty selection from the framing service falls back to every retrieved candidate`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns LegalViolationApiResponse(
            listOf(ViolationDetail(KEY_A, "raw", "raw", "raw"), ViolationDetail(KEY_B, "raw", "raw", "raw"))
        )
        coEvery { dao.getActionStepsByKey(KEY_A) } returns entityA
        coEvery { dao.getActionStepsByKey(KEY_B) } returns entityB
        coEvery { framingService.frame(any(), any()) } returns FramingResult(framingSentence = null, selectedKeys = emptyList())

        val answer = repository.ask("something happened")

        assertEquals(2, answer.matches.size)
    }

    // =========================================================================
    // 3. A working model call narrows matches to its selection and carries the framing sentence
    // =========================================================================

    @Test
    fun `a valid model selection narrows matches to the selected subset`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns LegalViolationApiResponse(
            listOf(ViolationDetail(KEY_A, "raw", "raw", "raw"), ViolationDetail(KEY_B, "raw", "raw", "raw"))
        )
        coEvery { dao.getActionStepsByKey(KEY_A) } returns entityA
        coEvery { dao.getActionStepsByKey(KEY_B) } returns entityB
        coEvery { framingService.frame(any(), any()) } returns
            FramingResult(framingSentence = "This looks like a search issue.", selectedKeys = listOf(KEY_A))

        val answer = repository.ask("they searched my car")

        assertEquals("This looks like a search issue.", answer.framingSentence)
        assertEquals(1, answer.matches.size)
        assertEquals(KEY_A, answer.matches[0].violationKey)
    }

    // =========================================================================
    // 4. The model only ever sees candidates retrieval already vetted
    // =========================================================================

    @Test
    fun `the framing service is offered only candidates retrieval already found in the verified store`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns
            LegalViolationApiResponse(listOf(ViolationDetail(KEY_A, "raw", "raw", "raw")))
        coEvery { dao.getActionStepsByKey(KEY_A) } returns entityA
        coEvery { framingService.frame(any(), any()) } returns null

        repository.ask("something happened")

        coVerify {
            framingService.frame("something happened", listOf(CandidatePassage(KEY_A, entityA.description)))
        }
    }

    // =========================================================================
    // 5. A key with no verified DB entry never becomes a candidate
    // =========================================================================

    @Test
    fun `a key with no verified DB entry is dropped before the framing service ever sees it`() = runTest {
        coEvery { retriever.analyzeLegalSituation(any()) } returns
            LegalViolationApiResponse(listOf(ViolationDetail("VIOLATION_UNKNOWN", "raw", "raw", "raw")))
        coEvery { dao.getActionStepsByKey("VIOLATION_UNKNOWN") } returns null

        val answer = repository.ask("something happened")

        assertTrue(answer.matches.isEmpty())
        coVerify(exactly = 0) { framingService.frame(any(), any()) }
    }
}
