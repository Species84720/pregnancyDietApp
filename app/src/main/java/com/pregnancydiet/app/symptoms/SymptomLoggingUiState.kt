package com.pregnancydiet.app.symptoms

import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import com.pregnancydiet.app.safety.SymptomSafetyResult

data class SymptomLoggingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val form: SymptomFormState = SymptomFormState(),
    val pregnancyProfileId: String? = null,
    val pregnancyProgress: PregnancyProgress? = null,
    val draftSafetyResult: SymptomSafetyResult = SymptomSafetyResult.Safe,
    val lastSavedSafetyResult: SymptomSafetyResult? = null,
    val history: List<SymptomLog> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
