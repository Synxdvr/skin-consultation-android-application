package com.example.skinconsultform.ui.viewmodel

import androidx.lifecycle.*
import com.example.skinconsultform.domain.model.*
import com.example.skinconsultform.domain.usecase.*
import com.example.skinconsultform.domain.validation.FormValidators
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    data class Success(val consultationId: Long, val result: ConsultationResult) : SubmitState()
    data class Error(val message: String) : SubmitState()
}

@HiltViewModel
class ConsultationViewModel @Inject constructor(
    private val submitConsultationUseCase: SubmitConsultationUseCase,
    private val getRecommendationsUseCase: GetRecommendationsUseCase
) : ViewModel() {

    // Form state — single source of truth for all 5 steps
    private val _form = MutableStateFlow(ConsultationForm())
    val form: StateFlow<ConsultationForm> = _form.asStateFlow()

    // Current step (1–5)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // Submission state
    private val _submitState = MutableStateFlow<SubmitState>(SubmitState.Idle)
    val submitState: StateFlow<SubmitState> = _submitState.asStateFlow()

    // ── Reactive can-proceed — re-evaluates on every form/step change ──
    val canProceed: StateFlow<Boolean> = combine(
        _form,
        _currentStep
    ) { form, step ->
        when (step) {
            1 -> FormValidators.isStep1Valid(
                name        = form.clientName,
                dateOfBirth = form.dateOfBirth,
                phone       = form.phone,
                email       = form.email
            )
            5 -> form.consentAccurate &&
                    form.consentNotMedical &&
                    form.consentTreatment &&
                    form.signatureBase64.isNotBlank()
            else -> true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    // ── Step navigation ──────────────────────────────────────────────

    fun nextStep() {
        if (_currentStep.value < 5) {
            _currentStep.update { it + 1 }
        }
    }

    fun previousStep() {
        if (_currentStep.value > 1) {
            _currentStep.update { it - 1 }
        }
    }

    fun canProceedFromCurrentStep(): Boolean {
        return when (_currentStep.value) {
            1 -> _form.value.isStep1Valid()
            5 -> _form.value.isStep5Valid()
            else -> true  // Steps 2–4 are optional / no hard required fields
        }
    }

    fun updateStep5(
        consentAccurate: Boolean? = null,
        consentNotMedical: Boolean? = null,
        consentTreatment: Boolean? = null,
        signatureBase64: String? = null,
        consultantName: String? = null
    ) {
        _form.update { f ->
            f.copy(
                consentAccurate = consentAccurate ?: f.consentAccurate,
                consentNotMedical = consentNotMedical ?: f.consentNotMedical,
                consentTreatment = consentTreatment ?: f.consentTreatment,
                signatureBase64 = signatureBase64 ?: f.signatureBase64,
                consultantName = consultantName ?: f.consultantName
            )
        }
    }

    // ── Submission ───────────────────────────────────────────────────

    fun submitForm(result: ConsultationResult) {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                val id = submitConsultationUseCase(_form.value, result)
                _submitState.value = SubmitState.Success(id, result)
            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(
                    e.message ?: "Submission failed"
                )
            }
        }
    }

    fun resetForm() {
        _form.value = ConsultationForm()
        _currentStep.value = 1
        _submitState.value = SubmitState.Idle
    }

    // Generic field router for steps 1–4 (used by host screen)
    fun updateStep1Field(field: String, value: Any) {
        _form.update { f ->
            when (field) {
                "clientName"   -> f.copy(clientName = value as String)
                "dateOfBirth"  -> f.copy(dateOfBirth = value as String)
                "age"          -> f.copy(age = value as Int)
                "gender"       -> f.copy(gender = value as String)
                "phone"        -> f.copy(phone = value as String)
                "email"        -> f.copy(email = value as String)
                "address"      -> f.copy(address = value as String)
                "occupation"   -> f.copy(occupation = value as String)
                "source"       -> f.copy(source = value as String)
                else           -> f
            }
        }
    }

    fun updateStep2Field(field: String, value: Any) {
        _form.update { f ->
            when (field) {
                "medicalConditions"        -> f.copy(medicalConditions = @Suppress("UNCHECKED_CAST")(value as List<String>))
                "cancerDetails"            -> f.copy(cancerDetails = value as String)
                "allergyDetails"           -> f.copy(allergyDetails = value as String)
                "isPregnantOrBreastfeeding"-> f.copy(isPregnantOrBreastfeeding = value as Boolean)
                "medications"              -> f.copy(medications = value as String)
                else                       -> f
            }
        }
    }

    fun updateStep3Field(field: String, value: Any) {
        _form.update { f ->
            when (field) {
                "skinConcerns"           -> f.copy(skinConcerns = @Suppress("UNCHECKED_CAST")(value as List<String>))
                "diagnosedSkinCondition" -> f.copy(diagnosedSkinCondition = value as String)
                "previousTreatments"     -> f.copy(previousTreatments = @Suppress("UNCHECKED_CAST")(value as List<String>))
                "lastTreatmentDate"      -> f.copy(lastTreatmentDate = value as String)
                "cleanser"               -> f.copy(cleanser = value as String)
                "moisturizer"            -> f.copy(moisturizer = value as String)
                "spfProduct"             -> f.copy(spfProduct = value as String)
                "otherProducts"          -> f.copy(otherProducts = value as String)
                else                     -> f
            }
        }
    }

    fun updateStep4Field(field: String, value: Any) {
        _form.update { f ->
            when (field) {
                "smokes"        -> f.copy(smokes = value as Boolean)
                "drinksAlcohol" -> f.copy(drinksAlcohol = value as Boolean)
                "sleepHours"    -> f.copy(sleepHours = value as Float)
                "waterLiters"   -> f.copy(waterLiters = value as Float)
                "highStress"    -> f.copy(highStress = value as Boolean)
                "sunExposure"   -> f.copy(sunExposure = value as Boolean)
                "wearsSpfDaily" -> f.copy(wearsSpfDaily = value as Boolean)
                else            -> f
            }
        }
    }

    fun triggerAiRecommendations() {
        viewModelScope.launch {
            _submitState.value = SubmitState.Loading
            try {
                // Step 1 — get AI recommendations
                val tempId = System.currentTimeMillis()
                val result = getRecommendationsUseCase(
                    form  = _form.value,
                    consultationId = tempId
                )

                // Step 2 — save to Room with results attached
                val savedId = submitConsultationUseCase(
                    form   = _form.value,
                    result = result
                )

                // Step 3 — navigate to results
                _submitState.value = SubmitState.Success(
                    consultationId = savedId,
                    result         = result.copy(consultationId = savedId)
                )

            } catch (e: Exception) {
                _submitState.value = SubmitState.Error(
                    e.message ?: "Something went wrong. Please try again."
                )
            }
        }
    }
}