package com.pregnancydiet.app.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.ReportsRepository
import com.pregnancydiet.app.firebase.FirestoreReportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReportsViewModel(
    private val repository: ReportsRepository = FirestoreReportsRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        generateReport(uid)
    }

    fun updateStartDate(value: String) {
        _uiState.update { it.copy(startDateText = value, successMessage = null, errorMessage = null, exportText = null) }
    }

    fun updateEndDate(value: String) {
        _uiState.update { it.copy(endDateText = value, successMessage = null, errorMessage = null, exportText = null) }
    }

    fun generateReport(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "Sign in again to view reports.") }
            return
        }
        val dateRange = currentDateRangeOrSetError() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null, exportText = null) }
            val result = repository.loadReport(uid, dateRange)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    report = result.getOrNull() ?: state.report,
                    successMessage = if (result.isSuccess) "Report loaded for ${dateRange.label}." else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun prepareExport() {
        val report = _uiState.value.report
        if (report == null) {
            _uiState.update { it.copy(errorMessage = "Load a report before exporting.") }
            return
        }
        val exportText = ReportExportFormatter.format(report)
        _uiState.update {
            it.copy(
                exportText = exportText,
                successMessage = "Shareable report is ready.",
                errorMessage = null,
            )
        }
    }

    fun exportConsumed() {
        _uiState.update { it.copy(exportText = null) }
    }

    private fun currentDateRangeOrSetError(): ReportDateRange? {
        val validation = ReportDateRangeValidation.validate(
            startDateText = _uiState.value.startDateText,
            endDateText = _uiState.value.endDateText,
        )
        val dateRange = validation.getOrNull()
        if (dateRange == null) {
            _uiState.update {
                it.copy(errorMessage = validation.exceptionOrNull()?.message ?: "Check date range and try again.")
            }
        }
        return dateRange
    }
}

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
