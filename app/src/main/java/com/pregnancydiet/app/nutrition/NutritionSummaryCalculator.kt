package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.NutritionStatus
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.WeeklyNutritionTrend

object NutritionSummaryCalculator {
    fun dailySummary(
        date: String,
        pregnancyProfile: PregnancyProfile,
        pregnancyWeek: Int?,
        trimester: Int?,
        meals: List<MealLog>,
    ): DailyNutritionSummary {
        val totals = NutritionAmountMath.totalsFromMeals(meals)
        val targetResult = NutritionTargetCalculator.calculate(
            NutritionTargetInput(
                pregnancyWeek = pregnancyWeek,
                trimester = trimester,
                currentWeightKg = pregnancyProfile.currentWeightKg,
                prePregnancyWeightKg = pregnancyProfile.prePregnancyWeightKg,
                heightCm = pregnancyProfile.heightCm,
                pregnancyType = pregnancyProfile.pregnancyType,
                dietaryRestrictions = pregnancyProfile.dietaryRestrictions,
                medicalConditions = pregnancyProfile.medicalConditions,
            ),
        )
        return DailyNutritionSummary(
            date = date,
            pregnancyProfileId = pregnancyProfile.id,
            pregnancyWeek = pregnancyWeek,
            trimester = trimester,
            currentWeightKg = pregnancyProfile.currentWeightKg,
            nutritionProfileVersion = targetResult.nutritionProfileVersion,
            totals = totals,
            targets = targetResult.targets,
            gaps = NutritionGapDetector.detect(totals, targetResult.targets),
            stagePriorities = targetResult.stagePriorities,
        )
    }

    fun weeklyTrend(summaries: List<DailyNutritionSummary>): WeeklyNutritionTrend {
        val repeatedGaps = summaries
            .flatMap { summary -> summary.gaps.filter { it.status == NutritionStatus.Low }.map { it.label } }
            .groupingBy { it }
            .eachCount()
            .filterValues { it >= 2 }
            .keys
            .sorted()
        return WeeklyNutritionTrend(
            daysIncluded = summaries.size,
            averageTotals = NutritionAmountMath.average(summaries.map { it.totals }),
            repeatedGaps = repeatedGaps,
            summaries = summaries,
        )
    }
}
