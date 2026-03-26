package com.example.skinconsultform.domain.usecase

import com.example.skinconsultform.data.remote.*
import com.example.skinconsultform.domain.ai.*
import com.example.skinconsultform.domain.model.*
import com.example.skinconsultform.BuildConfig
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import javax.inject.*

@Singleton
class GetRecommendationsUseCase @Inject constructor(
    private val ruleEngine: RuleEngine,
    private val geminiPromptBuilder: GeminiPromptBuilder,
    private val geminiApiService: GeminiApiService
) {
    companion object {
        private const val TIMEOUT_MS = 15_000L
    }

    suspend operator fun invoke(
        form: ConsultationForm,
        consultationId: Long
    ): ConsultationResult {

        // Step 1 — always run rule engine (works offline)
        val ruleResult = ruleEngine.analyze(form)

        // Step 2 — try Gemini for narrative, fall back gracefully
        val narrative = tryGemini(form, ruleResult)

        return ConsultationResult(
            consultationId  = consultationId,
            clientName      = form.clientName,
            submittedAt     = System.currentTimeMillis(),
            recommendations = ruleResult.recommendations,
            aiNarrative     = narrative,
            ruleFlags       = ruleResult.flags
        )
    }

    private suspend fun tryGemini(
        form: ConsultationForm,
        ruleResult: RuleEngineResult
    ): String {
        return try {
            withTimeout(TIMEOUT_MS) {
                val prompt = geminiPromptBuilder.buildPrompt(form, ruleResult)
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    )
                )

                // ── Try gemini-2.0-flash first ────────────────────────────
                try {
                    android.util.Log.d("GEMINI", "Trying gemini-2.0-flash...")
                    val response = geminiApiService.generateContentFlash20(
                        apiKey  = BuildConfig.GEMINI_API_KEY,
                        request = request
                    )
                    response.candidates
                        ?.firstOrNull()
                        ?.content
                        ?.parts
                        ?.firstOrNull()
                        ?.text
                        ?.trim()
                        ?: buildFallbackNarrative(form, ruleResult)

                } catch (e: retrofit2.HttpException) {
                    if (e.code() == 429) {
                        // ── 429 — fall back to gemini-1.5-flash-8b ────────
                        android.util.Log.w("GEMINI", "2.0 quota exceeded — trying lite...")
                        try {
                            val fallbackResponse = geminiApiService.generateContentFlash15(
                                apiKey  = BuildConfig.GEMINI_API_KEY,
                                request = request
                            )
                            fallbackResponse.candidates
                                ?.firstOrNull()
                                ?.content
                                ?.parts
                                ?.firstOrNull()
                                ?.text
                                ?.trim()
                                ?: buildFallbackNarrative(form, ruleResult)
                        } catch (fallbackEx: Exception) {
                            android.util.Log.e("GEMINI", "lite also failed: ${fallbackEx.message}")
                            buildFallbackNarrative(form, ruleResult)
                        }
                    } else {
                        android.util.Log.e("GEMINI", "HTTP ${e.code()}: ${e.response()?.errorBody()?.string()}")
                        buildFallbackNarrative(form, ruleResult)
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            android.util.Log.e("GEMINI", "Timeout: ${e.message}")
            buildFallbackNarrative(form, ruleResult)
        } catch (e: Exception) {
            android.util.Log.e("GEMINI", "Error: ${e.message}")
            buildFallbackNarrative(form, ruleResult)
        }
    }

    // Offline fallback — rule-based narrative when Gemini is unavailable
    private fun buildFallbackNarrative(
        form: ConsultationForm,
        ruleResult: RuleEngineResult
    ): String {
        val firstName  = form.clientName.split(" ").firstOrNull() ?: form.clientName
        val concerns   = form.skinConcerns.take(2).joinToString(" and ")
        val topTreatment = ruleResult.recommendations
            .filter { !it.isContraindicated }
            .minByOrNull { it.priority }
            ?.treatmentName ?: "a personalised facial"

        val lifestyleNote = when {
            form.sleepHours < 6f && form.waterLiters < 1.5f ->
                "We also noticed that improving your sleep and daily water " +
                        "intake will significantly enhance your results."
            form.sleepHours < 6f ->
                "Improving your sleep quality will greatly support your " +
                        "skin's natural repair process."
            form.waterLiters < 1.5f ->
                "Increasing your daily water intake will help maintain your " +
                        "skin's hydration from within."
            form.highStress ->
                "Managing stress levels will also play an important role " +
                        "in keeping your skin balanced and clear."
            !form.wearsSpfDaily ->
                "We strongly recommend incorporating a daily SPF into your " +
                        "routine to protect and preserve your results."
            else -> "Our team is here to support you every step of the way."
        }

        return "Thank you, $firstName — based on your consultation, we can " +
                "see that your primary concerns around " +
                "${concerns.ifBlank { "your skin" }} are something we " +
                "specialise in addressing here at S'thetic. " +
                "We have carefully selected a set of treatments tailored " +
                "specifically to your skin profile, starting with " +
                "$topTreatment. $lifestyleNote"
    }
}