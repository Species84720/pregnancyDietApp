package com.pregnancydiet.app.supplements

import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SupplementStatusMapperTest {
    @Test
    fun `empty active supplements show empty status`() {
        assertEquals("No active supplements added yet.", SupplementStatusMapper.todayStatus(emptyList()))
    }

    @Test
    fun `partial taken status counts active supplements`() {
        val items = listOf(
            SupplementWithTodayStatus(supplement("1", active = true), takenLog("1")),
            SupplementWithTodayStatus(supplement("2", active = true), null),
            SupplementWithTodayStatus(supplement("3", active = false), takenLog("3")),
        )

        assertEquals("1 of 2 marked taken today.", SupplementStatusMapper.todayStatus(items))
    }

    @Test
    fun `all taken status counts active supplements`() {
        val items = listOf(
            SupplementWithTodayStatus(supplement("1", active = true), takenLog("1")),
            SupplementWithTodayStatus(supplement("2", active = true), takenLog("2")),
        )

        assertEquals("All 2 active supplements marked taken today.", SupplementStatusMapper.todayStatus(items))
    }

    private fun supplement(id: String, active: Boolean) = Supplement(
        id = id,
        name = "Supplement $id",
        dose = "as prescribed",
        frequency = "daily",
        timeOfDay = "09:00",
        prescribedBy = "Gynecologist",
        instructions = "",
        startDate = "2026-06-25",
        endDate = null,
        active = active,
    )

    private fun takenLog(supplementId: String) = SupplementLog(
        id = "${supplementId}_2026-06-25",
        supplementId = supplementId,
        date = "2026-06-25",
        taken = true,
        takenAt = "2026-06-25T09:00:00",
        notes = "",
    )
}
