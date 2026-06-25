package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.MealLog

object MealStatusMapper {
    fun todayStatus(meals: List<MealLog>): String = when {
        meals.isEmpty() -> "No meals logged today."
        meals.size == 1 -> "1 meal logged today with ${meals.sumOf { it.itemCount }} food item."
        else -> "${meals.size} meals logged today with ${meals.sumOf { it.itemCount }} food items."
    }
}
