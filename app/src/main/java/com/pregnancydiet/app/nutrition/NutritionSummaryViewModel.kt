package com.pregnancydiet.app.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.NutritionSummaryRepository
import com.pregnancydiet.app.firebase.FirestoreNutritionSummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class NutritionSummaryViewModel(
    private val repository: NutritionSummaryRepository = FirestoreNutritionSummaryRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(NutritionSummaryUiState())
    val uiState: StateFlow<NutritionSummaryUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = NutritionSummaryUiState(
                isLoading = false,
                errorMessage = "Sign in again to view nutrition summaries.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val date = _uiState.value.selectedDate.toLocalDateOrToday()
            val dailyResult = repository.loadDailySummary(uid, date)
            val weeklyResult = repository.loadWeeklyTrend(uid, date)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    dailySummary = dailyResult.getOrNull(),
                    weeklyTrend = weeklyResult.getOrNull() ?: state.weeklyTrend,
                    errorMessage = dailyResult.exceptionOrNull()?.toUserFacingMessage()
                        ?: weeklyResult.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
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

    fun generate(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to generate nutrition summaries.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null) }
            val date = _uiState.value.selectedDate.toLocalDateOrNull()
            if (date == null) {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = "Date must use YYYY-MM-DD.")
                }
                return@launch
            }

            val summaryResult = repository.generateAndSaveDailySummary(uid, date)
            val weeklyResult = repository.loadWeeklyTrend(uid, date)
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    dailySummary = summaryResult.getOrNull() ?: state.dailySummary,
                    weeklyTrend = weeklyResult.getOrNull() ?: state.weeklyTrend,
                    successMessage = if (summaryResult.isSuccess) "Nutrition summary generated from your logged meals." else null,
                    errorMessage = summaryResult.exceptionOrNull()?.toUserFacingMessage()
                        ?: weeklyResult.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }
}

private fun String.toLocalDateOrNull(): LocalDate? = runCatching { LocalDate.parse(this) }.getOrNull()

private fun String.toLocalDateOrToday(): LocalDate = toLocalDateOrNull() ?: LocalDate.now()

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
