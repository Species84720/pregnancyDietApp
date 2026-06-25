package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress

data class MealLoggingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val form: MealFormState = MealFormState(),
    val pregnancyProfileId: String? = null,
    val pregnancyProgress: PregnancyProgress? = null,
    val mealsForDate: List<MealLog> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
