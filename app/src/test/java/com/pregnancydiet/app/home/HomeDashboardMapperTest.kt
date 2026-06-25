package com.pregnancydiet.app.home

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyDatingMethod
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class HomeDashboardMapperTest {
    @Test
    fun `dashboard includes non-negative due date countdown`() {
        val dashboard = HomeDashboardMapper.createDashboard(
            pregnancyProfile = testProfile(),
            progress = PregnancyProgress(
                pregnancyWeek = 12,
                dayWithinWeek = 3,
                trimester = 1,
                estimatedDueDate = LocalDate.of(2027, 1, 1),
                datingMethod = PregnancyDatingMethod.DoctorDueDate,
                message = null,
            ),
            today = LocalDate.of(2026, 12, 30),
        )

        assertEquals(2L, dashboard.countdownDays)
        assertEquals("2 days until estimated due date", HomeDashboardMapper.countdownLabel(dashboard.countdownDays))
    }

    @Test
    fun `dashboard omits countdown when due date is unavailable`() {
        val dashboard = HomeDashboardMapper.createDashboard(
            pregnancyProfile = testProfile(),
            progress = PregnancyProgress(
                pregnancyWeek = null,
                dayWithinWeek = null,
                trimester = null,
                estimatedDueDate = null,
                datingMethod = PregnancyDatingMethod.InsufficientInformation,
                message = null,
            ),
            today = LocalDate.of(2026, 12, 30),
        )

        assertNull(dashboard.countdownDays)
        assertEquals(
            "Add or confirm a due date for countdown tracking.",
            HomeDashboardMapper.countdownLabel(dashboard.countdownDays),
        )
    }

    @Test
    fun `current weight is displayed in kilograms`() {
        assertEquals("70.5 kg", HomeDashboardMapper.currentWeightLabel(testProfile(currentWeightKg = 70.5)))
    }

    private fun testProfile(currentWeightKg: Double = 70.0) = PregnancyProfile(
        id = "profile_123",
        dateFoundOut = "2026-06-25",
        lastMenstrualPeriod = "2026-04-01",
        estimatedDueDate = "2027-01-01",
        doctorConfirmedWeek = null,
        pregnancyType = PregnancyType.Singleton,
        heightCm = 165.0,
        prePregnancyWeightKg = 68.0,
        currentWeightKg = currentWeightKg,
        weightUnit = WeightUnit.Kg,
        allergies = emptyList(),
        dietaryRestrictions = emptyList(),
        medicalConditions = emptyList(),
    )
}