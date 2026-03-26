package com.example.skinconsultform.domain.usecase

import com.example.skinconsultform.data.db.ConsultationEntity
import com.example.skinconsultform.data.repository.ConsultationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetConsultationsUseCase @Inject constructor(
    private val repository: ConsultationRepository
) {
    operator fun invoke(): Flow<List<ConsultationEntity>> =
        repository.getAllConsultations()
}