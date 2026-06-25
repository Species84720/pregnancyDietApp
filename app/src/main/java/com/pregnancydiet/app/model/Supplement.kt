package com.pregnancydiet.app.model

data class Supplement(
    val id: String,
    val name: String,
    val dose: String,
    val frequency: String,
    val timeOfDay: String,
    val prescribedBy: String,
    val instructions: String,
    val startDate: String,
    val endDate: String?,
    val active: Boolean = true,
)

data class SupplementLog(
    val id: String,
    val supplementId: String,
    val date: String,
    val taken: Boolean,
    val takenAt: String?,
    val notes: String,
)

data class SupplementWithTodayStatus(
    val supplement: Supplement,
    val todayLog: SupplementLog?,
) {
    val isTakenToday: Boolean = todayLog?.taken == true
}
