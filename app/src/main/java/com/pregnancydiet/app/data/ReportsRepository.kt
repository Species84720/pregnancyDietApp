package com.pregnancydiet.app.data

import com.pregnancydiet.app.reports.GynecologistReport
import com.pregnancydiet.app.reports.ReportDateRange

interface ReportsRepository {
    suspend fun loadReport(
        uid: String,
        dateRange: ReportDateRange,
    ): Result<GynecologistReport>
}
