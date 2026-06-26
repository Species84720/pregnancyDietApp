package com.pregnancydiet.app.data

import com.pregnancydiet.app.ai.AiSummaryRecord
import java.time.LocalDate

interface AiSummaryRepository {
    suspend fun generateDailyInsight(
        uid: String,
        date: LocalDate = LocalDate.now(),
    ): Result<AiSummaryRecord>

    suspend fun generateSymptomGuidance(
        uid: String,
        date: LocalDate = LocalDate.now(),
    ): Result<AiSummaryRecord>

    suspend fun generateWeeklySummary(
        uid: String,
        endDate: LocalDate = LocalDate.now(),
    ): Result<AiSummaryRecord>
}
