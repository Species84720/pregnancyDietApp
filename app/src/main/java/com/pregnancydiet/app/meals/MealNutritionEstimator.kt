package com.pregnancydiet.app.meals

import com.pregnancydiet.app.model.FoodNutrition
import java.util.Locale
import kotlin.math.max

object MealNutritionEstimator {
    fun estimate(
        foodName: String,
        quantity: Double,
        weightGrams: Double?,
    ): FoodNutrition {
        val grams = weightGrams ?: (quantity * DEFAULT_SERVING_GRAMS)
        val multiplier = max(grams, 0.0) / 100.0
        val base = baseNutritionPer100g(foodName)
        return base.scale(multiplier)
    }

    private fun baseNutritionPer100g(foodName: String): FoodNutrition {
        val normalized = foodName.lowercase(Locale.US)
        return when {
            "banana" in normalized -> FoodNutrition(
                calories = 89.0,
                proteinGrams = 1.1,
                fiberGrams = 2.6,
                folateMcg = 20.0,
                ironMg = 0.3,
                calciumMg = 5.0,
                cholineMg = 9.8,
            )
            "egg" in normalized -> FoodNutrition(
                calories = 155.0,
                proteinGrams = 13.0,
                fiberGrams = 0.0,
                folateMcg = 47.0,
                ironMg = 1.8,
                calciumMg = 50.0,
                vitaminDMcg = 2.0,
                vitaminB12Mcg = 1.1,
                iodineMcg = 49.0,
                omega3Mg = 80.0,
                cholineMg = 294.0,
            )
            "milk" in normalized -> FoodNutrition(
                calories = 61.0,
                proteinGrams = 3.2,
                fiberGrams = 0.0,
                folateMcg = 5.0,
                ironMg = 0.0,
                calciumMg = 113.0,
                vitaminDMcg = 1.2,
                vitaminB12Mcg = 0.5,
                iodineMcg = 16.0,
                cholineMg = 15.0,
            )
            "lentil" in normalized -> FoodNutrition(
                calories = 116.0,
                proteinGrams = 9.0,
                fiberGrams = 7.9,
                folateMcg = 181.0,
                ironMg = 3.3,
                calciumMg = 19.0,
                cholineMg = 32.0,
            )
            "spinach" in normalized -> FoodNutrition(
                calories = 23.0,
                proteinGrams = 2.9,
                fiberGrams = 2.2,
                folateMcg = 194.0,
                ironMg = 2.7,
                calciumMg = 99.0,
                iodineMcg = 3.0,
                cholineMg = 19.0,
            )
            else -> FoodNutrition(
                calories = 100.0,
                proteinGrams = 3.0,
                fiberGrams = 2.0,
                folateMcg = 10.0,
                ironMg = 0.5,
                calciumMg = 20.0,
                cholineMg = 10.0,
            )
        }
    }

    private fun FoodNutrition.scale(multiplier: Double): FoodNutrition = FoodNutrition(
        calories = calories * multiplier,
        proteinGrams = proteinGrams * multiplier,
        fiberGrams = fiberGrams * multiplier,
        folateMcg = folateMcg * multiplier,
        ironMg = ironMg * multiplier,
        calciumMg = calciumMg * multiplier,
        vitaminDMcg = vitaminDMcg * multiplier,
        vitaminB12Mcg = vitaminB12Mcg * multiplier,
        iodineMcg = iodineMcg * multiplier,
        omega3Mg = omega3Mg * multiplier,
        cholineMg = cholineMg * multiplier,
    )

    private const val DEFAULT_SERVING_GRAMS = 100.0
}
