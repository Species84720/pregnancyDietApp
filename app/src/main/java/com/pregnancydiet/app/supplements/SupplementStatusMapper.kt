package com.pregnancydiet.app.supplements

import com.pregnancydiet.app.model.SupplementWithTodayStatus

object SupplementStatusMapper {
    fun todayStatus(items: List<SupplementWithTodayStatus>): String {
        val activeItems = items.filter { it.supplement.active }
        if (activeItems.isEmpty()) return "No active supplements added yet."
        val takenCount = activeItems.count { it.isTakenToday }
        return when {
            takenCount == 0 -> "0 of ${activeItems.size} marked taken today."
            takenCount == activeItems.size -> "All ${activeItems.size} active supplements marked taken today."
            else -> "$takenCount of ${activeItems.size} marked taken today."
        }
    }
}
