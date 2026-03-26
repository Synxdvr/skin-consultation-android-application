package com.example.skinconsultform.ui.viewmodel

import android.content.Intent
import androidx.lifecycle.*
import com.example.skinconsultform.data.db.ConsultationEntity
import com.example.skinconsultform.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getConsultationsUseCase: GetConsultationsUseCase,
    private val getConsultationByIdUseCase: GetConsultationByIdUseCase,
    private val exportPdfUseCase: ExportPdfUseCase
) : ViewModel() {

    private val _consultations = MutableStateFlow<List<ConsultationEntity>>(emptyList())
    val consultations: StateFlow<List<ConsultationEntity>> = _consultations.asStateFlow()

    private val _selectedConsultation = MutableStateFlow<ConsultationEntity?>(null)
    val selectedConsultation: StateFlow<ConsultationEntity?> =
        _selectedConsultation.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        loadConsultations()
    }

    private fun loadConsultations() {
        viewModelScope.launch {
            getConsultationsUseCase().collect { list ->
                _consultations.value = list
            }
        }
    }

    fun selectConsultation(id: Long) {
        viewModelScope.launch {
            _selectedConsultation.value = getConsultationByIdUseCase(id)
        }
    }

    fun authenticate(pin: String): Boolean {
        // Simple 4-digit PIN for MVP — replace with secure storage later
        val isValid = pin == "0202"
        _isAuthenticated.value = isValid
        return isValid
    }

    fun logout() {
        _isAuthenticated.value = false
        _selectedConsultation.value = null
    }

    private val _pdfShareIntent = MutableStateFlow<Intent?>(null)
    val pdfShareIntent: StateFlow<Intent?> = _pdfShareIntent.asStateFlow()

    fun exportPdf(entity: ConsultationEntity) {
        viewModelScope.launch {
            try {
                val intent = exportPdfUseCase(entity)
                _pdfShareIntent.value = intent
            } catch (e: Exception) {
                // handle error silently for MVP
            }
        }
    }

    fun clearPdfIntent() {
        _pdfShareIntent.value = null
    }

    fun exportPdfById(consultationId: Long) {
        viewModelScope.launch {
            val entity = getConsultationByIdUseCase(consultationId)
            entity?.let { exportPdfUseCase(it)
                .also { intent -> _pdfShareIntent.value = intent }
            }
        }
    }
}