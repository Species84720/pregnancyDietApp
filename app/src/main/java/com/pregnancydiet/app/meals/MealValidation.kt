package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.MealFoodItem
import java.time.LocalDate

object MealValidation {
    fun validateFoodItem(form: MealFoodItemFormState): Result<MealFoodItem> = runCatching {
        val foodName = form.foodName.trim()
        val unit = form.unit.trim()
        require(foodName.isNotBlank()) { "Food name is required." }
        require(unit.isNotBlank()) { "Unit is required." }
        val quantity = form.quantity.trim().toDoubleOrNull()
            ?: error("Quantity must be a valid number.")
        require(quantity > 0.0) { "Quantity must be greater than zero." }
        val weightGrams = form.weightGrams.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull()
            ?: if (form.weightGrams.isBlank()) null else error("Weight in grams must be a valid number.")
        if (weightGrams != null) {
            require(weightGrams > 0.0) { "Weight in grams must be greater than zero." }
        }

        MealFoodItem(
            foodName = foodName,
            quantity = quantity,
            unit = unit,
            weightGrams = weightGrams,
            nutrition = MealNutritionEstimator.estimate(foodName, quantity, weightGrams),
        )
    }

    fun validateMeal(form: MealFormState): Result<ValidatedMealInput> = runCatching {
        val date = parseDate(form.date)
        require(form.draftItems.isNotEmpty()) { "Add at least one food item before saving the meal." }
        ValidatedMealInput(
            id = form.editingMealId,
            date = date,
            mealType = form.mealType,
            items = form.draftItems,
        )
    }

    private fun parseDate(value: String): LocalDate {
        require(value.isNotBlank()) { "Date is required. Use YYYY-MM-DD." }
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { error("Date must use YYYY-MM-DD.") }
    }
}
