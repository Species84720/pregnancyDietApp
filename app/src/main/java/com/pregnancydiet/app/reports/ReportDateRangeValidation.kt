package com.pregnancydiet.app.reports

import java.time.LocalDate

object ReportDateRangeValidation {
    fun validate(
        startDateText: String,
        endDateText: String,
    ): Result<ReportDateRange> = runCatching {
        val startDate = parseRequiredDate(startDateText, "Start date")
        val endDate = parseRequiredDate(endDateText, "End date")
        ReportDateRange(startDate = startDate, endDate = endDate)
    }

    private fun parseRequiredDate(value: String, label: String): LocalDate {
        require(value.isNotBlank()) { "$label is required. Use YYYY-MM-DD." }
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { error("$label must use YYYY-MM-DD.") }
    }
}
