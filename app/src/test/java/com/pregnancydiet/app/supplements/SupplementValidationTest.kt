package com.pregnancydiet.app.supplements

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupplementValidationTest {
    @Test
    fun `valid supplement input is accepted`() {
        val result = SupplementValidation.validate(
            SupplementFormState(
                name = "Folic Acid",
                dose = "400 mcg",
                frequency = "daily",
                timeOfDay = "09:00",
                prescribedBy = "Gynecologist",
                startDate = "2026-06-25",
                endDate = "2026-07-25",
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals("Folic Acid", result.getOrThrow().name)
        assertEquals("09:00", result.getOrThrow().timeOfDay)
    }

    @Test
    fun `end date before start date is rejected`() {
        val result = SupplementValidation.validate(
            SupplementFormState(
                name = "Iron",
                dose = "as prescribed",
                frequency = "daily",
                timeOfDay = "20:00",
                prescribedBy = "Gynecologist",
                startDate = "2026-06-25",
                endDate = "2026-06-24",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("End date cannot be before start date.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `invalid time format is rejected`() {
        val result = SupplementValidation.validate(
            SupplementFormState(
                name = "Vitamin D",
                dose = "as prescribed",
                frequency = "daily",
                timeOfDay = "9 am",
                prescribedBy = "Gynecologist",
                startDate = "2026-06-25",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Time of day must use HH:MM, for example 09:00.", result.exceptionOrNull()?.message)
    }
}
