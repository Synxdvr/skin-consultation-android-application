package com.example.skinconsultform.data.repository

import com.google.gson.Gson
import com.example.skinconsultform.data.db.*
import com.example.skinconsultform.domain.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.*

@Singleton
class ConsultationRepository @Inject constructor(
    private val dao: ConsultationDao,
    private val gson: Gson
) {
    // Save new consultation (returns generated ID)
    suspend fun saveConsultation(
        form: ConsultationForm,
        result: ConsultationResult? = null
    ): Long {
        val entity = ConsultationEntity(
            clientName = form.clientName,
            dateOfBirth = form.dateOfBirth,
            age = form.age,
            gender = form.gender,
            phone = "+63${form.phone}",
            email = form.email,
            address = form.address,
            occupation = form.occupation,
            source = form.source,
            medicalConditions = gson.toJson(form.medicalConditions),
            cancerDetails = form.cancerDetails,
            allergyDetails = form.allergyDetails,
            isPregnantOrBreastfeeding = form.isPregnantOrBreastfeeding,
            medications = form.medications,
            skinConcerns = gson.toJson(form.skinConcerns),
            diagnosedSkinCondition = form.diagnosedSkinCondition,
            previousTreatments = gson.toJson(form.previousTreatments),
            lastTreatmentDate = form.lastTreatmentDate,
            cleanser = form.cleanser,
            moisturizer = form.moisturizer,
            spfProduct = form.spfProduct,
            otherProducts = form.otherProducts,
            smokes = form.smokes,
            drinksAlcohol = form.drinksAlcohol,
            sleepHours = form.sleepHours,
            waterLiters = form.waterLiters,
            highStress = form.highStress,
            sunExposure = form.sunExposure,
            wearsSpfDaily = form.wearsSpfDaily,
            consentAccurate = form.consentAccurate,
            consentNotMedical = form.consentNotMedical,
            consentTreatment = form.consentTreatment,
            signatureBase64 = form.signatureBase64,
            consultantName = form.consultantName,
            submittedAt = System.currentTimeMillis(),
            aiRecommendations = gson.toJson(result?.recommendations ?: emptyList<Any>()),
            ruleBasedFlags = gson.toJson(result?.ruleFlags ?: emptyList<Any>()),
            aiNarrative = result?.aiNarrative ?: ""
        )
        return dao.insert(entity)
    }

    fun getAllConsultations(): Flow<List<ConsultationEntity>> =
        dao.getAllConsultations()

    suspend fun getById(id: Long): ConsultationEntity? =
        dao.getById(id)
}