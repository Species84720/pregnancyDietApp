package com.pregnancydiet.app.reminders

import com.pregnancydiet.app.model.ReminderPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderTimeValidationTest {
    @Test
    fun `enabled meal reminder requires valid time`() {
        val result = ReminderTimeValidation.validate(
            ReminderPreferences(
                mealRemindersEnabled = true,
                mealReminderTime = "lunch",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Meal reminder time must use HH:MM, for example 12:30.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `enabled symptom reminder requires valid time`() {
        val result = ReminderTimeValidation.validate(
            ReminderPreferences(
                symptomCheckInEnabled = true,
                symptomReminderTime = "8 pm",
            ),
        )

        assertTrue(result.isFailure)
        assertEquals("Symptom check-in time must use HH:MM, for example 20:00.", result.exceptionOrNull()?.message)
    }

    @Test
    fun `disabled reminders do not validate time fields`() {
        val result = ReminderTimeValidation.validate(
            ReminderPreferences(
                mealRemindersEnabled = false,
                symptomCheckInEnabled = false,
                mealReminderTime = "lunch",
                symptomReminderTime = "evening",
            ),
        )

        assertTrue(result.isSuccess)
    }
}
