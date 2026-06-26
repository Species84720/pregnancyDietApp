package com.pregnancydiet.app.reports

data class ReportsUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val startDateText: String = ReportDateRange.default().startDate.toString(),
    val endDateText: String = ReportDateRange.default().endDate.toString(),
    val report: GynecologistReport? = null,
    val exportText: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
