package com.pregnancydiet.app.pregnancy

import java.time.LocalDate

data class PregnancyProgress(
    val pregnancyWeek: Int?,
    val dayWithinWeek: Int?,
    val trimester: Int?,
    val estimatedDueDate: LocalDate?,
    val datingMethod: PregnancyDatingMethod,
    val message: String?,
) {
    val hasAccurateDating: Boolean = pregnancyWeek != null && trimester != null && estimatedDueDate != null
}

enum class PregnancyDatingMethod {
    DoctorDueDate,
    LastMenstrualPeriod,
    DoctorConfirmedWeek,
    InsufficientInformation,
}