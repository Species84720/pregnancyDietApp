package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.PregnancyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionTargetCalculatorTest {
    @Test
    fun `targets change by trimester`() {
        val firstTrimester = NutritionTargetCalculator.calculate(baseInput(pregnancyWeek = 10, trimester = 1))
        val thirdTrimester = NutritionTargetCalculator.calculate(baseInput(pregnancyWeek = 30, trimester = 3))

        assertTrue(thirdTrimester.targets.proteinGrams > firstTrimester.targets.proteinGrams)
        assertTrue(thirdTrimester.targets.waterMl > firstTrimester.targets.waterMl)
        assertEquals("pregnancy_targets_v1", thirdTrimester.nutritionProfileVersion)
    }

    @Test
    fun `protein target responds to current weight`() {
        val lighter = NutritionTargetCalculator.calculate(baseInput(currentWeightKg = 60.0, trimester = 2))
        val heavier = NutritionTargetCalculator.calculate(baseInput(currentWeightKg = 90.0, trimester = 2))

        assertTrue(heavier.targets.proteinGrams > lighter.targets.proteinGrams)
    }

    @Test
    fun `pregnancy type and context adjust targets and priorities`() {
        val result = NutritionTargetCalculator.calculate(
            baseInput(
                pregnancyType = PregnancyType.Twins,
                dietaryRestrictions = listOf("vegan"),
                medicalConditions = listOf("gestational diabetes"),
            ),
        )

        assertTrue(result.targets.proteinGrams >= 80.0)
        assertTrue(result.targets.fiberGrams > 29.0)
        assertTrue(result.stagePriorities.contains("vitamin B12"))
    }

    private fun baseInput(
        pregnancyWeek: Int = 20,
        trimester: Int = 2,
        currentWeightKg: Double = 70.0,
        pregnancyType: PregnancyType = PregnancyType.Singleton,
        dietaryRestrictions: List<String> = emptyList(),
        medicalConditions: List<String> = emptyList(),
    ) = NutritionTargetInput(
        pregnancyWeek = pregnancyWeek,
        trimester = trimester,
        currentWeightKg = currentWeightKg,
        prePregnancyWeightKg = 68.0,
        heightCm = 165.0,
        pregnancyType = pregnancyType,
        dietaryRestrictions = dietaryRestrictions,
        medicalConditions = medicalConditions,
    )
}
