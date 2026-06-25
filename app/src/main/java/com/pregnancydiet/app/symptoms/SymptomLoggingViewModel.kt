package com.pregnancydiet.app.symptoms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.SymptomLogRepository
import com.pregnancydiet.app.firebase.FirestoreSymptomLogRepository
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.safety.RedFlagSymptomDetector
import com.pregnancydiet.app.safety.SymptomSafetyInput
import com.pregnancydiet.app.safety.SymptomSafetyResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SymptomLoggingViewModel(
    private val repository: SymptomLogRepository = FirestoreSymptomLogRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SymptomLoggingUiState())
    val uiState: StateFlow<SymptomLoggingUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = SymptomLoggingUiState(
                isLoading = false,
                errorMessage = "Sign in again to log symptoms.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val contextResult = repository.loadLoggingContext(uid)
            val historyResult = repository.loadRecentSymptomLogs(uid)
            val context = contextResult.getOrNull()
            _uiState.update { current ->
                current.copy(
                    isLoading = false,
                    pregnancyProfileId = context?.pregnancyProfileId,
                    pregnancyProgress = context?.progress,
                    history = historyResult.getOrNull().orEmpty(),
                    errorMessage = contextResult.exceptionOrNull()?.toUserFacingMessage()
                        ?: historyResult.exceptionOrNull()?.toUserFacingMessage(),
                ).withUpdatedDraftSafety()
            }
        }
    }

    fun updateDate(value: String) = updateForm { copy(date = value) }
    fun updateSymptomName(value: String) = updateForm { copy(symptomName = value) }
    fun updateSeverity(value: String) = updateForm { copy(severity = value.filter { it.isDigit() }.take(2)) }
    fun updateDuration(value: String) = updateForm { copy(duration = value) }
    fun updateNotes(value: String) = updateForm { copy(notes = value) }

    fun submit(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to log symptoms.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null, lastSavedSafetyResult = null) }
            val current = _uiState.value
            val validation = SymptomValidation.validate(current.form)
            val input = validation.getOrNull()
            if (input == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = validation.exceptionOrNull()?.message ?: "Check symptom fields and try again.",
                    )
                }
                return@launch
            }

            val safetyResult = RedFlagSymptomDetector.evaluate(
                SymptomSafetyInput(
                    name = input.symptom.name,
                    severity = input.symptom.severity,
                    duration = input.symptom.duration,
                    notes = input.symptom.notes,
                    pregnancyWeek = current.pregnancyProgress?.pregnancyWeek,
                ),
            )
            val log = SymptomLog(
                id = "",
                date = input.date.toString(),
                pregnancyProfileId = current.pregnancyProfileId,
                pregnancyWeek = current.pregnancyProgress?.pregnancyWeek,
                trimester = current.pregnancyProgress?.trimester,
                symptoms = listOf(input.symptom),
                urgentFlag = safetyResult.urgentFlag,
                urgentReasons = safetyResult.urgentReasons,
            )
            val saveResult = repository.saveSymptomLog(uid, log)
            val savedLog = saveResult.getOrNull()

            _uiState.update { state ->
                if (savedLog == null) {
                    state.copy(
                        isSaving = false,
                        errorMessage = saveResult.exceptionOrNull()?.toUserFacingMessage()
                            ?: "Could not save symptom log. Please try again.",
                    )
                } else {
                    state.copy(
                        isSaving = false,
                        form = state.form.copy(
                            date = LocalDate.now().toString(),
                            severity = "5",
                            duration = "",
                            notes = "",
                        ),
                        draftSafetyResult = SymptomSafetyResult.Safe,
                        lastSavedSafetyResult = safetyResult,
                        history = listOf(savedLog) + state.history.filterNot { it.id == savedLog.id },
                        successMessage = "Symptom saved.",
                        errorMessage = null,
                    )
                }
            }
        }
    }

    private fun updateForm(reducer: SymptomFormState.() -> SymptomFormState) {
        _uiState.update { state ->
            state.copy(
                form = state.form.reducer(),
                successMessage = null,
                errorMessage = null,
                lastSavedSafetyResult = null,
            ).withUpdatedDraftSafety()
        }
    }

    private fun SymptomLoggingUiState.withUpdatedDraftSafety(): SymptomLoggingUiState {
        val severity = form.severity.toIntOrNull()
        val safetyResult = if (severity != null && severity in 1..10) {
            RedFlagSymptomDetector.evaluate(
                SymptomSafetyInput(
                    name = form.symptomName,
                    severity = severity,
                    duration = form.duration,
                    notes = form.notes,
                    pregnancyWeek = pregnancyProgress?.pregnancyWeek,
                ),
            )
        } else {
            SymptomSafetyResult.Safe
        }
        return copy(draftSafetyResult = safetyResult)
    }
}

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
