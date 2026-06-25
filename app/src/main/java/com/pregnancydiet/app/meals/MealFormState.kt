package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.MealFoodItem
import com.pregnancydiet.app.model.MealType
import java.time.LocalDate

data class MealFormState(
    val editingMealId: String? = null,
    val date: String = LocalDate.now().toString(),
    val mealType: MealType = MealType.Breakfast,
    val currentItem: MealFoodItemFormState = MealFoodItemFormState(),
    val draftItems: List<MealFoodItem> = emptyList(),
) {
    val isEditing: Boolean = editingMealId != null
}

data class MealFoodItemFormState(
    val foodName: String = "",
    val quantity: String = "1",
    val unit: String = "serving",
    val weightGrams: String = "",
)

data class ValidatedMealInput(
    val id: String?,
    val date: LocalDate,
    val mealType: MealType,
    val items: List<MealFoodItem>,
)
