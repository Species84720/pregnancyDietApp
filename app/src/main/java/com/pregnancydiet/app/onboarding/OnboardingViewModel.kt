package com.pregnancydiet.app.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.PregnancyOnboardingRepository
import com.pregnancydiet.app.firebase.FirestorePregnancyOnboardingRepository
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyCalculator
import com.pregnancydiet.app.pregnancy.PregnancyDatingInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val repository: PregnancyOnboardingRepository = FirestorePregnancyOnboardingRepository(),
    private val pregnancyCalculator: PregnancyCalculator = PregnancyCalculator(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingFormState())
    val uiState: StateFlow<OnboardingFormState> = _uiState.asStateFlow()

    fun updateDateFoundOut(value: String) = update { copy(dateFoundOut = value, errorMessage = null) }
    fun updateLastMenstrualPeriod(value: String) = update { copy(lastMenstrualPeriod = value, errorMessage = null) }
    fun updateEstimatedDueDate(value: String) = update { copy(estimatedDueDate = value, errorMessage = null) }
    fun updateDoctorConfirmedWeek(value: String) = update { copy(doctorConfirmedWeek = value, errorMessage = null) }
    fun updatePregnancyType(value: PregnancyType) = update { copy(pregnancyType = value, errorMessage = null) }
    fun updateHeightCm(value: String) = update { copy(heightCm = value, errorMessage = null) }
    fun updatePrePregnancyWeight(value: String) = update { copy(prePregnancyWeight = value, errorMessage = null) }
    fun updateCurrentWeight(value: String) = update { copy(currentWeight = value, errorMessage = null) }
    fun updateWeightUnit(value: WeightUnit) = update { copy(weightUnit = value, errorMessage = null) }
    fun updateAllergies(value: String) = update { copy(allergies = value, errorMessage = null) }
    fun updateDietaryRestrictions(value: String) = update { copy(dietaryRestrictions = value, errorMessage = null) }
    fun updateMedicalConditions(value: String) = update { copy(medicalConditions = value, errorMessage = null) }

    fun submit(uid: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, completed = false) }
            val current = _uiState.value
            val validation = OnboardingValidation.validate(current)
            val input = validation.getOrNull()
            if (input == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = validation.exceptionOrNull()?.message ?: "Check onboarding fields and try again.",
                    )
                }
                return@launch
            }

            val progress = pregnancyCalculator.calculate(
                PregnancyDatingInput(
                    dateFoundOut = input.dateFoundOut,
                    lastMenstrualPeriod = input.lastMenstrualPeriod,
                    estimatedDueDate = input.estimatedDueDate,
                    doctorConfirmedWeek = input.doctorConfirmedWeek,
                ),
            )
            val profile = PregnancyProfile(
                id = "",
                dateFoundOut = input.dateFoundOut.toString(),
                lastMenstrualPeriod = input.lastMenstrualPeriod?.toString(),
                estimatedDueDate = input.estimatedDueDate?.toString(),
                doctorConfirmedWeek = input.doctorConfirmedWeek,
                pregnancyType = current.pregnancyType,
                heightCm = input.heightCm,
                prePregnancyWeightKg = input.prePregnancyWeightKg,
                currentWeightKg = input.currentWeightKg,
                weightUnit = current.weightUnit,
                allergies = input.allergies,
                dietaryRestrictions = input.dietaryRestrictions,
                medicalConditions = input.medicalConditions,
            )

            val saveResult = repository.saveOnboardingProfile(uid, input, profile, progress)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    progress = progress,
                    completed = saveResult.isSuccess,
                    errorMessage = saveResult.exceptionOrNull()?.message,
                )
            }
        }
    }

    fun markCompletionHandled() {
        _uiState.update { it.copy(completed = false) }
    }

    private fun update(reducer: OnboardingFormState.() -> OnboardingFormState) {
        _uiState.update { it.reducer() }
    }
}