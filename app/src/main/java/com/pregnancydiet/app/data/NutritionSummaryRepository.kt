package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.WeeklyNutritionTrend
import java.time.LocalDate

interface NutritionSummaryRepository {
    suspend fun generateAndSaveDailySummary(
        uid: String,
        date: LocalDate = LocalDate.now(),
    ): Result<DailyNutritionSummary>

    suspend fun loadDailySummary(
        uid: String,
        date: LocalDate = LocalDate.now(),
    ): Result<DailyNutritionSummary?>

    suspend fun loadWeeklyTrend(
        uid: String,
        endDate: LocalDate = LocalDate.now(),
    ): Result<WeeklyNutritionTrend>
}
