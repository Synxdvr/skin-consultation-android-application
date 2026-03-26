package com.example.skinconsultform.domain.model

data class TreatmentRecommendation(
    val treatmentName: String,
    val category: RecommendationCategory,
    val reason: String,
    val priority: Int,          // 1 = highest
    val isContraindicated: Boolean = false,
    val contraindicationNote: String = ""
)

enum class RecommendationCategory {
    FACIAL_TREATMENT,
    SKIN_CONCERN,
    LIFESTYLE_ADVISORY,
    PRODUCT_SUGGESTION,
    CAUTION
}

data class ConsultationResult(
    val consultationId: Long,
    val clientName: String,
    val submittedAt: Long,
    val recommendations: List<TreatmentRecommendation>,
    val aiNarrative: String,        // Gemini-generated summary paragraph
    val ruleFlags: List<String>     // internal flags like "CONTRAINDICATED_LASER"
)