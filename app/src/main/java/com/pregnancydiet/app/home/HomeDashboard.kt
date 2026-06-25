package com.pregnancydiet.app.home

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import java.time.LocalDate

data class HomeDashboard(
    val pregnancyProfile: PregnancyProfile,
    val progress: PregnancyProgress,
    val countdownDays: Long?,
    val today: LocalDate,
    val todaySupplementStatus: String = "No supplement status yet.",
)

data class DashboardPlaceholderCard(
    val title: String,
    val status: String,
)