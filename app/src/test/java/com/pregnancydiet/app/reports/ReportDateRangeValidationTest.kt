package com.pregnancydiet.app.reports

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportDateRangeValidationTest {
    @Test
    fun `valid range is accepted`() {
        val result = ReportDateRangeValidation.validate(
            startDateText = "2026-06-01",
            endDateText = "2026-06-26",
        )

        assertTrue(result.isSuccess)
        assertEquals("2026-06-01 to 2026-06-26", result.getOrThrow().label)
    }

    @Test
    fun `end before start is rejected`() {
        val result = ReportDateRangeValidation.validate(
            startDateText = "2026-06-26",
            endDateText = "2026-06-01",
        )

        assertTrue(result.isFailure)
        assertEquals("End date cannot be before start date.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invalid date format is rejected`() {
        val result = ReportDateRangeValidation.validate(
            startDateText = "06/01/2026",
            endDateText = "2026-06-26",
        )

        assertTrue(result.isFailure)
        assertEquals("Start date must use YYYY-MM-DD.", result.exceptionOrNull()?.message)
    }
}
