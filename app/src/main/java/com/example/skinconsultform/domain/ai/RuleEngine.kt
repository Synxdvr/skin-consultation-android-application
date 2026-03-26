package com.example.skinconsultform.domain.ai

import com.example.skinconsultform.domain.model.ConsultationForm
import com.example.skinconsultform.domain.model.RecommendationCategory
import com.example.skinconsultform.domain.model.TreatmentRecommendation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleEngine @Inject constructor() {

    fun analyze(form: ConsultationForm): RuleEngineResult {
        val recommendations = mutableListOf<TreatmentRecommendation>()
        val flags           = mutableListOf<String>()
        val contextNotes    = mutableListOf<String>()

        // ── Medical safety flags ──────────────────────────────────────
        val isPregnant      = form.isPregnantOrBreastfeeding
        val hasEpilepsy     = form.medicalConditions.contains("Epilepsy")
        val hasCancer       = form.medicalConditions.contains("Cancer")
        val hasRosacea      = form.medicalConditions.contains(
            "Skin conditions (eczema, psoriasis, rosacea)"
        )
        val hasDiabetes     = form.medicalConditions.contains("Diabetes")
        val hasHormonal     = form.medicalConditions.contains("Hormonal imbalance")
        val hasHeartCondition = form.medicalConditions
            .contains("Heart condition")

        if (isPregnant)        flags.add("PREGNANT_OR_BREASTFEEDING")
        if (hasEpilepsy)       flags.add("EPILEPSY")
        if (hasCancer)         flags.add("CANCER_HISTORY")
        if (hasDiabetes)       flags.add("DIABETES")
        if (hasHormonal)       flags.add("HORMONAL_IMBALANCE")
        if (hasHeartCondition) flags.add("HEART_CONDITION")

        // ── Skin concern scoring map ──────────────────────────────────
        val concerns = form.skinConcerns

        // Acne / Breakouts
        if (concerns.contains("Acne / Breakouts")) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Deep Cleansing Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Targets excess sebum and congested pores " +
                            "that contribute to breakouts.",
                    priority      = 1
                )
            )
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Chemical Peel (Salicylic Acid)",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Salicylic acid exfoliates inside the pore " +
                            "lining, reducing active acne and preventing " +
                            "future breakouts.",
                    priority      = 2,
                    isContraindicated = isPregnant,
                    contraindicationNote = if (isPregnant)
                        "Salicylic acid peels are not recommended during " +
                                "pregnancy. Please consult your OB-GYN first."
                    else ""
                )
            )
            if (form.smokes || form.highStress) {
                contextNotes.add(
                    "Smoking and high stress elevate cortisol and androgen " +
                            "levels which worsen acne. Lifestyle adjustments will " +
                            "significantly improve treatment results."
                )
            }
        }

        // Hyperpigmentation
        if (concerns.contains("Hyperpigmentation")) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Brightening Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Targets melanin overproduction with " +
                            "vitamin C and kojic acid actives.",
                    priority      = 1
                )
            )
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Laser Toning",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Low-fluence laser breaks down melanin " +
                            "deposits for a more even skin tone.",
                    priority      = 3,
                    isContraindicated = isPregnant || hasCancer,
                    contraindicationNote = when {
                        isPregnant -> "Laser treatments are not recommended " +
                                "during pregnancy."
                        hasCancer  -> "Please get clearance from your oncologist " +
                                "before laser treatments."
                        else       -> ""
                    }
                )
            )
            if (form.sunExposure && !form.wearsSpfDaily) {
                contextNotes.add(
                    "Daily broad-spectrum SPF 50+ is essential — unprotected " +
                            "sun exposure will reverse pigmentation treatments."
                )
            }
        }

        // Fine Lines / Wrinkles
        if (concerns.contains("Fine Lines / Wrinkles")) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Anti-Aging Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Combines peptide serums and massage " +
                            "techniques to stimulate collagen and reduce " +
                            "the appearance of fine lines.",
                    priority      = 1
                )
            )
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Microneedling",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Controlled micro-injuries trigger collagen " +
                            "remodelling, softening fine lines over a " +
                            "course of sessions.",
                    priority      = 2,
                    isContraindicated = isPregnant || hasDiabetes,
                    contraindicationNote = when {
                        isPregnant  -> "Microneedling is not recommended during " +
                                "pregnancy."
                        hasDiabetes -> "Impaired wound healing in diabetic clients " +
                                "requires specialist clearance first."
                        else        -> ""
                    }
                )
            )
            if (form.sleepHours < 6f) {
                contextNotes.add(
                    "Consistently getting less than 6 hours of sleep " +
                            "significantly accelerates collagen breakdown. Improving " +
                            "sleep quality will enhance anti-aging results."
                )
            }
        }

        // Dryness
        if (concerns.contains("Dryness")) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Hydrating Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Hyaluronic acid infusion replenishes the " +
                            "skin's moisture barrier for deep, lasting " +
                            "hydration.",
                    priority      = 1
                )
            )
            if (form.waterLiters < 1.5f) {
                contextNotes.add(
                    "Low water intake (${form.waterLiters}L/day) directly " +
                            "affects skin hydration. Aim for at least 2L daily to " +
                            "support treatment results."
                )
            }
        }

        // Oiliness
        if (concerns.contains("Oiliness")) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Oil Control Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Regulates sebaceous gland activity and " +
                            "minimises shine without stripping the skin.",
                    priority      = 1
                )
            )
        }

        // Sensitivity
        if (concerns.contains("Sensitivity") || hasRosacea) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Calming / Sensitive Skin Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Fragrance-free, hypoallergenic actives " +
                            "reduce inflammation and strengthen the " +
                            "skin barrier.",
                    priority      = 1
                )
            )
            flags.add("SENSITIVE_SKIN")
        }

        // Redness / Rosacea
        if (concerns.contains("Redness / Rosacea") || hasRosacea) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "LED Light Therapy (Red & Near-Infrared)",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Reduces vascular inflammation and calms " +
                            "rosacea flare-ups without heat or irritation.",
                    priority      = 2
                )
            )
        }

        // Uneven texture / Enlarged pores
        if (concerns.contains("Uneven texture") ||
            concerns.contains("Enlarged pores")
        ) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Microdermabrasion",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Physical exfoliation buffs away dead skin " +
                            "cells, visibly refining texture and minimising " +
                            "pore appearance.",
                    priority      = 2
                )
            )
        }

        // Scarring
        if (concerns.contains("Scarring")) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Fractional Laser Resurfacing",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Stimulates new skin growth to gradually " +
                            "flatten and fade both atrophic and " +
                            "hypertrophic scars.",
                    priority      = 2,
                    isContraindicated = isPregnant || hasCancer,
                    contraindicationNote = when {
                        isPregnant -> "Laser is not recommended during pregnancy."
                        hasCancer  -> "Oncologist clearance required before " +
                                "laser resurfacing."
                        else       -> ""
                    }
                )
            )
        }

        // ── Lifestyle advisory recommendations ────────────────────────
        if (form.smokes) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Antioxidant Infusion Facial",
                    category      = RecommendationCategory.LIFESTYLE_ADVISORY,
                    reason        = "Smoking depletes skin antioxidants and " +
                            "accelerates collagen breakdown. This facial " +
                            "replenishes vitamins C and E topically.",
                    priority      = 3
                )
            )
            contextNotes.add(
                "Smoking significantly reduces skin oxygenation and treatment " +
                        "efficacy. Results may take longer to become visible."
            )
        }

        if (!form.wearsSpfDaily) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Daily SPF 50+ (Product Recommendation)",
                    category      = RecommendationCategory.PRODUCT_SUGGESTION,
                    reason        = "UV damage is the leading cause of premature " +
                            "ageing and pigmentation. Daily SPF is the " +
                            "single most impactful skin habit.",
                    priority      = 1
                )
            )
        }

        if (form.sleepHours < 6f) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Overnight Recovery Serum",
                    category      = RecommendationCategory.PRODUCT_SUGGESTION,
                    reason        = "A retinol or peptide night serum maximises " +
                            "the limited skin repair window available " +
                            "during short sleep cycles.",
                    priority      = 3
                )
            )
        }

        if (form.highStress) {
            recommendations.add(
                TreatmentRecommendation(
                    treatmentName = "Relaxation / Aromatherapy Facial",
                    category      = RecommendationCategory.LIFESTYLE_ADVISORY,
                    reason        = "High cortisol levels trigger breakouts and " +
                            "accelerate ageing. This treatment addresses " +
                            "both skin and nervous system stress.",
                    priority      = 2
                )
            )
        }

        // ── Hormonal advisory ─────────────────────────────────────────
        if (hasHormonal) {
            contextNotes.add(
                "Hormonal imbalances can cause cyclical breakouts, " +
                        "pigmentation, and dryness. Treatment plans may need to " +
                        "be adjusted alongside any hormonal therapy."
            )
        }

        // ── Deduplicate, sort by category then reassign sequential priority ──
        val deduped = recommendations
            .distinctBy { it.treatmentName }
            .sortedWith(
                compareBy({ it.isContraindicated }, { it.priority })
            )

        // Reassign clean sequential numbers — 1, 2, 3, 4...
        // Active treatments first, contraindicated at the end
        val reindexed = deduped
            .filter { !it.isContraindicated }
            .mapIndexed { index, rec -> rec.copy(priority = index + 1) } +
                deduped
                    .filter { it.isContraindicated }
                    .mapIndexed { index, rec -> rec.copy(priority = index + 1) }

        return RuleEngineResult(
            recommendations = reindexed,
            flags           = flags,
            contextNotes    = contextNotes
        )
    }
}

data class RuleEngineResult(
    val recommendations: List<TreatmentRecommendation>,
    val flags: List<String>,
    val contextNotes: List<String>
)