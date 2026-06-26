package com.pregnancydiet.app.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingValidationTest {
    @Test
    fun `valid onboarding form is accepted`() {
        val result = OnboardingValidation.validate(
            OnboardingFormState(
                dateFoundOut = "2026-06-01",
                lastMenstrualPeriod = "2026-05-01",
                currentWeight = "70",
                heightCm = "165",
            ),
        )

        assertTrue(result.isSuccess)
        assertEquals(70.0, result.getOrThrow().currentWeightKg, 0.001)
    }

    @Test
    fun `non finite current weight is rejected`() {
        val result = OnboardingValidation.validate(
            OnboardingFormState(
                dateFoundOut = "2026-06-01",
                currentWeight = "Infinity",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Current weight should be between 30 kg and 300 kg.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `unrealistic height is rejected`() {
        val result = OnboardingValidation.validate(
            OnboardingFormState(
                dateFoundOut = "2026-06-01",
                currentWeight = "70",
                heightCm = "50",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Height should be between 90 cm and 250 cm.", result.exceptionOrNull()?.message)
    }
}