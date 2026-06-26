package com.pregnancydiet.app.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.ReminderPreferencesRepository
import com.pregnancydiet.app.data.SupplementRepository
import com.pregnancydiet.app.firebase.FirestoreReminderPreferencesRepository
import com.pregnancydiet.app.firebase.FirestoreSupplementRepository
import com.pregnancydiet.app.model.ReminderPreferences
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReminderSettingsViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository: ReminderPreferencesRepository = FirestoreReminderPreferencesRepository()
    private val supplementRepository: SupplementRepository = FirestoreSupplementRepository()
    private val scheduler: ReminderScheduler = AndroidReminderScheduler(application.applicationContext)

    private val _uiState = MutableStateFlow(ReminderSettingsUiState())
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    fun load(
        uid: String,
        notificationPermissionGranted: Boolean,
    ) {
        if (uid.isBlank()) {
            _uiState.value = ReminderSettingsUiState(
                isLoading = false,
                notificationPermissionGranted = notificationPermissionGranted,
                errorMessage = "Sign in again to manage reminders.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    notificationPermissionGranted = notificationPermissionGranted,
                    errorMessage = null,
                )
            }
            scheduler.createNotificationChannel()
            val preferencesResult = repository.loadReminderPreferences(uid)
            val supplementsResult = supplementRepository.loadSupplementsForDate(uid)
            val supplements = supplementsResult.getOrNull().orEmpty()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    preferences = preferencesResult.getOrNull() ?: state.preferences,
                    activeSupplementCount = supplements.count { it.supplement.active },
                    notificationPermissionGranted = notificationPermissionGranted,
                    errorMessage = preferencesResult.exceptionOrNull()?.toUserFacingMessage()
                        ?: supplementsResult.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun updateNotificationPermission(granted: Boolean) {
        _uiState.update { it.copy(notificationPermissionGranted = granted, errorMessage = null) }
    }

    fun toggleSupplementReminders() = updatePreferences {
        copy(supplementRemindersEnabled = !supplementRemindersEnabled)
    }

    fun toggleMealReminders() = updatePreferences {
        copy(mealRemindersEnabled = !mealRemindersEnabled)
    }

    fun toggleSymptomCheckIn() = updatePreferences {
        copy(symptomCheckInEnabled = !symptomCheckInEnabled)
    }

    fun updateMealReminderTime(value: String) = updatePreferences {
        copy(mealReminderTime = value.filterTimeInput())
    }

    fun updateSymptomReminderTime(value: String) = updatePreferences {
        copy(symptomReminderTime = value.filterTimeInput())
    }

    fun disableAll(uid: String) {
        updatePreferences {
            copy(
                supplementRemindersEnabled = false,
                mealRemindersEnabled = false,
                symptomCheckInEnabled = false,
            )
        }
        save(uid)
    }

    fun save(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to manage reminders.") }
            return
        }
        val current = _uiState.value.preferences
        val validation = ReminderTimeValidation.validate(current)
        if (validation.isFailure) {
            _uiState.update { it.copy(errorMessage = validation.exceptionOrNull()?.message) }
            return
        }
        if (current.anyEnabled && !_uiState.value.notificationPermissionGranted) {
            _uiState.update {
                it.copy(errorMessage = "Allow notifications before enabling reminders, or turn reminders off.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null, scheduledMessage = null) }
            val saveResult = repository.saveReminderPreferences(uid, current)
            val savedPreferences = saveResult.getOrNull()
            val supplements = if (savedPreferences != null) {
                supplementRepository.loadSupplementsForDate(uid).getOrNull().orEmpty()
            } else {
                emptyList()
            }
            val scheduleResult = savedPreferences?.let { scheduler.schedule(it, supplements) }
            _uiState.update { state ->
                if (savedPreferences == null) {
                    state.copy(
                        isSaving = false,
                        errorMessage = saveResult.exceptionOrNull()?.toUserFacingMessage()
                            ?: "Could not save reminder preferences. Please try again.",
                    )
                } else {
                    state.copy(
                        isSaving = false,
                        preferences = savedPreferences,
                        activeSupplementCount = supplements.count { it.supplement.active },
                        successMessage = "Reminder preferences saved.",
                        scheduledMessage = scheduleResult?.userMessage,
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun updatePreferences(reducer: ReminderPreferences.() -> ReminderPreferences) {
        _uiState.update {
            it.copy(
                preferences = it.preferences.reducer(),
                successMessage = null,
                scheduledMessage = null,
                errorMessage = null,
            )
        }
    }
}

private fun String.filterTimeInput(): String = filter { it.isDigit() || it == ':' }.take(5)

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
