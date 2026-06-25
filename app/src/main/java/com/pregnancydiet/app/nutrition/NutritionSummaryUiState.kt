package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.WeeklyNutritionTrend
import java.time.LocalDate

data class NutritionSummaryUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val selectedDate: String = LocalDate.now().toString(),
    val dailySummary: DailyNutritionSummary? = null,
    val weeklyTrend: WeeklyNutritionTrend = WeeklyNutritionTrend(
        daysIncluded = 0,
        averageTotals = com.pregnancydiet.app.model.NutrientAmounts(),
        repeatedGaps = emptyList(),
        summaries = emptyList(),
    ),
    val successMessage: String? = null,
    val errorMessage: String? = null,
)
