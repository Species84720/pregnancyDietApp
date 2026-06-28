package com.pregnancydiet.app.ai

import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeeklyNutritionTrend

class AiRequestBuilder {
    fun dailyNutritionSummary(
        date: String,
        pregnancyProfile: PregnancyProfile,
        nutritionSummary: DailyNutritionSummary,
        mealsToday: List<MealLog>,
        symptomsToday: List<SymptomLog>,
        supplementsToday: List<SupplementWithTodayStatus>,
    ): AiSummaryRequest = baseRequest(
        requestType = AiRequestType.DailyNutritionSummary,
        date = date,
        pregnancyProfile = pregnancyProfile,
        pregnancyWeek = nutritionSummary.pregnancyWeek,
        trimester = nutritionSummary.trimester,
        symptomsToday = symptomsToday,
        supplementsToday = supplementsToday,
    ).copy(
        foodsToday = mealsToday.toAiFoods(),
        nutritionTotals = nutritionSummary.totals.toAiPayload(),
        nutritionTargets = nutritionSummary.targets.toAiPayload(),
        detectedGaps = nutritionSummary.gaps.map { it.nutrient },
        nutritionAlreadyProcessedByAi = nutritionSummary.aiNutritionProcessed,
    )

    fun symptomExplanation(
        date: String,
        pregnancyProfile: PregnancyProfile,
        pregnancyWeek: Int?,
        trimester: Int?,
        symptomsToday: List<SymptomLog>,
        supplementsToday: List<SupplementWithTodayStatus> = emptyList(),
    ): AiSummaryRequest = baseRequest(
        requestType = AiRequestType.SymptomExplanation,
        date = date,
        pregnancyProfile = pregnancyProfile,
        pregnancyWeek = pregnancyWeek,
        trimester = trimester,
        symptomsToday = symptomsToday,
        supplementsToday = supplementsToday,
    )

    fun weeklySummary(
        weekId: String,
        endDate: String,
        pregnancyProfile: PregnancyProfile,
        pregnancyWeek: Int?,
        trimester: Int?,
        weeklyTrend: WeeklyNutritionTrend,
        symptomLogs: List<SymptomLog>,
    ): AiSummaryRequest = baseRequest(
        requestType = AiRequestType.WeeklySummary,
        date = endDate,
        weekId = weekId,
        pregnancyProfile = pregnancyProfile,
        pregnancyWeek = pregnancyWeek,
        trimester = trimester,
        symptomsToday = symptomLogs,
        supplementsToday = emptyList(),
    ).copy(
        nutritionTotals = weeklyTrend.averageTotals.toAiPayload(),
        detectedGaps = weeklyTrend.repeatedGaps,
        weeklyRepeatedGaps = weeklyTrend.repeatedGaps,
        nutritionAlreadyProcessedByAi = weeklyTrend.summaries.isNotEmpty() && weeklyTrend.summaries.all { it.aiNutritionProcessed },
    )

    private fun baseRequest(
        requestType: AiRequestType,
        date: String,
        pregnancyProfile: PregnancyProfile,
        pregnancyWeek: Int?,
        trimester: Int?,
        symptomsToday: List<SymptomLog>,
        supplementsToday: List<SupplementWithTodayStatus>,
        weekId: String? = null,
    ): AiSummaryRequest {
        val redFlagReasons = symptomsToday.flatMap { it.urgentReasons }.distinct()
        return AiSummaryRequest(
            requestType = requestType,
            date = date,
            weekId = weekId,
            pregnancyWeek = pregnancyWeek,
            trimester = trimester,
            estimatedDueDate = pregnancyProfile.estimatedDueDate,
            pregnancyType = pregnancyProfile.pregnancyType.firestoreValue,
            heightCm = pregnancyProfile.heightCm,
            prePregnancyWeightKg = pregnancyProfile.prePregnancyWeightKg,
            currentWeightKg = pregnancyProfile.currentWeightKg,
            dietaryRestrictions = pregnancyProfile.dietaryRestrictions,
            allergies = pregnancyProfile.allergies,
            medicalConditions = pregnancyProfile.medicalConditions,
            doctorNotes = "",
            symptomsToday = symptomsToday.toAiSymptoms(),
            supplementsToday = supplementsToday.toAiSupplements(),
            redFlagDetectedByApp = symptomsToday.any { it.urgentFlag },
            redFlagReasons = redFlagReasons,
        )
    }
}

private fun List<SymptomLog>.toAiSymptoms(): List<AiSymptomContext> = flatMap { log ->
    log.symptoms.map { symptom ->
        AiSymptomContext(
            name = symptom.name,
            severity = symptom.severity,
            duration = symptom.duration,
            notes = symptom.notes,
        )
    }
}

private fun List<MealLog>.toAiFoods(): List<AiFoodContext> = flatMap { meal ->
    meal.items.map { item ->
        AiFoodContext(
            foodName = item.foodName,
            quantity = item.quantity,
            unit = item.unit,
            weightGrams = item.weightGrams,
            nutrition = item.nutrition.toAiPayload(),
        )
    }
}

private fun List<SupplementWithTodayStatus>.toAiSupplements(): List<AiSupplementContext> = map { supplementStatus ->
    AiSupplementContext(
        name = supplementStatus.supplement.name,
        dose = supplementStatus.supplement.dose,
        taken = supplementStatus.isTakenToday,
    )
}

private fun FoodNutrition.toAiPayload(): AiNutrientPayload = AiNutrientPayload(
    calories = calories,
    caloriesKcal = calories,
    proteinGrams = proteinGrams,
    fiberGrams = fiberGrams,
    folateMcg = folateMcg,
    ironMg = ironMg,
    calciumMg = calciumMg,
    vitaminDMcg = vitaminDMcg,
    vitaminB12Mcg = vitaminB12Mcg,
    iodineMcg = iodineMcg,
    omega3Mg = omega3Mg,
    cholineMg = cholineMg,
)

private fun NutrientAmounts.toAiPayload(): AiNutrientPayload = AiNutrientPayload(
    calories = calories,
    caloriesKcal = calories,
    proteinGrams = proteinGrams,
    fiberGrams = fiberGrams,
    folateMcg = folateMcg,
    ironMg = ironMg,
    calciumMg = calciumMg,
    vitaminDMcg = vitaminDMcg,
    vitaminB12Mcg = vitaminB12Mcg,
    iodineMcg = iodineMcg,
    omega3Mg = omega3Mg,
    cholineMg = cholineMg,
    waterMl = waterMl,
)
