package com.pregnancydiet.app.ai

private const val JSON_RESPONSE_INSTRUCTIONS = """
Return strict JSON only. Do not include markdown fences, headings, bullets, or any extra text. Use this schema: {
  "summary": "short educational summary",
  "stageContext": "pregnancy week or trimester context",
  "nutritionEstimates": {},
  "nutritionGaps": [],
  "recommendations": [],
  "safetyWarnings": [],
  "symptomGuidance": null,
  "weightContext": null,
  "urgentWarning": false,
  "urgentReasons": [],
  "nextSteps": ["What to do now", "When to contact your clinician"],
  "disclaimer": "This is educational guidance and does not replace medical advice."
}
When nutrition context is provided, include AI-assisted nutritionEstimates using these keys where possible: caloriesKcal, proteinGrams, carbsGrams, fatGrams, fiberGrams, folateMcg, ironMg, calciumMg, vitaminDMcg, vitaminB12Mcg, iodineMcg, omega3Mg, cholineMg, waterMl.
"""

private const val DAILY_NUTRITION_JSON_RESPONSE_INSTRUCTIONS = """
Return ONLY compact strict JSON. Do not include markdown fences, headings, bullets, reasoning, commentary, or extra text.
This is pregnancy nutrition support for education and tracking only, not medical diagnosis or treatment.
The response must include: summary, nutritionEstimates, nutritionGaps, recommendations, safetyWarnings, nextSteps, and disclaimer.
Every nutritionGaps item must include nutrientKey, displayName, status, explanation, foodSuggestions, and safetyNote.
Every nutritionEstimates item must be an object with value, confidence, and explanation. Prefer an object keyed by nutrient name, but an array with key/value/confidence/explanation is acceptable.
Use these nutritionEstimates keys exactly: caloriesKcal, proteinGrams, carbsGrams, fatGrams, fiberGrams, folateMcg, ironMg, calciumMg, vitaminDMcg, vitaminB12Mcg, iodineMcg, omega3Mg, cholineMg, waterMl.
Estimate totals from logged foods. Keep explanations under 12 words. If uncertain, use confidence "low".
Do not recommend changing prescribed supplements or medication; say to contact a gynecologist for medical concerns or supplement changes.

Example: {"summary":"Logged foods provide protein but some nutrients may need attention.","nutritionEstimates":{"proteinGrams":{"value":72.5,"confidence":"medium","explanation":"Estimated from logged foods."}},"nutritionGaps":[{"nutrientKey":"ironMg","displayName":"Iron","status":"low","explanation":"Iron may need attention today.","foodSuggestions":["lentils","beans"],"safetyNote":"For medical concerns or supplement changes, contact your gynecologist."}],"recommendations":["Add food-based iron if appropriate."],"safetyWarnings":["Educational guidance only."],"nextSteps":["Discuss persistent gaps with your care team."],"disclaimer":"This app provides educational guidance and does not replace medical advice."}
"""

private const val SAFETY_GUARDRAILS = """
You are a pregnancy wellness assistant. Do not diagnose, prescribe, or replace a doctor, gynecologist, midwife, pharmacist, dietitian, or emergency service.
Never recommend stopping, starting, or changing prescribed medications or supplements.
For severe symptoms such as bleeding, severe abdominal pain, high fever, reduced fetal movement, severe headache, vision changes, sudden swelling, chest pain, shortness of breath, fainting, seizure, allergic reaction, persistent vomiting, or dehydration signs, advise urgent medical care.
For diet guidance, avoid alcohol, high-mercury fish, unpasteurized dairy or juice, undercooked meat, undercooked eggs, raw seafood, unsafe supplements, excess vitamin A or retinol unless prescribed, unwashed produce, and deli meats unless heated according to local pregnancy guidance.
"""

class PregnancyAdvicePromptBuilder {
    fun build(request: PregnancyAiRequest): String = buildString {
        appendLine(SAFETY_GUARDRAILS)
        appendLine(JSON_RESPONSE_INSTRUCTIONS)
        appendLine("Task: Provide concise pregnancy wellness advice.")
        appendLine("Pregnancy week: ${request.pregnancyWeek ?: "unknown"}; trimester: ${request.trimester ?: "unknown"}.")
        if (request.nutritionTotals.hasAnyNutrition()) {
            if (request.nutritionAlreadyProcessedByAi) {
                appendLine("Weekly average nutrition context is already based on saved AI nutrition estimates: ${request.nutritionTotals.toPromptSummary()}.")
                appendLine("Reuse these totals for nutritionEstimates; do not recalculate nutrients from scratch.")
            } else {
                appendLine("Weekly average nutrition context from saved logs: ${request.nutritionTotals.toPromptSummary()}.")
                appendLine("Return nutritionEstimates as AI-assisted weekly average estimates with value, confidence, and explanation.")
            }
        }
        if (request.nutritionGaps.isNotEmpty()) appendLine("Nutrition gaps: ${request.nutritionGaps.joinToString()}.")
        if (request.redFlagDetected) appendLine("Local app red-flag detected: ${request.redFlagReasons.joinToString()}.")
    }
}

class DietPlanPromptBuilder {
    fun build(request: DietAiRequest): String = buildString {
        appendLine(SAFETY_GUARDRAILS)
        appendLine(DAILY_NUTRITION_JSON_RESPONSE_INSTRUCTIONS)
        appendLine("Task: Generate today's daily nutrition summary with AI-assisted nutrition estimates from logged foods.")
        appendLine("Pregnancy week: ${request.pregnancyWeek ?: "unknown"}; trimester: ${request.trimester ?: "unknown"}.")
        if (request.allergies.isNotEmpty()) appendLine("Avoid allergies: ${request.allergies.joinToString()}.")
        if (request.dietaryRestrictions.isNotEmpty()) appendLine("Respect dietary restrictions: ${request.dietaryRestrictions.joinToString()}.")
        if (request.medicalConditions.isNotEmpty()) appendLine("Consider medical conditions only in general educational terms: ${request.medicalConditions.joinToString()}.")
        if (request.nutritionGaps.isNotEmpty()) appendLine("Focus on food-based support for gaps: ${request.nutritionGaps.joinToString()}.")
        if (request.nutritionAlreadyProcessedByAi) {
            appendLine("Saved AI nutrition estimates already exist for this date: ${request.nutritionTotals.toPromptSummary()}.")
            appendLine("Reuse these totals for nutritionEstimates and avoid recalculating nutrients from scratch.")
        }
        if (request.foodsToday.isNotEmpty() && !request.nutritionAlreadyProcessedByAi) {
            appendLine("Logged foods today. Estimate nutrition totals from these items:")
            request.foodsToday.take(20).forEach { food ->
                appendLine("- ${food.foodName}: ${food.quantity} ${food.unit}, weight grams ${food.weightGrams ?: "unknown"}. App fallback estimate for context only: calories ${food.nutrition.caloriesKcal}, protein ${food.nutrition.proteinGrams} g, fiber ${food.nutrition.fiberGrams} g, folate ${food.nutrition.folateMcg} mcg, iron ${food.nutrition.ironMg} mg, calcium ${food.nutrition.calciumMg} mg, vitamin D ${food.nutrition.vitaminDMcg} mcg, B12 ${food.nutrition.vitaminB12Mcg} mcg, iodine ${food.nutrition.iodineMcg} mcg, omega-3 ${food.nutrition.omega3Mg} mg, choline ${food.nutrition.cholineMg} mg.")
            }
        } else if (request.foodsToday.isEmpty()) {
            appendLine("No foods were logged today; return low-confidence zero estimates and recommend logging meals.")
        }
    }
}

private fun AiNutrientPayload.hasAnyNutrition(): Boolean = listOf(
    calories,
    caloriesKcal,
    proteinGrams,
    carbsGrams,
    fatGrams,
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

private fun AiNutrientPayload.toPromptSummary(): String = listOf(
    "caloriesKcal=$caloriesKcal",
    "proteinGrams=$proteinGrams",
    "fiberGrams=$fiberGrams",
    "folateMcg=$folateMcg",
    "ironMg=$ironMg",
    "calciumMg=$calciumMg",
    "vitaminDMcg=$vitaminDMcg",
    "vitaminB12Mcg=$vitaminB12Mcg",
    "iodineMcg=$iodineMcg",
    "omega3Mg=$omega3Mg",
    "cholineMg=$cholineMg",
    "waterMl=$waterMl",
).joinToString()

class SymptomAnalysisPromptBuilder {
    fun build(request: SymptomAiRequest): String = buildString {
        appendLine(SAFETY_GUARDRAILS)
        appendLine(JSON_RESPONSE_INSTRUCTIONS)
        appendLine("Task: Explain symptom context in educational, non-diagnostic language.")
        appendLine("Pregnancy week: ${request.pregnancyWeek ?: "unknown"}; trimester: ${request.trimester ?: "unknown"}.")
        appendLine("Symptoms: ${request.symptoms.joinToString { symptom -> "${symptom.name} severity ${symptom.severity}" }}.")
        if (request.redFlagDetected) appendLine("Local app red-flag detected; urgentWarning must be true. Reasons: ${request.redFlagReasons.joinToString()}.")
    }
}

class MedicationSupplementPromptBuilder {
    fun build(request: MedicationSupplementAiRequest): String = buildString {
        appendLine(SAFETY_GUARDRAILS)
        appendLine(JSON_RESPONSE_INSTRUCTIONS)
        appendLine("Task: Review logged supplements for educational reminders only.")
        appendLine("Pregnancy week: ${request.pregnancyWeek ?: "unknown"}; trimester: ${request.trimester ?: "unknown"}.")
        appendLine("Question: ${request.question.take(240)}")
        if (request.supplements.isNotEmpty()) appendLine("Logged supplements by name only: ${request.supplements.joinToString { it.name }}.")
        appendLine("Always advise checking with a gynecologist or pharmacist and never stopping prescribed medication without professional advice.")
    }
}