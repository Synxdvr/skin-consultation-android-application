package com.example.skinconsultform.domain.ai

import com.example.skinconsultform.domain.model.*
import javax.inject.*

@Singleton
class GeminiPromptBuilder @Inject constructor() {

    fun buildPrompt(
        form: ConsultationForm,
        ruleResult: RuleEngineResult
    ): String {
        val concerns = form.skinConcerns.take(3).joinToString(", ")
        val topTreatments = ruleResult.recommendations
            .filter { !it.isContraindicated }
            .take(3)
            .joinToString(", ") { it.treatmentName }
        val lifestyle = buildString {
            if (form.smokes) append("smokes, ")
            if (form.highStress) append("high stress, ")
            if (!form.wearsSpfDaily) append("no daily SPF, ")
            if (form.sleepHours < 6f) append("poor sleep, ")
            if (form.waterLiters < 1.5f) append("low water intake")
        }.trimEnd(',', ' ')

        return """
        You are a professional skin consultant at S'thetic Spa.
        Write a warm 2-3 sentence personalised assessment for ${form.clientName.split(" ").first()}, age ${form.age}.
        Skin concerns: ${concerns.ifBlank { "general skincare" }}.
        Lifestyle notes: ${lifestyle.ifBlank { "none" }}.
        Top recommended treatments: $topTreatments.
        Address client by first name. Be warm and professional. No bullet points. No headers. Plain paragraph only.
            """.trimIndent()
    }
}