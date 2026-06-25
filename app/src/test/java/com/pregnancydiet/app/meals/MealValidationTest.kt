package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MealValidationTest {
    @Test
    fun `valid food item is accepted with nutrition estimate`() {
        val result = MealValidation.validateFoodItem(
            MealFoodItemFormState(
                foodName = "banana",
                quantity = "1",
                unit = "piece",
                weightGrams = "120",
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals("banana", result.getOrThrow().foodName)
        assertEquals(120.0, result.getOrThrow().weightGrams!!, 0.001)
        assertTrue(result.getOrThrow().nutrition.calories > 0.0)
    }

    @Test
    fun `zero quantity is rejected`() {
        val result = MealValidation.validateFoodItem(
            MealFoodItemFormState(
                foodName = "banana",
                quantity = "0",
                unit = "piece",
                weightGrams = "120",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Quantity must be greater than zero.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `meal requires at least one food item`() {
        val result = MealValidation.validateMeal(
            MealFormState(
                date = "2026-06-25",
                mealType = MealType.Breakfast,
                draftItems = emptyList(),
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Add at least one food item before saving the meal.", result.exceptionOrNull()?.message)
    }
}
