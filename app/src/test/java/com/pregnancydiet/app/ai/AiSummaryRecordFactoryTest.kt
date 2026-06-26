package com.pregnancydiet.app.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AiSummaryRecordFactoryTest {
    @Test
    fun `success result becomes displayable saved record`() {
        val request = baseRequest()
        val result = AiSummaryResult.Success(
            AiSummaryResponse(
                summary = "Educational summary",
                stageContext = "Week-aware context",
                urgentWarning = false,
                disclaimer = AiPromptGuardrails.DISCLAIMER,
            ),
        )

        val record = AiSummaryRecordFactory.fromResult(
            request = request,
            result = result,
            pregnancyProfileId = "profile-1",
        )

        assertEquals(AiRequestType.DailyNutritionSummary, record.requestType)
        assertEquals("profile-1", record.pregnancyProfileId)
        assertEquals("Educational summary", record.summary)
        assertFalse(record.fallback)
    }

    @Test
    fun `fallback result keeps urgent warning and hides unsafe output`() {
        val request = baseRequest(
            redFlagDetectedByApp = true,
            redFlagReasons = listOf("Severe headache was logged."),
            detectedGaps = listOf("iron"),
        )
        val result = AiPromptGuardrails.fallbackFor(request, "AI response contained unsafe medical guidance.")

        val record = AiSummaryRecordFactory.fromResult(
            request = request,
            result = result,
            pregnancyProfileId = "profile-1",
        )

        assertTrue(record.fallback)
        assertTrue(record.urgentWarning)
        assertEquals(listOf("Severe headache was logged."), record.urgentReasons)
        assertEquals(AiPromptGuardrails.FALLBACK_MESSAGE, record.summary)
        assertEquals("AI response contained unsafe medical guidance.", record.fallbackReason)
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
}
