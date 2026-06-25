package com.pregnancydiet.app.model

data class MealLog(
    val id: String,
    val date: String,
    val pregnancyProfileId: String?,
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val mealType: MealType,
    val items: List<MealFoodItem>,
) {
    val itemCount: Int = items.size
}

data class MealFoodItem(
    val foodName: String,
    val quantity: Double,
    val unit: String,
    val weightGrams: Double?,
    val nutrition: FoodNutrition,
)

data class FoodNutrition(
    val calories: Double = 0.0,
    val proteinGrams: Double = 0.0,
    val fiberGrams: Double = 0.0,
    val folateMcg: Double = 0.0,
    val ironMg: Double = 0.0,
    val calciumMg: Double = 0.0,
    val vitaminDMcg: Double = 0.0,
    val vitaminB12Mcg: Double = 0.0,
    val iodineMcg: Double = 0.0,
    val omega3Mg: Double = 0.0,
    val cholineMg: Double = 0.0,
)

enum class MealType(
    val firestoreValue: String,
    val label: String,
) {
    Breakfast("breakfast", "Breakfast"),
    Lunch("lunch", "Lunch"),
    Dinner("dinner", "Dinner"),
    Snack("snack", "Snack"),
    Drink("drink", "Drink"),
}
