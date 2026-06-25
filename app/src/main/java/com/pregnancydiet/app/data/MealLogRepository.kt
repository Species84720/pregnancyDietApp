package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import java.time.LocalDate

data class MealLoggingContext(
    val pregnancyProfileId: String?,
    val progress: PregnancyProgress,
)

interface MealLogRepository {
    suspend fun loadLoggingContext(uid: String): Result<MealLoggingContext>

    suspend fun loadMealLogsForDate(uid: String, date: LocalDate = LocalDate.now()): Result<List<MealLog>>

    suspend fun saveMealLog(uid: String, mealLog: MealLog): Result<MealLog>

    suspend fun deleteMealLog(uid: String, mealId: String): Result<Unit>
}
