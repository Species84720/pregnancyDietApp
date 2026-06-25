package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.MealFoodItem
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.MealType
import org.junit.Assert.assertEquals
import org.junit.Test

class MealStatusMapperTest {
    @Test
    fun `empty meals show empty status`() {
        assertEquals("No meals logged today.", MealStatusMapper.todayStatus(emptyList()))
    }

    @Test
    fun `single meal status includes item count`() {
        assertEquals("1 meal logged today with 1 food item.", MealStatusMapper.todayStatus(listOf(meal("1", 1))))
    }

    @Test
    fun `multiple meals status includes total food items`() {
        assertEquals(
            "2 meals logged today with 3 food items.",
            MealStatusMapper.todayStatus(listOf(meal("1", 1), meal("2", 2))),
        )
    }

    private fun meal(id: String, itemCount: Int) = MealLog(
        id = id,
        date = "2026-06-25",
        pregnancyProfileId = "profile_123",
        pregnancyWeek = 8,
        trimester = 1,
        mealType = MealType.Breakfast,
        items = List(itemCount) { index ->
            MealFoodItem(
                foodName = "Food $index",
                quantity = 1.0,
                unit = "serving",
                weightGrams = 100.0,
                nutrition = FoodNutrition(calories = 100.0),
            )
        },
    )
}
