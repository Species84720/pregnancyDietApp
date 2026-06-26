package com.pregnancydiet.app.ai

import java.util.UUID

data class AiUsageState(
    val selectedAccessMode: AiAccessMode = AiAccessMode.FREE_HOURLY,
    val lastRequestAtMillis: Long? = null,
    val nextEstimatedFreeRequestAtMillis: Long? = null,
    val requestsInCurrentHour: Int = 0,
    val successfulRequestsToday: Int = 0,
    val failedRateLimitCountToday: Int = 0,
    val quotaExceededCountToday: Int = 0,
    val lastStatus: AiUsageStatus = AiUsageStatus.UNKNOWN,
    val lastErrorMessage: String? = null,
    val usingUserAccount: Boolean = false,
    val userAccountStatus: UserAccountStatus = UserAccountStatus.NOT_CONNECTED,
    val estimatedFreePollenPerHour: Int = 1,
    val dayStartMillis: Long = TimeBuckets.startOfLocalDay(System.currentTimeMillis()),
    val hourWindowStartMillis: Long? = null,
)

enum class AiUsageStatus {
    AVAILABLE,
    COOLING_DOWN,
    RATE_LIMITED,
    QUOTA_EXCEEDED,
    SETUP_REQUIRED,
    NETWORK_ERROR,
    UNKNOWN,
}

enum class UserAccountStatus {
    NOT_CONNECTED,
    CONNECTED,
    INVALID,
    QUOTA_EXCEEDED,
    UNKNOWN,
}

data class AiUsageEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestampMillis: Long = System.currentTimeMillis(),
    val feature: AiFeature,
    val status: AiUsageEventStatus,
    val estimatedPollenCost: Int,
    val retryAfterMillis: Long?,
    val shortMessage: String,
)

enum class AiFeature {
    SYMPTOM_ANALYSIS,
    DIET_PLAN,
    DAILY_ADVICE,
    MEDICATION_SUPPLEMENT_CHECK,
}

enum class AiUsageEventStatus {
    SUCCESS,
    RATE_LIMITED,
    QUOTA_EXCEEDED,
    UNAUTHORIZED,
    NETWORK_ERROR,
    INVALID_RESPONSE,
    SETUP_REQUIRED,
    SAFETY_BLOCKED,
}

sealed class AiAvailability {
    data object Available : AiAvailability()
    data class CoolingDown(val nextAvailableAtMillis: Long, val message: String) : AiAvailability()
    data class SetupRequired(val message: String) : AiAvailability()
    data class AccountInvalid(val message: String) : AiAvailability()
    data class QuotaExceeded(val message: String) : AiAvailability()
    data class Unknown(val message: String) : AiAvailability()
}

internal object TimeBuckets {
    const val ONE_HOUR_MILLIS: Long = 60 * 60 * 1000L

    fun startOfLocalDay(nowMillis: Long): Long {
        val zone = java.time.ZoneId.systemDefault()
        return java.time.Instant.ofEpochMilli(nowMillis)
            .atZone(zone)
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
    }
}