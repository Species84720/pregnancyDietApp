package com.pregnancydiet.app.reminders

import com.pregnancydiet.app.model.Supplement

object ReminderCopy {
    const val CHANNEL_ID = "pregnancy_diet_reminders"
    const val CHANNEL_NAME = "Gentle reminders"
    const val CHANNEL_DESCRIPTION = "Optional reminders for supplements, meals, and symptom check-ins."

    fun supplementTitle(supplement: Supplement): String = "Gentle supplement reminder"

    fun supplementBody(supplement: Supplement): String = buildString {
        append("If this is still part of your care plan, consider marking ")
        append(supplement.name.ifBlank { "your supplement" })
        append(" as taken.")
    }

    fun mealTitle(): String = "Meal logging reminder"

    fun mealBody(): String = "If it helps, take a moment to log a meal or snack from today."

    fun symptomTitle(): String = "Daily check-in"

    fun symptomBody(): String = "If you noticed symptoms today, you can log them when you have a moment."
}
