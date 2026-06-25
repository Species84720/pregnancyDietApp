package com.pregnancydiet.app.onboarding

import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyProgress

data class OnboardingFormState(
    val dateFoundOut: String = "",
    val lastMenstrualPeriod: String = "",
    val estimatedDueDate: String = "",
    val doctorConfirmedWeek: String = "",
    val pregnancyType: PregnancyType = PregnancyType.Unknown,
    val heightCm: String = "",
    val prePregnancyWeight: String = "",
    val currentWeight: String = "",
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val allergies: String = "",
    val dietaryRestrictions: String = "",
    val medicalConditions: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val progress: PregnancyProgress? = null,
    val completed: Boolean = false,
)