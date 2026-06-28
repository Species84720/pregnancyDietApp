package com.pregnancydiet.app.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.AiSummaryRepository
import com.pregnancydiet.app.firebase.FirestoreAiSummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class AiSummaryViewModel(
    private val repository: AiSummaryRepository = FirestoreAiSummaryRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiSummaryUiState())
    val uiState: StateFlow<AiSummaryUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = AiSummaryUiState(
                isLoading = false,
                errorMessage = "Sign in again to generate AI summaries.",
            )
            return
        }
        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
    }

    fun updateSelectedDate(value: String) {
        _uiState.update {
            it.copy(
                selectedDate = value,
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun generateDailyInsight(uid: String) = generate(
        uid = uid,
        action = AiSummaryAction.DailyInsight,
        successMessage = "Daily insight ready and saved.",
    ) { userId, date -> repository.generateDailyInsight(userId, date) }

    fun generateSymptomGuidance(uid: String) = generate(
        uid = uid,
        action = AiSummaryAction.SymptomGuidance,
        successMessage = "Symptom guidance ready and saved.",
    ) { userId, date -> repository.generateSymptomGuidance(userId, date) }

    fun generateWeeklySummary(uid: String) = generate(
        uid = uid,
        action = AiSummaryAction.WeeklySummary,
        successMessage = "Weekly summary ready and saved.",
    ) { userId, date -> repository.generateWeeklySummary(userId, date) }

    private fun generate(
        uid: String,
        action: AiSummaryAction,
        successMessage: String,
        operation: suspend (String, LocalDate) -> Result<AiSummaryRecord>,
    ) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to generate AI summaries.") }
            return
        }
        val date = _uiState.value.selectedDate.toLocalDateOrNull()
        if (date == null) {
            _uiState.update { it.copy(errorMessage = "Date must use YYYY-MM-DD.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    activeAction = action,
                    successMessage = null,
                    errorMessage = null,
                )
            }
            val result = operation(uid, date)
            _uiState.update { state ->
                state.copy(
                    activeAction = null,
                    latestSummary = result.getOrNull() ?: state.latestSummary,
                    successMessage = if (result.isSuccess) successMessage else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
