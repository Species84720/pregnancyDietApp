package com.pregnancydiet.app.pregnancy

import java.time.LocalDate

data class PregnancyDatingInput(
    val dateFoundOut: LocalDate,
    val lastMenstrualPeriod: LocalDate?,
    val estimatedDueDate: LocalDate?,
    val doctorConfirmedWeek: Int?,
)