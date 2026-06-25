package com.pregnancydiet.app.pregnancy

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

class PregnancyCalculator {
    fun calculate(
        input: PregnancyDatingInput,
        today: LocalDate = LocalDate.now(),
    ): PregnancyProgress {
        input.estimatedDueDate?.let { dueDate ->
            return progressFromDueDate(
                dueDate = dueDate,
                today = today,
                datingMethod = PregnancyDatingMethod.DoctorDueDate,
                message = "Using your gynecologist-provided estimated due date.",
            )
        }

        input.lastMenstrualPeriod?.let { lmp ->
            return progressFromDueDate(
                dueDate = lmp.plusDays(PREGNANCY_LENGTH_DAYS.toLong()),
                today = today,
                datingMethod = PregnancyDatingMethod.LastMenstrualPeriod,
                message = "Estimated from last menstrual period. Confirm dating with your gynecologist.",
            )
        }

        input.doctorConfirmedWeek?.let { week ->
            val normalizedWeek = week.coerceIn(1, MAX_PREGNANCY_WEEK)
            return PregnancyProgress(
                pregnancyWeek = normalizedWeek,
                dayWithinWeek = 1,
                trimester = trimesterForWeek(normalizedWeek),
                estimatedDueDate = today.plusDays((PREGNANCY_LENGTH_DAYS - ((normalizedWeek - 1) * DAYS_IN_WEEK)).toLong()),
                datingMethod = PregnancyDatingMethod.DoctorConfirmedWeek,
                message = "Estimated from doctor-confirmed pregnancy week. Add a due date when available for better tracking.",
            )
        }

        return PregnancyProgress(
            pregnancyWeek = null,
            dayWithinWeek = null,
            trimester = null,
            estimatedDueDate = null,
            datingMethod = PregnancyDatingMethod.InsufficientInformation,
            message = "Date found out alone is not enough for accurate pregnancy dating. Add your last menstrual period, estimated due date, or doctor-confirmed week.",
        )
    }

    private fun progressFromDueDate(
        dueDate: LocalDate,
        today: LocalDate,
        datingMethod: PregnancyDatingMethod,
        message: String,
    ): PregnancyProgress {
        val daysUntilDueDate = ChronoUnit.DAYS.between(today, dueDate).toInt()
        val gestationalAgeDays = (PREGNANCY_LENGTH_DAYS - daysUntilDueDate).coerceAtLeast(0)
        val pregnancyWeek = min(MAX_PREGNANCY_WEEK, max(1, (gestationalAgeDays / DAYS_IN_WEEK) + 1))
        val dayWithinWeek = (gestationalAgeDays % DAYS_IN_WEEK) + 1

        return PregnancyProgress(
            pregnancyWeek = pregnancyWeek,
            dayWithinWeek = dayWithinWeek,
            trimester = trimesterForWeek(pregnancyWeek),
            estimatedDueDate = dueDate,
            datingMethod = datingMethod,
            message = message,
        )
    }

    fun trimesterForWeek(week: Int): Int = when {
        week <= 13 -> 1
        week <= 27 -> 2
        else -> 3
    }

    private companion object {
        const val DAYS_IN_WEEK = 7
        const val PREGNANCY_LENGTH_DAYS = 280
        const val MAX_PREGNANCY_WEEK = 42
    }
}