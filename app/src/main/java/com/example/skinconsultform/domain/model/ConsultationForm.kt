package com.example.skinconsultform.domain.model

data class ConsultationForm(
    // Step 1
    val clientName: String = "",
    val dateOfBirth: String = "",
    val age: Int = 0,
    val gender: String = "",
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val occupation: String = "",
    val source: String = "",

    // Step 2
    val medicalConditions: List<String> = emptyList(),
    val cancerDetails: String = "",
    val allergyDetails: String = "",
    val isPregnantOrBreastfeeding: Boolean = false,
    val medications: String = "",

    // Step 3
    val skinConcerns: List<String> = emptyList(),
    val diagnosedSkinCondition: String = "",
    val previousTreatments: List<String> = emptyList(),
    val lastTreatmentDate: String = "",
    val cleanser: String = "",
    val moisturizer: String = "",
    val spfProduct: String = "",
    val otherProducts: String = "",

    // Step 4
    val smokes: Boolean = false,
    val drinksAlcohol: Boolean = false,
    val sleepHours: Float = 7f,
    val waterLiters: Float = 2f,
    val highStress: Boolean = false,
    val sunExposure: Boolean = false,
    val wearsSpfDaily: Boolean = false,

    // Step 5
    val consentAccurate: Boolean = false,
    val consentNotMedical: Boolean = false,
    val consentTreatment: Boolean = false,
    val signatureBase64: String = "",
    val consultantName: String = ""
)

// Validation helpers
fun ConsultationForm.isStep1Valid(): Boolean =
    clientName.isNotBlank() && dateOfBirth.isNotBlank() && phone.isNotBlank()

fun ConsultationForm.isStep5Valid(): Boolean =
    consentAccurate && consentNotMedical && consentTreatment && signatureBase64.isNotBlank()