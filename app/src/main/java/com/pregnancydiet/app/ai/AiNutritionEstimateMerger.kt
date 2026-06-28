package com.pregnancydiet.app.ai

data class AiNutritionEstimateMergeResult(
    val estimates: AiNutritionEstimates,
    val source: AiNutritionEstimateSource,
    val note: String,
)

object AiNutritionEstimateMerger {
    val fieldKeys: List<String> = listOf(
        "caloriesKcal",
        "proteinGrams",
        "carbsGrams",
        "fatGrams",
        "fiberGrams",
        "folateMcg",
        "ironMg",
        "calciumMg",
        "vitaminDMcg",
        "vitaminB12Mcg",
        "iodineMcg",
        "omega3Mg",
        "cholineMg",
        "waterMl",
    )

    fun merge(
        aiEstimates: Map<String, AiNutritionEstimate>,
        localTotals: AiNutrientPayload,
    ): AiNutritionEstimateMergeResult {
        val sanitizedAi = aiEstimates.filterKeys { it in fieldKeys }
        val merged = fieldKeys.associateWith { key ->
            sanitizedAi[key]?.copy(source = "ai") ?: localEstimate(key, localTotals)
        }
        val source = when (sanitizedAi.size) {
            0 -> AiNutritionEstimateSource.LocalFallback
            fieldKeys.size -> AiNutritionEstimateSource.AiAssisted
            else -> AiNutritionEstimateSource.MixedAiLocal
        }
        val note = when (source) {
            AiNutritionEstimateSource.AiAssisted -> "Nutrition values are AI-assisted estimates from your logged foods."
            AiNutritionEstimateSource.LocalFallback -> "Nutrition values are local fallback estimates from your logged foods."
            AiNutritionEstimateSource.MixedAiLocal -> "Some values were estimated locally because the AI response was incomplete."
        }
        return AiNutritionEstimateMergeResult(
            estimates = merged.toAiNutritionEstimates(),
            source = source,
            note = note,
        )
    }

    fun localEstimates(localTotals: AiNutrientPayload): AiNutritionEstimates = merge(
        aiEstimates = emptyMap(),
        localTotals = localTotals,
    ).estimates

    private fun localEstimate(key: String, localTotals: AiNutrientPayload): AiNutritionEstimate = AiNutritionEstimate(
        value = localTotals.valueFor(key),
        confidence = "local",
        explanation = DEFAULT_LOCAL_ESTIMATE_EXPLANATION,
        source = "local",
    )

    private fun AiNutrientPayload.valueFor(key: String): Double = when (key) {
        "caloriesKcal" -> caloriesKcal.takeIf { it > 0.0 } ?: calories
        "proteinGrams" -> proteinGrams
        "carbsGrams" -> carbsGrams
        "fatGrams" -> fatGrams
        "fiberGrams" -> fiberGrams
        "folateMcg" -> folateMcg
        "ironMg" -> ironMg
        "calciumMg" -> calciumMg
        "vitaminDMcg" -> vitaminDMcg
        "vitaminB12Mcg" -> vitaminB12Mcg
        "iodineMcg" -> iodineMcg
        "omega3Mg" -> omega3Mg
        "cholineMg" -> cholineMg
        "waterMl" -> waterMl
        else -> 0.0
    }

    private fun Map<String, AiNutritionEstimate>.toAiNutritionEstimates(): AiNutritionEstimates = AiNutritionEstimates(
        caloriesKcal = getValue("caloriesKcal"),
        proteinGrams = getValue("proteinGrams"),
        carbsGrams = getValue("carbsGrams"),
        fatGrams = getValue("fatGrams"),
        fiberGrams = getValue("fiberGrams"),
        folateMcg = getValue("folateMcg"),
        ironMg = getValue("ironMg"),
        calciumMg = getValue("calciumMg"),
        vitaminDMcg = getValue("vitaminDMcg"),
        vitaminB12Mcg = getValue("vitaminB12Mcg"),
        iodineMcg = getValue("iodineMcg"),
        omega3Mg = getValue("omega3Mg"),
        cholineMg = getValue("cholineMg"),
        waterMl = getValue("waterMl"),
    )
}
