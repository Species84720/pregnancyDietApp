package com.pregnancydiet.app.reports

import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import java.time.LocalDateTime

data class GynecologistReport(
    val generatedAt: LocalDateTime,
    val dateRange: ReportDateRange,
    val pregnancyProfile: PregnancyProfile?,
    val pregnancyProgress: PregnancyProgress?,
    val symptomLogs: List<SymptomLog>,
    val supplements: List<Supplement>,
    val supplementLogs: List<SupplementLog>,
    val mealLogs: List<MealLog>,
    val nutritionSummaries: List<DailyNutritionSummary>,
    val weightLogs: List<WeightLog>,
    val weeklyAiSummaries: List<ReportWeeklyAiSummary>,
) {
    val activeSupplements: List<Supplement> = supplements.filter { it.active }
    val supplementTakenCount: Int = supplementLogs.count { it.taken }
    val urgentSymptomCount: Int = symptomLogs.count { it.urgentFlag }
}

data class ReportWeeklyAiSummary(
    val weekId: String,
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val summary: String,
    val urgentWarning: Boolean,
    val fallback: Boolean,
)
