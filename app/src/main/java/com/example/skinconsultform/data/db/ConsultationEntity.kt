package com.example.skinconsultform.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Step 1 — Personal info
    val clientName: String,
    val dateOfBirth: String,          // ISO: "YYYY-MM-DD"
    val age: Int,
    val gender: String,
    val phone: String,
    val email: String,
    val address: String,
    val occupation: String,
    val source: String,

    // Step 2 — Medical (stored as JSON strings for list fields)
    val medicalConditions: String,    // JSON array e.g. ["Diabetes","Epilepsy"]
    val cancerDetails: String,
    val allergyDetails: String,
    val isPregnantOrBreastfeeding: Boolean,
    val medications: String,          // free text list

    // Step 3 — Skin history
    val skinConcerns: String,         // JSON array
    val diagnosedSkinCondition: String,
    val previousTreatments: String,   // JSON array
    val lastTreatmentDate: String,
    val cleanser: String,
    val moisturizer: String,
    val spfProduct: String,
    val otherProducts: String,

    // Step 4 — Lifestyle
    val smokes: Boolean,
    val drinksAlcohol: Boolean,
    val sleepHours: Float,            // slider value
    val waterLiters: Float,           // slider value
    val highStress: Boolean,
    val sunExposure: Boolean,
    val wearsSpfDaily: Boolean,

    // Step 5 — Consent
    val consentAccurate: Boolean,
    val consentNotMedical: Boolean,
    val consentTreatment: Boolean,
    val signatureBase64: String,      // PNG encoded as base64
    val consultantName: String,
    val submittedAt: Long,            // epoch milliseconds

    // AI results (stored after generation)
    val aiRecommendations: String,    // JSON — populated post-submit
    val ruleBasedFlags: String,        // JSON — internal scoring flags
    val aiNarrative: String = "",
)