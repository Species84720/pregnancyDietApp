package com.pregnancydiet.app.reminders

import com.pregnancydiet.app.model.ReminderPreferences
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulePlannerTest {
    @Test
    fun `disabled preferences schedule no reminders`() {
        val plan = ReminderSchedulePlanner.plan(
            preferences = ReminderPreferences(),
            supplements = listOf(activeSupplement()),
        )

        assertTrue(plan.requests.isEmpty())
        assertEquals(0, plan.skippedCount)
    }

    @Test
    fun `supplement reminders use active supplement times`() {
        val plan = ReminderSchedulePlanner.plan(
            preferences = ReminderPreferences(supplementRemindersEnabled = true),
            supplements = listOf(
                activeSupplement(id = "folic", timeOfDay = "09:00"),
                activeSupplement(id = "iron", timeOfDay = "20:00"),
            ),
        )

        assertEquals(2, plan.requests.size)
        assertEquals(listOf("09:00", "20:00"), plan.requests.map { it.timeOfDay })
        assertTrue(plan.requests.all { it.type == ReminderType.Supplement })
    }

    @Test
    fun `invalid supplement time is skipped without scheduling spam`() {
        val plan = ReminderSchedulePlanner.plan(
            preferences = ReminderPreferences(supplementRemindersEnabled = true),
            supplements = listOf(activeSupplement(timeOfDay = "morning")),
        )

        assertTrue(plan.requests.isEmpty())
        assertEquals(1, plan.skippedCount)
    }

    @Test
    fun `meal and symptom reminders create one request each`() {
        val plan = ReminderSchedulePlanner.plan(
            preferences = ReminderPreferences(
                mealRemindersEnabled = true,
                symptomCheckInEnabled = true,
                mealReminderTime = "12:30",
                symptomReminderTime = "20:00",
            ),
            supplements = emptyList(),
        )

        assertEquals(2, plan.requests.size)
        assertEquals(listOf(ReminderType.Meal, ReminderType.Symptom), plan.requests.map { it.type })
    }

    private fun activeSupplement(
        id: String = "supplement-1",
        timeOfDay: String = "09:00",
    ): SupplementWithTodayStatus = SupplementWithTodayStatus(
        supplement = Supplement(
            id = id,
            name = "Folic Acid",
            dose = "as prescribed",
            frequency = "daily",
            timeOfDay = timeOfDay,
            prescribedBy = "Gynecologist",
            instructions = "Take after breakfast",
            startDate = "2026-06-25",
            endDate = null,
            active = true,
        ),
        todayLog = null,
    )
}
