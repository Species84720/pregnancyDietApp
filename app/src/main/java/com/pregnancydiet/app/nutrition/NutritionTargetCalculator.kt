package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.PregnancyType
import java.util.Locale
import kotlin.math.max

data class NutritionTargetInput(
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val currentWeightKg: Double,
    val prePregnancyWeightKg: Double?,
    val heightCm: Double?,
    val pregnancyType: PregnancyType,
    val dietaryRestrictions: List<String>,
    val medicalConditions: List<String>,
)

data class NutritionTargetResult(
    val nutritionProfileVersion: String,
    val targets: NutrientAmounts,
    val stagePriorities: List<String>,
    val stageName: String,
)

object NutritionTargetCalculator {
    const val PROFILE_VERSION = "pregnancy_targets_v1"

    fun calculate(input: NutritionTargetInput): NutritionTargetResult {
        val trimester = input.trimester ?: trimesterForWeek(input.pregnancyWeek)
        val stage = PregnancyStageProfile.forTrimester(trimester)
        val multiplePregnancyAdjustment = when (input.pregnancyType) {
            PregnancyType.Twins -> 10.0
            PregnancyType.Multiple -> 15.0
            PregnancyType.Singleton,
            PregnancyType.Unknown -> 0.0
        }
        val bmiAdjustment = input.prePregnancyBmi()?.let { bmi ->
            if (bmi < 18.5) 5.0 else 0.0
        } ?: 0.0
        val restrictionAdjustment = if (input.dietaryRestrictions.normalizedAny("vegan")) 2.0 else 0.0
        val proteinTarget = max(
            stage.baseProteinGrams,
            (input.currentWeightKg * stage.proteinPerKg).coerceAtLeast(0.0) + multiplePregnancyAdjustment + bmiAdjustment + restrictionAdjustment,
        )
        val fiberAdjustment = if (input.medicalConditions.normalizedAny("gestational diabetes", "constipation")) 2.0 else 0.0
        val ironAdjustment = if (input.medicalConditions.normalizedAny("anemia", "low iron")) 3.0 else 0.0
        val b12Adjustment = if (input.dietaryRestrictions.normalizedAny("vegan", "vegetarian")) 0.2 else 0.0
        val waterAdjustment = when (input.pregnancyType) {
            PregnancyType.Twins -> 250.0
            PregnancyType.Multiple -> 350.0
            PregnancyType.Singleton,
            PregnancyType.Unknown -> 0.0
        }

        return NutritionTargetResult(
            nutritionProfileVersion = PROFILE_VERSION,
            targets = NutrientAmounts(
                calories = 0.0,
                proteinGrams = proteinTarget,
                fiberGrams = stage.fiberGrams + fiberAdjustment,
                folateMcg = stage.folateMcg,
                ironMg = stage.ironMg + ironAdjustment,
                calciumMg = stage.calciumMg,
                vitaminDMcg = stage.vitaminDMcg,
                vitaminB12Mcg = stage.vitaminB12Mcg + b12Adjustment,
                iodineMcg = stage.iodineMcg,
                omega3Mg = stage.omega3Mg,
                cholineMg = stage.cholineMg,
                waterMl = stage.waterMl + waterAdjustment,
            ),
            stagePriorities = stage.priorities + input.contextPriorities(),
            stageName = stage.name,
        )
    }

    private fun trimesterForWeek(week: Int?): Int = when (week) {
        null -> 1
        in 1..13 -> 1
        in 14..27 -> 2
        else -> 3
    }
}

private data class PregnancyStageProfile(
    val trimester: Int,
    val name: String,
    val baseProteinGrams: Double,
    val proteinPerKg: Double,
    val fiberGrams: Double,
    val folateMcg: Double,
    val ironMg: Double,
    val calciumMg: Double,
    val vitaminDMcg: Double,
    val vitaminB12Mcg: Double,
    val iodineMcg: Double,
    val omega3Mg: Double,
    val cholineMg: Double,
    val waterMl: Double,
    val priorities: List<String>,
) {
    companion object {
        fun forTrimester(trimester: Int): PregnancyStageProfile = when (trimester) {
            1 -> PregnancyStageProfile(
                trimester = 1,
                name = "Trimester 1",
                baseProteinGrams = 60.0,
                proteinPerKg = 0.95,
                fiberGrams = 28.0,
                folateMcg = 600.0,
                ironMg = 27.0,
                calciumMg = 1000.0,
                vitaminDMcg = 15.0,
                vitaminB12Mcg = 2.6,
                iodineMcg = 220.0,
                omega3Mg = 200.0,
                cholineMg = 450.0,
                waterMl = 2300.0,
                priorities = listOf("folate", "iodine", "hydration", "protein tolerance"),
            )
            2 -> PregnancyStageProfile(
                trimester = 2,
                name = "Trimester 2",
                baseProteinGrams = 70.0,
                proteinPerKg = 1.05,
                fiberGrams = 29.0,
                folateMcg = 600.0,
                ironMg = 27.0,
                calciumMg = 1000.0,
                vitaminDMcg = 15.0,
                vitaminB12Mcg = 2.6,
                iodineMcg = 220.0,
                omega3Mg = 220.0,
                cholineMg = 450.0,
                waterMl = 2400.0,
                priorities = listOf("protein", "iron", "calcium", "vitamin D", "fiber"),
            )
            else -> PregnancyStageProfile(
                trimester = 3,
                name = "Trimester 3",
                baseProteinGrams = 75.0,
                proteinPerKg = 1.1,
                fiberGrams = 30.0,
                folateMcg = 600.0,
                ironMg = 27.0,
                calciumMg = 1000.0,
                vitaminDMcg = 15.0,
                vitaminB12Mcg = 2.6,
                iodineMcg = 220.0,
                omega3Mg = 220.0,
                cholineMg = 460.0,
                waterMl = 2500.0,
                priorities = listOf("protein", "iron", "choline", "fiber", "hydration"),
            )
        }
    }
}

private fun NutritionTargetInput.prePregnancyBmi(): Double? {
    val weight = prePregnancyWeightKg ?: return null
    val heightMeters = heightCm?.div(100.0) ?: return null
    if (weight <= 0.0 || heightMeters <= 0.0) return null
    return weight / (heightMeters * heightMeters)
}

private fun NutritionTargetInput.contextPriorities(): List<String> = buildList {
    if (dietaryRestrictions.normalizedAny("vegan", "vegetarian")) add("vitamin B12")
    if (medicalConditions.normalizedAny("gestational diabetes")) add("fiber")
    if (medicalConditions.normalizedAny("anemia", "low iron")) add("iron-rich foods")
}.distinct()

private fun List<String>.normalizedAny(vararg values: String): Boolean {
    val normalizedValues = values.map { it.lowercase(Locale.US) }
    return any { item ->
        val normalizedItem = item.lowercase(Locale.US)
        normalizedValues.any { it in normalizedItem }
    }
}
