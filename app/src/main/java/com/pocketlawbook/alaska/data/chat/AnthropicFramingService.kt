package com.pocketlawbook.alaska.data.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

/**
 * Calls the Anthropic Messages API to select among on-device-retrieved
 * candidates and write one framing sentence. See [LanguageModelFramingService]
 * for the trust boundary this sits behind, and [FramingResponseParser] for how
 * its output is validated before anything from it is used.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * [apiKey] IS NOT SAFE TO SHIP IN A RELEASE BUILD. Any string bundled into an
 * APK - including a BuildConfig field - can be pulled back out with a decompiler
 * in minutes. This class exists to make the chat feature demonstrable during
 * development, the same way StubBillingRepository makes the paywall
 * demonstrable: it is not the production design. Shipping this for real needs
 * a thin server-side proxy that holds the key and forwards requests, so the key
 * never leaves a server you control - the same lesson BillingRepository's
 * comment already states about client-side trust.
 * ─────────────────────────────────────────────────────────────────────────────
 */
class AnthropicFramingService(
    private val apiKey: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
    private val model: String = "claude-haiku-4-5-20251001"
) : LanguageModelFramingService {

    override suspend fun frame(query: String, candidates: List<CandidatePassage>): FramingResult? {
        if (apiKey.isBlank() || candidates.isEmpty()) return null

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.anthropic.com/v1/messages")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(
                        requestBody(query, candidates).toString()
                            .toRequestBody("application/json".toMediaType())
                    )
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@withContext null
                    val body = response.body?.string() ?: return@withContext null
                    val text = JSONObject(body)
                        .getJSONArray("content")
                        .getJSONObject(0)
                        .getString("text")
                    FramingResponseParser.parse(text, candidates.map { it.violationKey }.toSet())
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun requestBody(query: String, candidates: List<CandidatePassage>): JSONObject {
        val candidateList = candidates.withIndex().joinToString("\n") { (i, candidate) ->
            "${i + 1}. [${candidate.violationKey}] ${candidate.description}"
        }
        val userContent = "User's situation: $query\n\nCandidate summaries:\n$candidateList"

        return JSONObject()
            .put("model", model)
            .put("max_tokens", 300)
            .put("system", SYSTEM_PROMPT)
            .put(
                "messages",
                JSONArray().put(JSONObject().put("role", "user").put("content", userContent))
            )
    }

    companion object {
        private val SYSTEM_PROMPT = """
            You help select which of a list of already-vetted legal-situation summaries answer a
            user's question, and you write exactly one plain-language framing sentence.

            Rules:
            - You may ONLY select IDs from the numbered candidate list you are given. Never invent
              an ID that is not in that list.
            - Your framing sentence must NOT contain any digit, a section symbol (§), or a case
              citation. It is framing only - never a legal claim, a statute number, or a citation.
            - If none of the candidates answer the question, return an empty selectedKeys list and
              an empty framing string.
            - Respond with ONLY this JSON object, nothing else, no markdown fences:
              {"framing": "<one sentence, or empty string>", "selectedKeys": ["KEY1", "KEY2"]}
        """.trimIndent()
    }
}
