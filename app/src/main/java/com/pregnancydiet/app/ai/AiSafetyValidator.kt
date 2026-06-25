package com.pregnancydiet.app.ai

object AiSafetyValidator {
    private val prohibitedPatterns: List<Regex> = listOf(
        Regex("\\bdefinitely\\s+safe\\b", RegexOption.IGNORE_CASE),
        Regex("\\bsafe\\s+to\\s+ignore\\b", RegexOption.IGNORE_CASE),
        Regex("\\bignore\\s+(this|the)\\s+symptom", RegexOption.IGNORE_CASE),
        Regex("\\b(stop|start|change|increase|decrease|double|skip)\\s+(your\\s+)?(prescribed\\s+)?(medication|medicine|supplement|dose|dosage|pill|pills)\\b", RegexOption.IGNORE_CASE),
        Regex("\\byou\\s+(have|are\\s+having)\\s+(preeclampsia|gestational\\s+diabetes|anemia|miscarriage|infection|allergic\\s+reaction)\\b", RegexOption.IGNORE_CASE),
        Regex("\\bdiagnosed\\s+with\\b", RegexOption.IGNORE_CASE),
    )

    fun validate(
        response: AiSummaryResponse,
        request: AiSummaryRequest,
    ): AiSummaryResult {
        if (response.disclaimer.isBlank()) {
            return AiPromptGuardrails.fallbackFor(request, "AI response did not include a medical disclaimer.")
        }
        val displayedText = response.displayedText()
        val prohibitedMatch = prohibitedPatterns.firstOrNull { it.containsMatchIn(displayedText) }
        if (prohibitedMatch != null) {
            return AiPromptGuardrails.fallbackFor(request, "AI response contained unsafe medical guidance.")
        }

        val normalizedResponse = if (request.redFlagDetectedByApp) {
            response.withUrgentWarning(request)
        } else {
            response
        }
        return AiSummaryResult.Success(normalizedResponse)
    }

    private fun AiSummaryResponse.withUrgentWarning(request: AiSummaryRequest): AiSummaryResponse {
        val reasons = urgentReasons.ifEmpty {
            request.redFlagReasons.ifEmpty { listOf("A red-flag symptom was detected by the app.") }
        }
        val urgentNextStep = "Because a red-flag symptom was reported, seek medical advice urgently."
        return copy(
            urgentWarning = true,
            urgentReasons = reasons,
            symptomGuidance = symptomGuidance?.copy(
                severity = "urgent",
                contactDoctorIf = (symptomGuidance.contactDoctorIf + urgentNextStep).distinct(),
            ) ?: AiSymptomGuidance(
                severity = "urgent",
                commonContext = "This app cannot assess emergencies.",
                selfCare = emptyList(),
                contactDoctorIf = listOf(urgentNextStep),
            ),
            nextSteps = (listOf(urgentNextStep) + nextSteps).distinct(),
        )
    }

    private fun AiSummaryResponse.displayedText(): String = buildString {
        appendLine(summary)
        appendLine(stageContext)
        nutritionGaps.forEach { gap ->
            appendLine(gap.nutrient)
            appendLine(gap.status)
            appendLine(gap.explanation)
            appendLine(gap.foodSuggestions.joinToString(" "))
            appendLine(gap.safetyNote)
        }
        symptomGuidance?.let { guidance ->
            appendLine(guidance.severity)
            appendLine(guidance.commonContext)
            appendLine(guidance.selfCare.joinToString(" "))
            appendLine(guidance.contactDoctorIf.joinToString(" "))
        }
        weightContext?.let { context -> appendLine(context.summary) }
        appendLine(urgentReasons.joinToString(" "))
        appendLine(nextSteps.joinToString(" "))
        appendLine(disclaimer)
    }
}
