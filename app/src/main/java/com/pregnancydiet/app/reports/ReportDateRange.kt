package com.pregnancydiet.app.reports

import java.time.LocalDate

data class ReportDateRange(
    val startDate: LocalDate,
    val endDate: LocalDate,
) {
    init {
        require(!endDate.isBefore(startDate)) { "End date cannot be before start date." }
    }

    val label: String = "${startDate} to ${endDate}"

    companion object {
        fun default(today: LocalDate = LocalDate.now()): ReportDateRange = ReportDateRange(
            startDate = today.minusDays(6),
            endDate = today,
        )
    }
}
