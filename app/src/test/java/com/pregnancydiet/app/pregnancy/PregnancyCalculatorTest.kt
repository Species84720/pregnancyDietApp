package com.pregnancydiet.app.pregnancy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PregnancyCalculatorTest {
    private val calculator = PregnancyCalculator()
    private val today = LocalDate.of(2026, 6, 25)

    @Test
    fun `doctor provided estimated due date takes priority over LMP`() {
        val result = calculator.calculate(
            input = PregnancyDatingInput(
                dateFoundOut = LocalDate.of(2026, 6, 20),
                lastMenstrualPeriod = LocalDate.of(2026, 4, 1),
                estimatedDueDate = LocalDate.of(2027, 1, 8),
                doctorConfirmedWeek = null,
            ),
            today = today,
        )

        assertEquals(PregnancyDatingMethod.DoctorDueDate, result.datingMethod)
        assertEquals(LocalDate.of(2027, 1, 8), result.estimatedDueDate)
        assertEquals(12, result.pregnancyWeek)
        assertEquals(1, result.trimester)
    }

    @Test
    fun `last menstrual period estimates due date and trimester`() {
        val result = calculator.calculate(
            input = PregnancyDatingInput(
                dateFoundOut = LocalDate.of(2026, 6, 1),
                lastMenstrualPeriod = LocalDate.of(2026, 3, 19),
                estimatedDueDate = null,
                doctorConfirmedWeek = null,
            ),
            today = today,
        )

        assertEquals(PregnancyDatingMethod.LastMenstrualPeriod, result.datingMethod)
        assertEquals(LocalDate.of(2026, 12, 24), result.estimatedDueDate)
        assertEquals(15, result.pregnancyWeek)
        assertEquals(2, result.trimester)
        assertTrue(result.hasAccurateDating)
    }

    @Test
    fun `doctor confirmed week calculates trimester when dates are unavailable`() {
        val result = calculator.calculate(
            input = PregnancyDatingInput(
                dateFoundOut = LocalDate.of(2026, 6, 1),
                lastMenstrualPeriod = null,
                estimatedDueDate = null,
                doctorConfirmedWeek = 29,
            ),
            today = today,
        )

        assertEquals(PregnancyDatingMethod.DoctorConfirmedWeek, result.datingMethod)
        assertEquals(29, result.pregnancyWeek)
        assertEquals(3, result.trimester)
        assertTrue(result.hasAccurateDating)
    }

    @Test
    fun `date found out alone is not enough for accurate dating`() {
        val result = calculator.calculate(
            input = PregnancyDatingInput(
                dateFoundOut = LocalDate.of(2026, 6, 1),
                lastMenstrualPeriod = null,
                estimatedDueDate = null,
                doctorConfirmedWeek = null,
            ),
            today = today,
        )

        assertEquals(PregnancyDatingMethod.InsufficientInformation, result.datingMethod)
        assertFalse(result.hasAccurateDating)
        assertTrue(result.message.orEmpty().contains("not enough"))
    }
}