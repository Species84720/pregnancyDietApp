package com.pregnancydiet.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiResponseParserTest {
    private val parser = AiResponseParser()

    @Test
    fun `valid response parses structured summary`() {
        val result = parser.parse(validJson(), baseRequest())

        assertTrue(result is AiSummaryResult.Success)
        val response = (result as AiSummaryResult.Success).response
        assertEquals("Short user-friendly summary.", response.summary)
        assertEquals("ironMg", response.nutritionGaps.first().nutrientKey)
        assertFalse(response.urgentWarning)
        assertEquals(AiPromptGuardrails.DISCLAIMER, response.disclaimer)
    }

    @Test
    fun `app red flag forces urgent warning`() {
        val result = parser.parse(
            validJson(urgentWarning = false, urgentReasons = emptyList()),
            baseRequest(
                redFlagDetectedByApp = true,
                redFlagReasons = listOf("Vaginal bleeding was logged."),
            ),
        )

        assertTrue(result is AiSummaryResult.Success)
        val response = (result as AiSummaryResult.Success).response
        assertTrue(response.urgentWarning)
        assertEquals(listOf("Vaginal bleeding was logged."), response.urgentReasons)
        assertEquals("urgent", response.symptomGuidance?.severity)
    }

    @Test
    fun `missing disclaimer returns fallback`() {
        val result = parser.parse(validJson(disclaimer = ""), baseRequest())

        assertTrue(result is AiSummaryResult.Fallback)
        val fallback = (result as AiSummaryResult.Fallback).fallback
        assertEquals(AiPromptGuardrails.FALLBACK_MESSAGE, fallback.message)
        assertEquals(AiPromptGuardrails.DISCLAIMER, fallback.disclaimer)
    }

    @Test
    fun `unsafe medication change advice returns fallback`() {
        val result = parser.parse(
            validJson(summary = "You should stop your prescribed supplement today."),
            baseRequest(detectedGaps = listOf("iron")),
        )

        assertTrue(result is AiSummaryResult.Fallback)
        val fallback = (result as AiSummaryResult.Fallback).fallback
        assertEquals(listOf("iron"), fallback.localNutritionGaps)
    }

    @Test
    fun `invalid json returns fallback and keeps urgent state`() {
        val result = parser.parse(
            rawResponse = "not json",
            request = baseRequest(
                redFlagDetectedByApp = true,
                redFlagReasons = listOf("Severe headache was logged."),
            ),
        )

        assertTrue(result is AiSummaryResult.Fallback)
        val fallback = (result as AiSummaryResult.Fallback).fallback
        assertTrue(fallback.urgentWarning)
        assertEquals(listOf("Severe headache was logged."), fallback.urgentReasons)
    }

    @Test
    fun `nutrition gap missing status defaults to unknown`() {
        val result = parser.parse(
            responseWithNutritionGap("""
                {
                  "nutrientKey": "ironMg",
                  "displayName": "Iron",
                  "explanation": "Needs attention.",
                  "foodSuggestions": ["beans"],
                  "safetyNote": "Ask your gynecologist about supplement changes."
                }
            """.trimIndent()),
            baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
        val gap = (result as AiSummaryResult.Success).response.nutritionGaps.first()
        assertEquals("unknown", gap.status)
        assertEquals("Needs attention.", gap.explanation)
    }

    @Test
    fun `nutrition gap missing explanation uses safe generic explanation`() {
        val result = parser.parse(
            responseWithNutritionGap("""
                {
                  "nutrientKey": "folateMcg",
                  "displayName": "Folate",
                  "status": "low",
                  "foodSuggestions": ["spinach"]
                }
            """.trimIndent()),
            baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
        val gap = (result as AiSummaryResult.Success).response.nutritionGaps.first()
        assertEquals("low", gap.status)
        assertEquals(GENERIC_NUTRITION_GAP_EXPLANATION, gap.explanation)
        assertEquals(DEFAULT_NUTRITION_GAP_SAFETY_NOTE, gap.safetyNote)
    }

    @Test
    fun `nutrition gap string converts to safe gap object`() {
        val result = parser.parse(
            validJson(nutritionGapsJson = "[\"Iron\"]"),
            baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
        val gap = (result as AiSummaryResult.Success).response.nutritionGaps.first()
        assertEquals("iron", gap.nutrientKey)
        assertEquals("Iron", gap.displayName)
        assertEquals("unknown", gap.status)
        assertEquals(GENERIC_NUTRITION_GAP_EXPLANATION, gap.explanation)
    }

    @Test
    fun `nutrition estimates raw numbers convert to estimate objects`() {
        val result = parser.parse(
            validJson(
                nutritionEstimatesJson = """
                    {
                      "proteinGrams": 72.5,
                      "ironMg": 14
                    }
                """.trimIndent(),
            ),
            baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
        val response = (result as AiSummaryResult.Success).response
        assertEquals(72.5, response.nutritionEstimates.proteinGrams.value, 0.001)
        assertEquals("low", response.nutritionEstimates.proteinGrams.confidence)
        assertEquals(DEFAULT_AI_ESTIMATE_EXPLANATION, response.nutritionEstimates.ironMg.explanation)
        assertEquals(AiNutritionEstimateSource.MixedAiLocal, response.nutritionEstimateSource)
    }

    @Test
    fun `nutrition estimates array with keys converts to estimate objects`() {
        val result = parser.parse(
            validJson(
                nutritionEstimatesJson = """
                    [
                      { "key": "proteinGrams", "value": 72.5, "confidence": "medium", "explanation": "Estimated from logged protein foods." },
                      { "key": "ironMg", "value": 10, "confidence": "medium", "explanation": "Estimated from eggs and lentils." }
                    ]
                """.trimIndent(),
            ),
            baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
        val response = (result as AiSummaryResult.Success).response
        assertEquals(72.5, response.nutritionEstimates.proteinGrams.value, 0.001)
        assertEquals("medium", response.nutritionEstimates.proteinGrams.confidence)
        assertEquals(10.0, response.nutritionEstimates.ironMg.value, 0.001)
        assertEquals(AiNutritionEstimateSource.MixedAiLocal, response.nutritionEstimateSource)
    }

    @Test
    fun `response wrapped in markdown fences parses`() {
        val result = parser.parse(
            rawResponse = """
                ```json
                ${validJson()}
                ```
            """.trimIndent(),
            request = baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
    }

    @Test
    fun `response with extra prose before and after json parses`() {
        val result = parser.parse(
            rawResponse = "Here is the JSON response: ${validJson()} Hope this helps.",
            request = baseRequest(),
        )

        assertTrue(result is AiSummaryResult.Success)
        assertEquals("Short user-friendly summary.", (result as AiSummaryResult.Success).response.summary)
    }

    @Test
    fun `partial AI estimates merge with local estimates`() {
        val result = parser.parse(
            validJson(
                nutritionEstimatesJson = """
                    {
                      "proteinGrams": { "value": 72.5, "confidence": "medium", "explanation": "Estimated from logged protein foods." }
                    }
                """.trimIndent(),
            ),
            baseRequest(
                nutritionTotals = AiNutrientPayload(
                    calories = 1800.0,
                    caloriesKcal = 1800.0,
                    proteinGrams = 50.0,
                    fiberGrams = 20.0,
                    ironMg = 8.0,
                ),
            ),
        )

        assertTrue(result is AiSummaryResult.Success)
        val response = (result as AiSummaryResult.Success).response
        assertEquals(AiNutritionEstimateSource.MixedAiLocal, response.nutritionEstimateSource)
        assertEquals(72.5, response.nutritionEstimates.proteinGrams.value, 0.001)
        assertEquals("ai", response.nutritionEstimates.proteinGrams.source)
        assertEquals(1800.0, response.nutritionEstimates.caloriesKcal.value, 0.001)
        assertEquals("local", response.nutritionEstimates.caloriesKcal.source)
        assertEquals("Some values were estimated locally because the AI response was incomplete.", response.nutritionEstimateNote)
    }

    private fun baseRequest(
        redFlagDetectedByApp: Boolean = false,
        redFlagReasons: List<String> = emptyList(),
        detectedGaps: List<String> = emptyList(),
        nutritionTotals: AiNutrientPayload = AiNutrientPayload(),
    ): AiSummaryRequest = AiSummaryRequest(
        requestType = AiRequestType.DailyNutritionSummary,
        date = "2026-06-25",
        pregnancyWeek = 12,
        trimester = 1,
        redFlagDetectedByApp = redFlagDetectedByApp,
        redFlagReasons = redFlagReasons,
        detectedGaps = detectedGaps,
                nutritionTotals = nutritionTotals,
    )

        private fun responseWithNutritionGap(gapJson: String): String = validJson(nutritionGapsJson = "[$gapJson]")

    private fun validJson(
        summary: String = "Short user-friendly summary.",
        urgentWarning: Boolean = false,
        urgentReasons: List<String> = emptyList(),
        disclaimer: String = AiPromptGuardrails.DISCLAIMER,
                nutritionGapsJson: String = """
                        [
                            {
                                "nutrientKey": "ironMg",
                                "displayName": "Iron",
                                "status": "low",
                                "explanation": "Iron needs often increase during pregnancy.",
                                "foodSuggestions": ["lentils", "beans"],
                                "safetyNote": "Confirm supplement changes with your gynecologist."
                            }
                        ]
                """.trimIndent(),
                nutritionEstimatesJson: String = """
                        {
                            "caloriesKcal": { "value": 1800, "confidence": "medium", "explanation": "Estimated from logged foods." },
                            "proteinGrams": { "value": 72.5, "confidence": "medium", "explanation": "Estimated from eggs, yogurt, chicken, and lentils logged today." },
                            "carbsGrams": { "value": 210, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "fatGrams": { "value": 65, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "fiberGrams": { "value": 26, "confidence": "medium", "explanation": "Estimated from logged foods." },
                            "folateMcg": { "value": 450, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "ironMg": { "value": 15, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "calciumMg": { "value": 900, "confidence": "medium", "explanation": "Estimated from logged foods." },
                            "vitaminDMcg": { "value": 6, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "vitaminB12Mcg": { "value": 2.4, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "iodineMcg": { "value": 120, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "omega3Mg": { "value": 300, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "cholineMg": { "value": 360, "confidence": "low", "explanation": "Estimated from logged foods." },
                            "waterMl": { "value": 1800, "confidence": "low", "explanation": "Estimated from logged drinks." }
                        }
                """.trimIndent(),
    ): String {
        val urgentReasonsJson = urgentReasons.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        return """
            {
              "summary": "$summary",
              "stageContext": "Nutrition needs can change by trimester.",
                            "nutritionEstimates": $nutritionEstimatesJson,
                            "nutritionGaps": $nutritionGapsJson,
                            "recommendations": ["Include an iron-rich food"],
                            "safetyWarnings": ["This is educational guidance only."],
              "symptomGuidance": {
                "severity": "none",
                "commonContext": "Educational explanation only.",
                "selfCare": ["Hydration"],
                "contactDoctorIf": ["Symptoms become severe"]
              },
              "weightContext": {
                "summary": "Weight changes can vary during pregnancy.",
                "doctorDiscussionRecommended": false
              },
              "urgentWarning": $urgentWarning,
              "urgentReasons": $urgentReasonsJson,
              "nextSteps": ["Include an iron-rich food"],
              "disclaimer": "$disclaimer"
            }
        """.trimIndent()
    }
}
