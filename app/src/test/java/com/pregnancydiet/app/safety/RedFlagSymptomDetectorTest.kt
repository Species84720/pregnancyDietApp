package com.pregnancydiet.app.safety

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RedFlagSymptomDetectorTest {
    @Test
    fun `nausea severity five is saved without urgent warning`() {
        val result = RedFlagSymptomDetector.evaluate(
            SymptomSafetyInput(
                name = "nausea",
                severity = 5,
                duration = "2 hours",
                pregnancyWeek = 8,
            ),
        )

        assertFalse(result.urgentFlag)
    }

    @Test
    fun `bleeding triggers urgent warning`() {
        val result = RedFlagSymptomDetector.evaluate(
            SymptomSafetyInput(
                name = "bleeding",
                severity = 3,
                pregnancyWeek = 10,
            ),
        )

        assertTrue(result.urgentFlag)
        assertTrue(result.urgentReasons.any { it.contains("Bleeding") })
    }

    @Test
    fun `severe headache with vision changes triggers urgent warning`() {
        val result = RedFlagSymptomDetector.evaluate(
            SymptomSafetyInput(
                name = "headache",
                severity = 9,
                notes = "Blurred vision and seeing spots",
                pregnancyWeek = 30,
            ),
        )

        assertTrue(result.urgentFlag)
        assertTrue(result.urgentReasons.any { it.contains("headache", ignoreCase = true) })
        assertTrue(result.urgentReasons.any { it.contains("Vision") })
    }

    @Test
    fun `vomiting with inability to keep fluids triggers urgent warning`() {
        val result = RedFlagSymptomDetector.evaluate(
            SymptomSafetyInput(
                name = "vomiting",
                severity = 6,
                notes = "Unable to keep fluids down since morning",
                pregnancyWeek = 12,
            ),
        )

        assertTrue(result.urgentFlag)
        assertTrue(result.urgentReasons.any { it.contains("fluids") })
    }

    @Test
    fun `reduced fetal movement later in pregnancy triggers urgent warning`() {
        val result = RedFlagSymptomDetector.evaluate(
            SymptomSafetyInput(
                name = "reduced fetal movement",
                severity = 4,
                pregnancyWeek = 28,
            ),
        )

        assertTrue(result.urgentFlag)
        assertTrue(result.urgentReasons.any { it.contains("Reduced fetal movement") })
    }
}
