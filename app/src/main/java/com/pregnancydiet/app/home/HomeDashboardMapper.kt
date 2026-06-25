package com.pregnancydiet.app.home

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

object HomeDashboardMapper {
    fun createDashboard(
        pregnancyProfile: PregnancyProfile,
        progress: PregnancyProgress,
        today: LocalDate = LocalDate.now(),
        todaySupplementStatus: String = "No supplement status yet.",
    ): HomeDashboard = HomeDashboard(
        pregnancyProfile = pregnancyProfile,
        progress = progress,
        countdownDays = progress.estimatedDueDate?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) },
        today = today,
        todaySupplementStatus = todaySupplementStatus,
    )

    fun currentWeightLabel(profile: PregnancyProfile): String = "%.1f kg".format(Locale.US, profile.currentWeightKg)

    fun countdownLabel(countdownDays: Long?): String = when (countdownDays) {
        null -> "Add or confirm a due date for countdown tracking."
        0L -> "Due date is today. Contact your maternity care team for guidance."
        1L -> "1 day until estimated due date"
        else -> "$countdownDays days until estimated due date"
    }

    fun placeholderCards(dashboard: HomeDashboard? = null): List<DashboardPlaceholderCard> = listOf(
        DashboardPlaceholderCard("Today's symptoms", "No symptom logs yet."),
        DashboardPlaceholderCard("Today's meals", "No meals logged yet."),
        DashboardPlaceholderCard("Today's supplements", dashboard?.todaySupplementStatus ?: "No supplement status yet."),
        DashboardPlaceholderCard("Nutrition status", "Nutrition summary will appear after meal logging."),
    )
}