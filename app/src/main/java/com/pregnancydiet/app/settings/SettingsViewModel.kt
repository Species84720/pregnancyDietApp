package com.pregnancydiet.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.auth.AuthRepository
import com.pregnancydiet.app.firebase.FirebaseAuthRepository
import com.pregnancydiet.app.data.SettingsRepository
import com.pregnancydiet.app.firebase.FirestoreSettingsRepository
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.PrivacySettings
import com.pregnancydiet.app.model.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository = FirestoreSettingsRepository(),
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = SettingsUiState(
                isLoading = false,
                errorMessage = "Sign in again to manage settings.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = settingsRepository.loadSettings(uid)
            val data = result.getOrNull()
            _uiState.value = SettingsUiState(
                isLoading = false,
                userProfile = data?.userProfile,
                pregnancyProfile = data?.pregnancyProfile,
                profileForm = data?.pregnancyProfile?.toSettingsFormState() ?: SettingsFormState(),
                privacySettings = data?.privacySettings ?: PrivacySettings(),
                errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
            )
        }
    }

    fun updateDateFoundOut(value: String) = updateForm { copy(dateFoundOut = value) }
    fun updateLastMenstrualPeriod(value: String) = updateForm { copy(lastMenstrualPeriod = value) }
    fun updateEstimatedDueDate(value: String) = updateForm { copy(estimatedDueDate = value) }
    fun updateDoctorConfirmedWeek(value: String) = updateForm { copy(doctorConfirmedWeek = value) }
    fun updatePregnancyType(value: PregnancyType) = updateForm { copy(pregnancyType = value) }
    fun updateHeightCm(value: String) = updateForm { copy(heightCm = value) }
    fun updatePrePregnancyWeight(value: String) = updateForm { copy(prePregnancyWeight = value) }
    fun updateCurrentWeight(value: String) = updateForm { copy(currentWeight = value) }
    fun updateWeightUnit(value: WeightUnit) = updateForm { copy(weightUnit = value) }
    fun updateAllergies(value: String) = updateForm { copy(allergies = value) }
    fun updateDietaryRestrictions(value: String) = updateForm { copy(dietaryRestrictions = value) }
    fun updateMedicalConditions(value: String) = updateForm { copy(medicalConditions = value) }

    fun updateAiProcessingAllowed(value: Boolean) {
        _uiState.update {
            it.copy(
                privacySettings = it.privacySettings.copy(aiProcessingAllowed = value),
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun savePregnancyProfile(uid: String) {
        val existingProfile = _uiState.value.pregnancyProfile
        if (uid.isBlank() || existingProfile == null) {
            _uiState.update { it.copy(errorMessage = "Complete onboarding before editing pregnancy settings.") }
            return
        }
        val validation = SettingsProfileValidation.validate(_uiState.value.profileForm, existingProfile)
        val updatedProfile = validation.getOrNull()
        if (updatedProfile == null) {
            _uiState.update { it.copy(errorMessage = validation.exceptionOrNull()?.toUserFacingMessage()) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingProfile = true, successMessage = null, errorMessage = null) }
            val result = settingsRepository.savePregnancyProfile(uid, updatedProfile)
            val savedProfile = result.getOrNull()
            _uiState.update { state ->
                state.copy(
                    isSavingProfile = false,
                    pregnancyProfile = savedProfile ?: state.pregnancyProfile,
                    profileForm = savedProfile?.toSettingsFormState() ?: state.profileForm,
                    successMessage = if (result.isSuccess) "Pregnancy settings saved." else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun savePrivacySettings(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to manage privacy settings.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPrivacy = true, successMessage = null, errorMessage = null) }
            val result = settingsRepository.savePrivacySettings(uid, _uiState.value.privacySettings)
            _uiState.update { state ->
                state.copy(
                    isSavingPrivacy = false,
                    privacySettings = result.getOrNull() ?: state.privacySettings,
                    successMessage = if (result.isSuccess) "Privacy settings saved." else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun deleteAccount(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again before deleting your account.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingAccount = true, successMessage = null, errorMessage = null) }
            val dataDeleteResult = settingsRepository.deleteUserData(uid)
            if (dataDeleteResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        errorMessage = dataDeleteResult.exceptionOrNull()?.toUserFacingMessage(),
                    )
                }
                return@launch
            }

            val authDeleteResult = authRepository.deleteCurrentUser()
            if (authDeleteResult.isSuccess) {
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        successMessage = "Account and user-scoped data deleted.",
                    )
                }
            } else {
                authRepository.signOut()
                _uiState.update {
                    it.copy(
                        isDeletingAccount = false,
                        errorMessage = "Your app data was deleted. Firebase account deletion may require a recent sign-in; sign in again and retry if the account remains.",
                    )
                }
            }
        }
    }

    private fun updateForm(reducer: SettingsFormState.() -> SettingsFormState) {
        _uiState.update {
            it.copy(
                profileForm = it.profileForm.reducer(),
                successMessage = null,
                errorMessage = null,
            )
        }
    }
}

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."