package com.pregnancydiet.app.meals

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MealNutritionEstimatorTest {
    @Test
    fun `banana estimate scales by grams`() {
        val nutrition = MealNutritionEstimator.estimate(
            foodName = "banana",
            quantity = 1.0,
            weightGrams = 120.0,
        )

        assertEquals(106.8, nutrition.calories, 0.001)
        assertEquals(1.32, nutrition.proteinGrams, 0.001)
        assertTrue(nutrition.folateMcg > 0.0)
    }

    @Test
    fun `unknown food uses local placeholder estimate`() {
        val nutrition = MealNutritionEstimator.estimate(
            foodName = "homemade meal",
            quantity = 2.0,
            weightGrams = null,
        )

        assertEquals(200.0, nutrition.calories, 0.001)
        assertEquals(6.0, nutrition.proteinGrams, 0.001)
    }
}
