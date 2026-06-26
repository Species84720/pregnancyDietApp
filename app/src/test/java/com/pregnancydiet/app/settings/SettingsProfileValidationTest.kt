package com.pregnancydiet.app.settings

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsProfileValidationTest {
    @Test
    fun `valid settings form updates profile and converts pounds to kilograms`() {
        val form = SettingsFormState(
            pregnancyProfileId = "profile-1",
            dateFoundOut = "2026-06-01",
            lastMenstrualPeriod = "2026-05-01",
            estimatedDueDate = "2027-02-05",
            doctorConfirmedWeek = "8",
            pregnancyType = PregnancyType.Twins,
            heightCm = "165",
            prePregnancyWeight = "150",
            currentWeight = "155",
            weightUnit = WeightUnit.Lb,
            allergies = "peanuts, shellfish",
            dietaryRestrictions = "vegetarian",
            medicalConditions = "gestational diabetes",
        )

        val result = SettingsProfileValidation.validate(form, existingProfile())

        assertTrue(result.isSuccess)
        val profile = result.getOrThrow()
        assertEquals(PregnancyType.Twins, profile.pregnancyType)
        assertEquals(70.3068, profile.currentWeightKg, 0.0001)
        assertEquals(listOf("peanuts", "shellfish"), profile.allergies)
        assertEquals(listOf("vegetarian"), profile.dietaryRestrictions)
        assertEquals(listOf("gestational diabetes"), profile.medicalConditions)
    }

    @Test
    fun `doctor confirmed week must be in pregnancy range`() {
        val result = SettingsProfileValidation.validate(
            SettingsFormState(
                pregnancyProfileId = "profile-1",
                dateFoundOut = "2026-06-01",
                doctorConfirmedWeek = "50",
                currentWeight = "70",
            ),
            existingProfile(),
        )

        assertTrue(result.isFailure)
        assertEquals("Doctor-confirmed week must be between 1 and 42.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `current weight is required`() {
        val result = SettingsProfileValidation.validate(
            SettingsFormState(
                pregnancyProfileId = "profile-1",
                dateFoundOut = "2026-06-01",
                currentWeight = "",
            ),
            existingProfile(),
        )

        assertTrue(result.isFailure)
        assertEquals("Current weight is required.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `non finite current weight is rejected`() {
        val result = SettingsProfileValidation.validate(
            SettingsFormState(
                pregnancyProfileId = "profile-1",
                dateFoundOut = "2026-06-01",
                currentWeight = "Infinity",
            ),
            existingProfile(),
        )

        assertTrue(result.isFailure)
        assertEquals("Current weight should be between 30 kg and 300 kg.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `unrealistic height is rejected`() {
        val result = SettingsProfileValidation.validate(
            SettingsFormState(
                pregnancyProfileId = "profile-1",
                dateFoundOut = "2026-06-01",
                currentWeight = "70",
                heightCm = "300",
            ),
            existingProfile(),
        )

        assertTrue(result.isFailure)
        assertEquals("Height should be between 90 cm and 250 cm.", result.exceptionOrNull()?.message)
    }

    private fun existingProfile(): PregnancyProfile = PregnancyProfile(
        id = "profile-1",
        dateFoundOut = "2026-06-01",
        lastMenstrualPeriod = null,
        estimatedDueDate = null,
        doctorConfirmedWeek = null,
        pregnancyType = PregnancyType.Unknown,
        heightCm = null,
        prePregnancyWeightKg = null,
        currentWeightKg = 70.0,
        weightUnit = WeightUnit.Kg,
        allergies = emptyList(),
        dietaryRestrictions = emptyList(),
        medicalConditions = emptyList(),
    )
}