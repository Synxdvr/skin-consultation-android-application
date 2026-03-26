package com.example.skinconsultform.domain.usecase

import com.example.skinconsultform.data.repository.ConsultationRepository
import com.example.skinconsultform.domain.model.*
import javax.inject.Inject

class SubmitConsultationUseCase @Inject constructor(
    private val repository: ConsultationRepository
) {
    suspend operator fun invoke(
        form: ConsultationForm,
        result: ConsultationResult? = null
    ): Long {
        return repository.saveConsultation(form, result)
    }
}