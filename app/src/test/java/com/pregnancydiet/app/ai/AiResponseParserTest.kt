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
        assertEquals("iron", response.nutritionGaps.first().nutrient)
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

    private fun baseRequest(
        redFlagDetectedByApp: Boolean = false,
        redFlagReasons: List<String> = emptyList(),
        detectedGaps: List<String> = emptyList(),
    ): AiSummaryRequest = AiSummaryRequest(
        requestType = AiRequestType.DailyNutritionSummary,
        date = "2026-06-25",
        pregnancyWeek = 12,
        trimester = 1,
        redFlagDetectedByApp = redFlagDetectedByApp,
        redFlagReasons = redFlagReasons,
        detectedGaps = detectedGaps,
    )

    private fun validJson(
        summary: String = "Short user-friendly summary.",
        urgentWarning: Boolean = false,
        urgentReasons: List<String> = emptyList(),
        disclaimer: String = AiPromptGuardrails.DISCLAIMER,
    ): String {
        val urgentReasonsJson = urgentReasons.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }
        return """
            {
              "summary": "$summary",
              "stageContext": "Nutrition needs can change by trimester.",
              "nutritionGaps": [
                {
                  "nutrient": "iron",
                  "status": "low",
                  "explanation": "Iron needs often increase during pregnancy.",
                  "foodSuggestions": ["lentils", "beans"],
                  "safetyNote": "Confirm supplement changes with your gynecologist."
                }
              ],
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
