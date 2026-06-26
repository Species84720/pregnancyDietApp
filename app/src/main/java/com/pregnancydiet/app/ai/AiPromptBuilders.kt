package com.pregnancydiet.app.ai

private const val JSON_RESPONSE_INSTRUCTIONS = """
Return JSON only using this schema: {
  "summary": "short educational summary",
  "stageContext": "pregnancy week or trimester context",
  "nutritionGaps": [],
  "symptomGuidance": null,
  "weightContext": null,
  "urgentWarning": false,
  "urgentReasons": [],
  "nextSteps": ["What to do now", "When to contact your clinician"],
  "disclaimer": "This is educational guidance and does not replace medical advice."
}
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
        if (request.nutritionGaps.isNotEmpty()) appendLine("Nutrition gaps: ${request.nutritionGaps.joinToString()}.")
        if (request.redFlagDetected) appendLine("Local app red-flag detected: ${request.redFlagReasons.joinToString()}.")
    }
}

class DietPlanPromptBuilder {
    fun build(request: DietAiRequest): String = buildString {
        appendLine(SAFETY_GUARDRAILS)
        appendLine(JSON_RESPONSE_INSTRUCTIONS)
        appendLine("Task: Create trimester-aware food suggestions and a brief diet plan.")
        appendLine("Pregnancy week: ${request.pregnancyWeek ?: "unknown"}; trimester: ${request.trimester ?: "unknown"}.")
        if (request.allergies.isNotEmpty()) appendLine("Avoid allergies: ${request.allergies.joinToString()}.")
        if (request.dietaryRestrictions.isNotEmpty()) appendLine("Respect dietary restrictions: ${request.dietaryRestrictions.joinToString()}.")
        if (request.medicalConditions.isNotEmpty()) appendLine("Consider medical conditions only in general educational terms: ${request.medicalConditions.joinToString()}.")
        if (request.nutritionGaps.isNotEmpty()) appendLine("Focus on food-based support for gaps: ${request.nutritionGaps.joinToString()}.")
        if (request.foodsToday.isNotEmpty()) appendLine("Foods today, minimized context: ${request.foodsToday.take(8).joinToString { it.foodName }}.")
    }
}

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