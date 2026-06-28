package com.pregnancydiet.app.model

data class NutrientAmounts(
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
    val waterMl: Double = 0.0,
)

data class DailyNutritionSummary(
    val date: String,
    val pregnancyProfileId: String?,
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val currentWeightKg: Double,
    val nutritionProfileVersion: String,
    val totals: NutrientAmounts,
    val targets: NutrientAmounts,
    val gaps: List<NutritionGap>,
    val stagePriorities: List<String>,
    val aiNutritionTotals: NutrientAmounts? = null,
    val aiNutritionProcessed: Boolean = false,
    val nutritionProcessedBy: String? = null,
    val nutritionProcessingStatus: String? = null,
) {
    val effectiveTotals: NutrientAmounts
        get() = aiNutritionTotals
            ?.takeIf { aiNutritionProcessed && it.hasAnyTrackedValue() }
            ?: totals
}

data class NutritionGap(
    val nutrient: String,
    val label: String,
    val status: NutritionStatus,
    val severity: GapSeverity,
    val total: Double,
    val target: Double,
    val unit: String,
    val foodSuggestion: String,
)

data class WeeklyNutritionTrend(
    val daysIncluded: Int,
    val averageTotals: NutrientAmounts,
    val repeatedGaps: List<String>,
    val summaries: List<DailyNutritionSummary>,
)

enum class NutritionStatus(val firestoreValue: String) {
    Low("low"),
    Adequate("adequate"),
    High("high"),
}

enum class GapSeverity(val firestoreValue: String) {
    None("none"),
    Mild("mild"),
    Moderate("moderate"),
    High("high"),
}

fun NutrientAmounts.hasAnyTrackedValue(): Boolean = listOf(
    calories,
    proteinGrams,
    fiberGrams,
    folateMcg,
    ironMg,
    calciumMg,
    vitaminDMcg,
    vitaminB12Mcg,
    iodineMcg,
    omega3Mg,
    cholineMg,
    waterMl,
).any { it > 0.0 }
