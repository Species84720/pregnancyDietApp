package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.MealFoodItem
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.MealType
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionSummaryCalculatorTest {
    @Test
    fun `daily summary totals meal nutrients and detects gaps`() {
        val summary = NutritionSummaryCalculator.dailySummary(
            date = "2026-06-25",
            pregnancyProfile = profile(),
            pregnancyWeek = 24,
            trimester = 2,
            meals = listOf(meal(protein = 25.0, iron = 4.0)),
        )

        assertEquals(25.0, summary.totals.proteinGrams, 0.001)
        assertEquals(4.0, summary.totals.ironMg, 0.001)
        assertTrue(summary.targets.proteinGrams > 0.0)
        assertTrue(summary.gaps.any { it.nutrient == "proteinGrams" })
        assertTrue(summary.stagePriorities.contains("protein"))
    }

    @Test
    fun `weekly trend finds repeated low gaps`() {
        val first = NutritionSummaryCalculator.dailySummary(
            date = "2026-06-24",
            pregnancyProfile = profile(),
            pregnancyWeek = 24,
            trimester = 2,
            meals = listOf(meal(protein = 20.0, iron = 3.0)),
        )
        val second = NutritionSummaryCalculator.dailySummary(
            date = "2026-06-25",
            pregnancyProfile = profile(),
            pregnancyWeek = 24,
            trimester = 2,
            meals = listOf(meal(protein = 22.0, iron = 4.0)),
        )

        val trend = NutritionSummaryCalculator.weeklyTrend(listOf(first, second))

        assertEquals(2, trend.daysIncluded)
        assertTrue(trend.repeatedGaps.contains("Protein"))
    }

    private fun meal(protein: Double, iron: Double) = MealLog(
        id = "meal_1",
        date = "2026-06-25",
        pregnancyProfileId = "profile_123",
        pregnancyWeek = 24,
        trimester = 2,
        mealType = MealType.Lunch,
        items = listOf(
            MealFoodItem(
                foodName = "lentils",
                quantity = 1.0,
                unit = "serving",
                weightGrams = 150.0,
                nutrition = FoodNutrition(
                    calories = 200.0,
                    proteinGrams = protein,
                    ironMg = iron,
                    fiberGrams = 8.0,
                    folateMcg = 180.0,
                ),
            ),
        ),
    )

    private fun profile() = PregnancyProfile(
        id = "profile_123",
        dateFoundOut = "2026-06-25",
        lastMenstrualPeriod = "2026-02-01",
        estimatedDueDate = "2026-11-08",
        doctorConfirmedWeek = null,
        pregnancyType = PregnancyType.Singleton,
        heightCm = 165.0,
        prePregnancyWeightKg = 68.0,
        currentWeightKg = 72.0,
        weightUnit = WeightUnit.Kg,
        allergies = emptyList(),
        dietaryRestrictions = emptyList(),
        medicalConditions = emptyList(),
    )
}
