package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.GapSeverity
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.NutritionGap
import com.pregnancydiet.app.model.NutritionStatus

object NutritionGapDetector {
    fun detect(
        totals: NutrientAmounts,
        targets: NutrientAmounts,
    ): List<NutritionGap> = NutrientCatalog.tracked.mapNotNull { definition ->
        val target = definition.amount(targets)
        if (target <= 0.0) return@mapNotNull null
        val total = definition.amount(totals)
        val ratio = total / target
        val status = when {
            ratio < 0.8 -> NutritionStatus.Low
            ratio > 1.5 -> NutritionStatus.High
            else -> NutritionStatus.Adequate
        }
        if (status == NutritionStatus.Adequate) return@mapNotNull null
        NutritionGap(
            nutrient = definition.key,
            label = definition.label,
            status = status,
            severity = severityFor(ratio, status),
            total = total,
            target = target,
            unit = definition.unit,
            foodSuggestion = definition.foodSuggestion,
        )
    }

    private fun severityFor(ratio: Double, status: NutritionStatus): GapSeverity = when (status) {
        NutritionStatus.Low -> when {
            ratio < 0.5 -> GapSeverity.High
            ratio < 0.65 -> GapSeverity.Moderate
            else -> GapSeverity.Mild
        }
        NutritionStatus.High -> when {
            ratio > 2.0 -> GapSeverity.High
            ratio > 1.75 -> GapSeverity.Moderate
            else -> GapSeverity.Mild
        }
        NutritionStatus.Adequate -> GapSeverity.None
    }
}
