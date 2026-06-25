package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.NutrientAmounts

data class NutrientDefinition(
    val key: String,
    val label: String,
    val unit: String,
    val amount: (NutrientAmounts) -> Double,
    val foodSuggestion: String,
)

object NutrientCatalog {
    val tracked = listOf(
        NutrientDefinition("proteinGrams", "Protein", "g", { it.proteinGrams }, "Try beans, lentils, eggs, yogurt, tofu, fish low in mercury, or lean meats if they fit your diet."),
        NutrientDefinition("folateMcg", "Folate", "mcg", { it.folateMcg }, "Try leafy greens, lentils, beans, asparagus, avocado, or fortified grains."),
        NutrientDefinition("ironMg", "Iron", "mg", { it.ironMg }, "Try lentils, beans, tofu, fortified cereals, spinach, eggs, or lean meats if they fit your diet."),
        NutrientDefinition("calciumMg", "Calcium", "mg", { it.calciumMg }, "Try yogurt, milk, fortified plant milks, calcium-set tofu, sesame, or leafy greens."),
        NutrientDefinition("vitaminDMcg", "Vitamin D", "mcg", { it.vitaminDMcg }, "Try fortified milk or plant milk, eggs, and safe sunlight habits; discuss supplement questions with your clinician."),
        NutrientDefinition("vitaminB12Mcg", "Vitamin B12", "mcg", { it.vitaminB12Mcg }, "Try dairy, eggs, fish, meat, or fortified foods if you eat mostly plant-based."),
        NutrientDefinition("iodineMcg", "Iodine", "mcg", { it.iodineMcg }, "Try dairy, eggs, seafood low in mercury, or iodized salt if appropriate for you."),
        NutrientDefinition("fiberGrams", "Fiber", "g", { it.fiberGrams }, "Try oats, beans, lentils, berries, vegetables, chia, or whole grains."),
        NutrientDefinition("omega3Mg", "Omega-3", "mg", { it.omega3Mg }, "Try salmon or sardines low in mercury, chia, flax, walnuts, or fortified foods."),
        NutrientDefinition("cholineMg", "Choline", "mg", { it.cholineMg }, "Try eggs, soy foods, beans, fish low in mercury, chicken, or broccoli."),
        NutrientDefinition("waterMl", "Water", "ml", { it.waterMl }, "Use water, soups, fruit, and clinician-approved hydration strategies; increase fluids if advised."),
    )
}
