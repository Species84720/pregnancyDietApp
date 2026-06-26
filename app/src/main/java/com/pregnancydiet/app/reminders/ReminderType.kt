package com.pregnancydiet.app.reminders

enum class ReminderType(
    val notificationIdBase: Int,
    val displayName: String,
) {
    Supplement(10_000, "Supplement reminder"),
    Meal(20_000, "Meal logging reminder"),
    Symptom(30_000, "Symptom check-in"),
}
