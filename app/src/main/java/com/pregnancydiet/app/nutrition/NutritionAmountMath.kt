package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.NutrientAmounts

object NutritionAmountMath {
    fun totalsFromMeals(meals: List<MealLog>): NutrientAmounts = meals
        .flatMap { it.items }
        .fold(NutrientAmounts()) { total, item -> total + item.nutrition.toNutrientAmounts() }

    fun average(values: List<NutrientAmounts>): NutrientAmounts {
        if (values.isEmpty()) return NutrientAmounts()
        val total = values.fold(NutrientAmounts()) { sum, next -> sum + next }
        return total / values.size.toDouble()
    }
}

operator fun NutrientAmounts.plus(other: NutrientAmounts): NutrientAmounts = NutrientAmounts(
    calories = calories + other.calories,
    proteinGrams = proteinGrams + other.proteinGrams,
    fiberGrams = fiberGrams + other.fiberGrams,
    folateMcg = folateMcg + other.folateMcg,
    ironMg = ironMg + other.ironMg,
    calciumMg = calciumMg + other.calciumMg,
    vitaminDMcg = vitaminDMcg + other.vitaminDMcg,
    vitaminB12Mcg = vitaminB12Mcg + other.vitaminB12Mcg,
    iodineMcg = iodineMcg + other.iodineMcg,
    omega3Mg = omega3Mg + other.omega3Mg,
    cholineMg = cholineMg + other.cholineMg,
    waterMl = waterMl + other.waterMl,
)

operator fun NutrientAmounts.div(divisor: Double): NutrientAmounts = NutrientAmounts(
    calories = calories / divisor,
    proteinGrams = proteinGrams / divisor,
    fiberGrams = fiberGrams / divisor,
    folateMcg = folateMcg / divisor,
    ironMg = ironMg / divisor,
    calciumMg = calciumMg / divisor,
    vitaminDMcg = vitaminDMcg / divisor,
    vitaminB12Mcg = vitaminB12Mcg / divisor,
    iodineMcg = iodineMcg / divisor,
    omega3Mg = omega3Mg / divisor,
    cholineMg = cholineMg / divisor,
    waterMl = waterMl / divisor,
)

fun FoodNutrition.toNutrientAmounts(): NutrientAmounts = NutrientAmounts(
    calories = calories,
    proteinGrams = proteinGrams,
    fiberGrams = fiberGrams,
    folateMcg = folateMcg,
    ironMg = ironMg,
    calciumMg = calciumMg,
    vitaminDMcg = vitaminDMcg,
    vitaminB12Mcg = vitaminB12Mcg,
    iodineMcg = iodineMcg,
    omega3Mg = omega3Mg,
    cholineMg = cholineMg,
)
