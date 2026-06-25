package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress

data class SymptomLoggingContext(
    val pregnancyProfileId: String?,
    val progress: PregnancyProgress,
)

interface SymptomLogRepository {
    suspend fun loadLoggingContext(uid: String): Result<SymptomLoggingContext>

    suspend fun saveSymptomLog(uid: String, symptomLog: SymptomLog): Result<SymptomLog>

    suspend fun loadRecentSymptomLogs(uid: String, limit: Long = 20): Result<List<SymptomLog>>
}
