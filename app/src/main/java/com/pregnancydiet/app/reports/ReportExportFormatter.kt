package com.pregnancydiet.app.reports

import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.NutritionGap
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import java.time.format.DateTimeFormatter
import java.util.Locale

object ReportExportFormatter {
    fun format(report: GynecologistReport): String = buildString {
        appendLine("Pregnancy Diet Tracker report")
        appendLine("Generated: ${report.generatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        appendLine("Date range: ${report.dateRange.label}")
        appendLine()
        appendLine("Important note")
        appendLine("This report contains factual user-entered tracking data and deterministic nutrition summaries. It does not diagnose, prescribe, or replace medical advice.")
        appendLine(AppConstants.MEDICAL_DISCLAIMER)
        appendLine()
        appendPregnancySection(report.pregnancyProfile, report.pregnancyProgress)
        appendSymptomsSection(report.symptomLogs)
        appendSupplementsSection(report.supplements, report.supplementLogs)
        appendMealsSection(report.mealLogs)
        appendNutritionSection(report.nutritionSummaries)
        appendWeightSection(report.weightLogs)
        appendWeeklyAiSection(report.weeklyAiSummaries)
    }.trimEnd()

    private fun StringBuilder.appendPregnancySection(
        profile: PregnancyProfile?,
        progress: PregnancyProgress?,
    ) {
        appendLine("Pregnancy context")
        if (profile == null) {
            appendLine("- No active pregnancy profile found.")
        } else {
            appendLine("- Pregnancy type: ${profile.pregnancyType.firestoreValue}")
            appendLine("- Current calculated week: ${progress?.pregnancyWeek?.toString() ?: "not available"}")
            appendLine("- Current trimester: ${progress?.trimester?.toString() ?: "not available"}")
            appendLine("- Estimated due date: ${progress?.estimatedDueDate?.toString() ?: profile.estimatedDueDate ?: "not available"}")
            appendLine("- Current weight: ${profile.currentWeightKg.formatOne()} kg")
            profile.allergies.takeIf { it.isNotEmpty() }?.let { appendLine("- Allergies: ${it.joinToString()}") }
            profile.dietaryRestrictions.takeIf { it.isNotEmpty() }?.let { appendLine("- Dietary restrictions: ${it.joinToString()}") }
            profile.medicalConditions.takeIf { it.isNotEmpty() }?.let { appendLine("- Logged medical conditions: ${it.joinToString()}") }
        }
        appendLine()
    }

    private fun StringBuilder.appendSymptomsSection(symptomLogs: List<SymptomLog>) {
        appendLine("Symptom history (${symptomLogs.size} logs)")
        if (symptomLogs.isEmpty()) {
            appendLine("- No symptoms logged in this range.")
        } else {
            symptomLogs.forEach { log ->
                val symptoms = log.symptoms.joinToString { symptom ->
                    "${symptom.name} severity ${symptom.severity}/10${symptom.duration.takeIf { it.isNotBlank() }?.let { ", duration $it" }.orEmpty()}"
                }
                appendLine("- ${log.date} · week ${log.pregnancyWeek ?: "?"}: $symptoms")
                if (log.urgentFlag) {
                    appendLine("  Red-flag warning shown by app: ${log.urgentReasons.joinToString()}")
                }
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendSupplementsSection(
        supplements: List<Supplement>,
        logs: List<SupplementLog>,
    ) {
        appendLine("Supplements and adherence")
        if (supplements.isEmpty()) {
            appendLine("- No supplements tracked.")
        } else {
            supplements.sortedBy { it.name.lowercase(Locale.US) }.forEach { supplement ->
                val takenCount = logs.count { it.supplementId == supplement.id && it.taken }
                appendLine("- ${supplement.name}: ${supplement.dose}, ${supplement.frequency}, time ${supplement.timeOfDay}, active ${supplement.active}. Taken logs in range: $takenCount")
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendMealsSection(meals: List<MealLog>) {
        appendLine("Meal history (${meals.size} meals)")
        if (meals.isEmpty()) {
            appendLine("- No meals logged in this range.")
        } else {
            meals.forEach { meal -> appendLine(meal.toReportLine()) }
        }
        appendLine()
    }

    private fun StringBuilder.appendNutritionSection(summaries: List<DailyNutritionSummary>) {
        appendLine("Nutrition gaps (${summaries.size} daily summaries)")
        if (summaries.isEmpty()) {
            appendLine("- No nutrition summaries generated in this range.")
        } else {
            summaries.forEach { summary ->
                appendLine("- ${summary.date} · week ${summary.pregnancyWeek ?: "?"}: ${summary.gaps.toGapSummary()}")
            }
        }
        appendLine()
    }

    private fun StringBuilder.appendWeightSection(weightLogs: List<WeightLog>) {
        appendLine("Weight trend (${weightLogs.size} logs)")
        if (weightLogs.isEmpty()) {
            appendLine("- No weight logs in this range.")
        } else {
            weightLogs.forEach { log ->
                appendLine("- ${log.date} · week ${log.pregnancyWeek ?: "?"}: ${log.weightKg.formatOne()} kg (${log.source})")
            }
            val first = weightLogs.first()
            val last = weightLogs.last()
            appendLine("- Range change: ${(last.weightKg - first.weightKg).formatOne()} kg")
        }
        appendLine()
    }

    private fun StringBuilder.appendWeeklyAiSection(summaries: List<ReportWeeklyAiSummary>) {
        appendLine("Weekly AI summaries (${summaries.size})")
        if (summaries.isEmpty()) {
            appendLine("- No weekly AI summaries saved in this range.")
        } else {
            summaries.forEach { summary ->
                appendLine("- ${summary.weekId} · pregnancy week ${summary.pregnancyWeek ?: "?"}: ${summary.summary}")
                if (summary.urgentWarning) appendLine("  Urgent warning was included in the saved summary.")
                if (summary.fallback) appendLine("  Safe fallback summary was used.")
            }
        }
    }

    private fun MealLog.toReportLine(): String {
        val itemSummary = items.joinToString { item ->
            val grams = item.weightGrams?.let { ", ${it.formatOne()} g" }.orEmpty()
            "${item.foodName} (${item.quantity.formatOne()} ${item.unit}$grams)"
        }
        return "- $date · ${mealType.label} · week ${pregnancyWeek ?: "?"}: $itemSummary"
    }

    private fun List<NutritionGap>.toGapSummary(): String = if (isEmpty()) {
        "No low or high tracked nutrient gaps detected."
    } else {
        joinToString { gap -> "${gap.label} ${gap.status.firestoreValue} (${gap.severity.firestoreValue})" }
    }
}

private fun Double.formatOne(): String = String.format(Locale.US, "%.1f", this)
