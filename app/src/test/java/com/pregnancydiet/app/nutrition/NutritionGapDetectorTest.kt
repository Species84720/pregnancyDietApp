package com.pregnancydiet.app.nutrition

import com.pregnancydiet.app.model.GapSeverity
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.NutritionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionGapDetectorTest {
    @Test
    fun `low nutrient gap is detected`() {
        val gaps = NutritionGapDetector.detect(
            totals = NutrientAmounts(proteinGrams = 30.0, waterMl = 1000.0),
            targets = NutrientAmounts(proteinGrams = 75.0, waterMl = 2300.0),
        )

        val proteinGap = gaps.first { it.nutrient == "proteinGrams" }
        assertEquals(NutritionStatus.Low, proteinGap.status)
        assertEquals(GapSeverity.High, proteinGap.severity)
    }

    @Test
    fun `adequate nutrients are omitted from gaps`() {
        val gaps = NutritionGapDetector.detect(
            totals = NutrientAmounts(proteinGrams = 70.0),
            targets = NutrientAmounts(proteinGrams = 75.0),
        )

        assertTrue(gaps.none { it.nutrient == "proteinGrams" })
    }

    @Test
    fun `high nutrient status is detected`() {
        val gaps = NutritionGapDetector.detect(
            totals = NutrientAmounts(ironMg = 60.0),
            targets = NutrientAmounts(ironMg = 27.0),
        )

        val ironGap = gaps.first { it.nutrient == "ironMg" }
        assertEquals(NutritionStatus.High, ironGap.status)
    }
}
