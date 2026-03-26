package com.example.skinconsultform.domain.usecase

import com.example.skinconsultform.data.db.ConsultationEntity
import com.example.skinconsultform.data.repository.ConsultationRepository
import javax.inject.Inject

class GetConsultationByIdUseCase @Inject constructor(
    private val repository: ConsultationRepository
) {
    suspend operator fun invoke(id: Long): ConsultationEntity? =
        repository.getById(id)
}