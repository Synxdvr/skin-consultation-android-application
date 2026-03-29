package com.example.skinconsultform.domain.ai

import com.example.skinconsultform.domain.model.*
import javax.inject.*

@Singleton
class RuleEngine @Inject constructor() {

    fun analyze(form: ConsultationForm): RuleEngineResult {
        val recommendations = mutableListOf<TreatmentRecommendation>()
        val flags           = mutableListOf<String>()
        val contextNotes    = mutableListOf<String>()

        // ── Medical safety flags ──────────────────────────────────────
        val isPregnant        = form.isPregnantOrBreastfeeding
        val hasEpilepsy       = form.medicalConditions.contains("Epilepsy")
        val hasCancer         = form.medicalConditions.contains("Cancer")
        val hasRosacea        = form.medicalConditions.contains(
            "Skin conditions (eczema, psoriasis, rosacea)"
        )
        val hasDiabetes       = form.medicalConditions.contains("Diabetes")
        val hasHormonal       = form.medicalConditions.contains("Hormonal imbalance")
        val hasHeartCondition = form.medicalConditions.contains("Heart condition")

        if (isPregnant)        flags.add("PREGNANT_OR_BREASTFEEDING")
        if (hasEpilepsy)       flags.add("EPILEPSY")
        if (hasCancer)         flags.add("CANCER_HISTORY")
        if (hasDiabetes)       flags.add("DIABETES")
        if (hasHormonal)       flags.add("HORMONAL_IMBALANCE")
        if (hasHeartCondition) flags.add("HEART_CONDITION")

        val concerns = form.skinConcerns

        // ── Fine Lines / Wrinkles ─────────────────────────────────────
        if (concerns.contains("Fine Lines / Wrinkles")) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Viva Glow Facial",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "A gentle red light treatment that boosts collagen, " +
                        "reduces inflammation, and leaves skin smooth and radiant.",
                priority      = 1
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Oxygen Renew and Rewind",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Hydrates and plumps skin with oxygen and serums, " +
                        "reducing fine lines for a fresh, youthful glow.",
                priority      = 2
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Skin Revive Facial (Radio Frequency)",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses gentle RF energy to boost collagen, tighten skin, " +
                        "and reduce fine lines for a smoother, firmer look.",
                priority      = 3,
                isContraindicated = hasHeartCondition || hasEpilepsy,
                contraindicationNote = when {
                    hasHeartCondition -> "RF treatments are not recommended for clients " +
                            "with heart conditions."
                    hasEpilepsy       -> "RF treatments are not recommended for clients " +
                            "with epilepsy."
                    else              -> ""
                }
            ))
            if (form.sleepHours < 6f) {
                contextNotes.add(
                    "Getting less than 6 hours of sleep accelerates collagen breakdown. " +
                            "Improving sleep quality will significantly enhance anti-aging results."
                )
            }
        }

        // ── Hyperpigmentation / Uneven skin tone / Post acne marks ────
        if (concerns.contains("Hyperpigmentation") ||
            concerns.contains("Uneven texture")
        ) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Pearl White Facial",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Green LED light therapy brightens and evens skin " +
                        "for a radiant, luminous complexion.",
                priority      = 1
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Crystal Clear Whitening Facial",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses diode laser to target pigmentation for a brighter, " +
                        "smoother, more even complexion.",
                priority      = 2,
                isContraindicated = isPregnant || hasCancer,
                contraindicationNote = when {
                    isPregnant -> "Laser treatments are not recommended during pregnancy."
                    hasCancer  -> "Please get clearance from your oncologist before " +
                            "laser treatments."
                    else       -> ""
                }
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Purelite Glow Facial (Pico Laser)",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses Pico Laser to target pigmentation and uneven tone " +
                        "for clearer, smoother, radiant skin.",
                priority      = 3,
                isContraindicated = isPregnant || hasCancer,
                contraindicationNote = when {
                    isPregnant -> "Laser treatments are not recommended during pregnancy."
                    hasCancer  -> "Oncologist clearance required before laser treatments."
                    else       -> ""
                }
            ))
            if (form.sunExposure && !form.wearsSpfDaily) {
                contextNotes.add(
                    "Daily broad-spectrum SPF 50+ is essential — unprotected sun exposure " +
                            "will reverse pigmentation treatments."
                )
            }
        }

        // ── Acne / Breakouts / Oiliness ───────────────────────────────
        if (concerns.contains("Acne / Breakouts") ||
            concerns.contains("Oiliness")
        ) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Acne Clear Essence Facial",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses high-frequency to kill bacteria, reduce redness, " +
                        "and clear acne for smooth, refreshed skin.",
                priority      = 1
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Blue Light Acne Therapy",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Clears acne by targeting bacteria and inflammation, " +
                        "preventing breakouts with a gentle, chemical-free treatment.",
                priority      = 2
            ))
            if (concerns.contains("Acne / Breakouts") &&
                concerns.contains("Hyperpigmentation")
            ) {
                recommendations.add(TreatmentRecommendation(
                    treatmentName = "BB Glow Facial",
                    category      = RecommendationCategory.FACIAL_TREATMENT,
                    reason        = "Uses microneedling with BB serum to even skin tone, " +
                            "brighten complexion, and address post-acne marks.",
                    priority      = 3,
                    isContraindicated = isPregnant || hasDiabetes,
                    contraindicationNote = when {
                        isPregnant  -> "Microneedling is not recommended during pregnancy."
                        hasDiabetes -> "Impaired wound healing requires specialist clearance."
                        else        -> ""
                    }
                ))
            }
            if (form.smokes || form.highStress) {
                contextNotes.add(
                    "Smoking and high stress elevate cortisol levels which worsen acne. " +
                            "Lifestyle adjustments will significantly improve treatment results."
                )
            }
        }

        // ── Scarring / Enlarged pores ─────────────────────────────────
        if (concerns.contains("Scarring") ||
            concerns.contains("Enlarged pores")
        ) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Dermapen (Collagen Remodeling)",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Advanced microneedling that stimulates collagen and elastin, " +
                        "boosting cell renewal for smoother, firmer, radiant skin.",
                priority      = 1,
                isContraindicated = isPregnant || hasDiabetes,
                contraindicationNote = when {
                    isPregnant  -> "Microneedling is not recommended during pregnancy."
                    hasDiabetes -> "Impaired wound healing in diabetic clients requires " +
                            "specialist clearance first."
                    else        -> ""
                }
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Purelite Glow Facial (Pico Laser)",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses Pico Laser to target scarring and enlarged pores " +
                        "for clearer, smoother, radiant skin.",
                priority      = 2,
                isContraindicated = isPregnant || hasCancer,
                contraindicationNote = when {
                    isPregnant -> "Laser treatments are not recommended during pregnancy."
                    hasCancer  -> "Oncologist clearance required before laser treatments."
                    else       -> ""
                }
            ))
        }

        // ── Dryness / Sensitivity / Redness / Rosacea ────────────────
        if (concerns.contains("Dryness") ||
            concerns.contains("Sensitivity") ||
            concerns.contains("Redness / Rosacea") ||
            hasRosacea
        ) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Oxygen Renew and Rewind",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Hydrates and plumps sensitive skin with oxygen and serums, " +
                        "calming redness and restoring moisture balance.",
                priority      = 1
            ))
            flags.add("SENSITIVE_SKIN")
            if (form.waterLiters < 1.5f) {
                contextNotes.add(
                    "Low water intake (${form.waterLiters}L/day) directly affects skin " +
                            "hydration. Aim for at least 2L daily to support treatment results."
                )
            }
        }

        // ── Warts ─────────────────────────────────────────────────────
        if (concerns.contains("Warts")) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Warts Removal",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses an electrocautery machine to safely and effectively " +
                        "eliminate warts with precision and minimal downtime.",
                priority      = 1,
                isContraindicated = hasDiabetes,
                contraindicationNote = if (hasDiabetes)
                    "Electrocautery on diabetic clients requires specialist clearance " +
                            "due to impaired wound healing." else ""
            ))
        }

        // ── Unwanted Hair ─────────────────────────────────────────────
        if (concerns.contains("Unwanted Hair")) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Diode Hair Removal",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses laser technology to permanently reduce unwanted hair " +
                        "for smooth, long-lasting results.",
                priority      = 1,
                isContraindicated = isPregnant,
                contraindicationNote = if (isPregnant)
                    "Laser hair removal is not recommended during pregnancy." else ""
            ))
        }

        // ── Dark Underarms ────────────────────────────────────────────
        if (concerns.contains("Dark Underarms")) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Diode Skin Whitening",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses laser technology to target pigmentation and brighten " +
                        "skin for a more even, radiant complexion.",
                priority      = 1,
                isContraindicated = isPregnant || hasCancer,
                contraindicationNote = when {
                    isPregnant -> "Laser treatments are not recommended during pregnancy."
                    hasCancer  -> "Oncologist clearance required before laser treatments."
                    else       -> ""
                }
            ))
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Picowhite",
                category      = RecommendationCategory.FACIAL_TREATMENT,
                reason        = "Uses Pico Laser to target pigmentation and uneven tone " +
                        "for clearer, smoother, radiant skin.",
                priority      = 2,
                isContraindicated = isPregnant || hasCancer,
                contraindicationNote = when {
                    isPregnant -> "Laser treatments are not recommended during pregnancy."
                    hasCancer  -> "Oncologist clearance required before laser treatments."
                    else       -> ""
                }
            ))
        }

        // ── Lifestyle advisory ────────────────────────────────────────
        if (form.smokes) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Viva Glow Facial",
                category      = RecommendationCategory.LIFESTYLE_ADVISORY,
                reason        = "Smoking depletes skin antioxidants and accelerates collagen " +
                        "breakdown. Red light therapy helps replenish skin vitality.",
                priority      = 4
            ))
            contextNotes.add(
                "Smoking significantly reduces skin oxygenation and treatment efficacy. " +
                        "Results may take longer to become visible."
            )
        }

        if (!form.wearsSpfDaily) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Daily SPF 50+ (Product Recommendation)",
                category      = RecommendationCategory.PRODUCT_SUGGESTION,
                reason        = "UV damage is the leading cause of premature ageing and " +
                        "pigmentation. Daily SPF is the single most impactful skin habit.",
                priority      = 1
            ))
        }

        if (form.highStress) {
            recommendations.add(TreatmentRecommendation(
                treatmentName = "Oxygen Renew and Rewind",
                category      = RecommendationCategory.LIFESTYLE_ADVISORY,
                reason        = "High cortisol levels trigger breakouts and accelerate ageing. " +
                        "This treatment addresses both skin and nervous system stress.",
                priority      = 3
            ))
        }

        if (hasHormonal) {
            contextNotes.add(
                "Hormonal imbalances can cause cyclical breakouts, pigmentation, and dryness. " +
                        "Treatment plans may need to be adjusted alongside any hormonal therapy."
            )
        }

        // ── Deduplicate, sort and reindex ─────────────────────────────
        val deduped = recommendations
            .distinctBy { it.treatmentName }
            .sortedWith(compareBy({ it.isContraindicated }, { it.priority }))

        val reindexed =
            deduped.filter { !it.isContraindicated }
                .mapIndexed { index, rec -> rec.copy(priority = index + 1) } +
                    deduped.filter { it.isContraindicated }
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