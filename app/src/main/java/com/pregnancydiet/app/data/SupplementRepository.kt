package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import java.time.LocalDate

interface SupplementRepository {
    suspend fun loadSupplementsForDate(uid: String, date: LocalDate = LocalDate.now()): Result<List<SupplementWithTodayStatus>>

    suspend fun saveSupplement(uid: String, supplement: Supplement): Result<Supplement>

    suspend fun deactivateSupplement(uid: String, supplementId: String): Result<Unit>

    suspend fun markSupplementTaken(
        uid: String,
        supplementId: String,
        date: LocalDate = LocalDate.now(),
        notes: String = "",
    ): Result<SupplementLog>
}
