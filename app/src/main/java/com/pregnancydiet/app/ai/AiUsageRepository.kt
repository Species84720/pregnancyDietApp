package com.pregnancydiet.app.ai

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface AiUsageRepository {
    fun observeUsageState(): Flow<AiUsageState>
    fun observeRecentUsageEvents(limit: Int = 20): Flow<List<AiUsageEvent>>
    suspend fun getUsageState(): AiUsageState
    suspend fun canUseAi(feature: AiFeature): AiAvailability
    suspend fun recordSuccess(feature: AiFeature, accessMode: AiAccessMode)
    suspend fun recordRateLimited(feature: AiFeature, retryAfterMillis: Long?, accessMode: AiAccessMode)
    suspend fun recordQuotaExceeded(feature: AiFeature, accessMode: AiAccessMode)
    suspend fun recordUnauthorized(feature: AiFeature, accessMode: AiAccessMode)
    suspend fun recordNetworkError(feature: AiFeature, message: String, accessMode: AiAccessMode)
    suspend fun recordInvalidResponse(feature: AiFeature, message: String, accessMode: AiAccessMode)
    suspend fun recordSetupRequired(feature: AiFeature, message: String, accessMode: AiAccessMode)
    suspend fun resetDailyCountersIfNeeded(nowMillis: Long)
    suspend fun resetCurrentHourCountersIfNeeded(nowMillis: Long)
}

class LocalAiUsageRepository(
    context: Context,
    private val accessModeRepository: AiAccessModeRepository,
) : InMemoryAiUsageRepository() {
    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    init {
        val state = AiUsageState(
            selectedAccessMode = prefs.getString(KEY_MODE, AiAccessMode.FREE_HOURLY.name)
                ?.let { value -> AiAccessMode.entries.firstOrNull { it.name == value } }
                ?: AiAccessMode.FREE_HOURLY,
            lastRequestAtMillis = prefs.getLongOrNull(KEY_LAST_REQUEST),
            nextEstimatedFreeRequestAtMillis = prefs.getLongOrNull(KEY_NEXT_FREE),
            requestsInCurrentHour = prefs.getInt(KEY_REQUESTS_HOUR, 0),
            successfulRequestsToday = prefs.getInt(KEY_SUCCESS_TODAY, 0),
            failedRateLimitCountToday = prefs.getInt(KEY_RATE_LIMIT_TODAY, 0),
            quotaExceededCountToday = prefs.getInt(KEY_QUOTA_TODAY, 0),
            lastStatus = prefs.getString(KEY_STATUS, AiUsageStatus.UNKNOWN.name)
                ?.let { value -> AiUsageStatus.entries.firstOrNull { it.name == value } }
                ?: AiUsageStatus.UNKNOWN,
            lastErrorMessage = prefs.getString(KEY_ERROR, null),
            usingUserAccount = prefs.getBoolean(KEY_USING_USER, false),
            userAccountStatus = prefs.getString(KEY_ACCOUNT_STATUS, UserAccountStatus.NOT_CONNECTED.name)
                ?.let { value -> UserAccountStatus.entries.firstOrNull { it.name == value } }
                ?: UserAccountStatus.NOT_CONNECTED,
            dayStartMillis = prefs.getLong(KEY_DAY_START, TimeBuckets.startOfLocalDay(System.currentTimeMillis())),
            hourWindowStartMillis = prefs.getLongOrNull(KEY_HOUR_START),
        )
        replaceState(state)
    }

    override suspend fun persistState(state: AiUsageState) {
        prefs.edit()
            .putString(KEY_MODE, state.selectedAccessMode.name)
            .putLongOrRemove(KEY_LAST_REQUEST, state.lastRequestAtMillis)
            .putLongOrRemove(KEY_NEXT_FREE, state.nextEstimatedFreeRequestAtMillis)
            .putInt(KEY_REQUESTS_HOUR, state.requestsInCurrentHour)
            .putInt(KEY_SUCCESS_TODAY, state.successfulRequestsToday)
            .putInt(KEY_RATE_LIMIT_TODAY, state.failedRateLimitCountToday)
            .putInt(KEY_QUOTA_TODAY, state.quotaExceededCountToday)
            .putString(KEY_STATUS, state.lastStatus.name)
            .putString(KEY_ERROR, state.lastErrorMessage)
            .putBoolean(KEY_USING_USER, state.usingUserAccount)
            .putString(KEY_ACCOUNT_STATUS, state.userAccountStatus.name)
            .putLong(KEY_DAY_START, state.dayStartMillis)
            .putLongOrRemove(KEY_HOUR_START, state.hourWindowStartMillis)
            .apply()
    }

    override suspend fun currentAccessMode(): AiAccessMode = accessModeRepository.getAccessMode()

    private companion object {
        const val PREFS = "ai_usage_state"
        const val KEY_MODE = "mode"
        const val KEY_LAST_REQUEST = "last_request"
        const val KEY_NEXT_FREE = "next_free"
        const val KEY_REQUESTS_HOUR = "requests_hour"
        const val KEY_SUCCESS_TODAY = "success_today"
        const val KEY_RATE_LIMIT_TODAY = "rate_limit_today"
        const val KEY_QUOTA_TODAY = "quota_today"
        const val KEY_STATUS = "status"
        const val KEY_ERROR = "error"
        const val KEY_USING_USER = "using_user"
        const val KEY_ACCOUNT_STATUS = "account_status"
        const val KEY_DAY_START = "day_start"
        const val KEY_HOUR_START = "hour_start"
    }
}

open class InMemoryAiUsageRepository(
    initialState: AiUsageState = AiUsageState(),
) : AiUsageRepository {
    private val state = MutableStateFlow(initialState)
    private val events = MutableStateFlow<List<AiUsageEvent>>(emptyList())

    override fun observeUsageState(): Flow<AiUsageState> = state.asStateFlow()
    override fun observeRecentUsageEvents(limit: Int): Flow<List<AiUsageEvent>> = events.asStateFlow()
    override suspend fun getUsageState(): AiUsageState = state.value
    protected fun replaceState(next: AiUsageState) {
        state.value = next
    }

    protected open suspend fun currentAccessMode(): AiAccessMode = state.value.selectedAccessMode
    protected open suspend fun persistState(state: AiUsageState) = Unit

    override suspend fun canUseAi(feature: AiFeature): AiAvailability {
        val now = System.currentTimeMillis()
        resetDailyCountersIfNeeded(now)
        resetCurrentHourCountersIfNeeded(now)
        val mode = currentAccessMode()
        val current = state.value.copy(selectedAccessMode = mode, usingUserAccount = mode == AiAccessMode.USER_ACCOUNT)
        state.value = current
        return when {
            current.lastStatus == AiUsageStatus.SETUP_REQUIRED -> AiAvailability.SetupRequired(current.lastErrorMessage ?: "AI setup is required.")
            mode == AiAccessMode.USER_ACCOUNT && current.userAccountStatus == UserAccountStatus.INVALID -> AiAvailability.AccountInvalid("Your Pollinations account connection needs to be updated.")
            mode == AiAccessMode.USER_ACCOUNT && current.userAccountStatus == UserAccountStatus.QUOTA_EXCEEDED -> AiAvailability.QuotaExceeded("Your Pollinations account appears to be out of allowance or rate limited.")
            mode == AiAccessMode.FREE_HOURLY && current.nextEstimatedFreeRequestAtMillis?.let { it > now } == true -> AiAvailability.CoolingDown(
                nextAvailableAtMillis = current.nextEstimatedFreeRequestAtMillis,
                message = "Next estimated free request is available later.",
            )
            else -> AiAvailability.Available
        }
    }

    override suspend fun recordSuccess(feature: AiFeature, accessMode: AiAccessMode) {
        val now = System.currentTimeMillis()
        updateState(
            event = AiUsageEvent(feature = feature, status = AiUsageEventStatus.SUCCESS, estimatedPollenCost = 1, retryAfterMillis = null, shortMessage = "AI request completed."),
        ) { current ->
            current.copy(
                selectedAccessMode = accessMode,
                lastRequestAtMillis = now,
                nextEstimatedFreeRequestAtMillis = if (accessMode == AiAccessMode.FREE_HOURLY) now + TimeBuckets.ONE_HOUR_MILLIS else current.nextEstimatedFreeRequestAtMillis,
                requestsInCurrentHour = current.requestsInCurrentHour + 1,
                successfulRequestsToday = current.successfulRequestsToday + 1,
                lastStatus = AiUsageStatus.AVAILABLE,
                lastErrorMessage = null,
                usingUserAccount = accessMode == AiAccessMode.USER_ACCOUNT,
                userAccountStatus = if (accessMode == AiAccessMode.USER_ACCOUNT) UserAccountStatus.CONNECTED else current.userAccountStatus,
                hourWindowStartMillis = current.hourWindowStartMillis ?: now,
            )
        }
    }

    override suspend fun recordRateLimited(feature: AiFeature, retryAfterMillis: Long?, accessMode: AiAccessMode) {
        val now = System.currentTimeMillis()
        val next = retryAfterMillis ?: (now + TimeBuckets.ONE_HOUR_MILLIS)
        updateState(AiUsageEvent(feature = feature, status = AiUsageEventStatus.RATE_LIMITED, estimatedPollenCost = 0, retryAfterMillis = next, shortMessage = "AI usage is currently limited.")) { current ->
            current.copy(
                selectedAccessMode = accessMode,
                nextEstimatedFreeRequestAtMillis = next,
                failedRateLimitCountToday = current.failedRateLimitCountToday + 1,
                lastStatus = AiUsageStatus.RATE_LIMITED,
                lastErrorMessage = "AI usage is currently limited.",
                usingUserAccount = accessMode == AiAccessMode.USER_ACCOUNT,
            )
        }
    }

    override suspend fun recordQuotaExceeded(feature: AiFeature, accessMode: AiAccessMode) {
        updateState(AiUsageEvent(feature = feature, status = AiUsageEventStatus.QUOTA_EXCEEDED, estimatedPollenCost = 0, retryAfterMillis = null, shortMessage = "AI allowance may be used up.")) { current ->
            current.copy(
                selectedAccessMode = accessMode,
                quotaExceededCountToday = current.quotaExceededCountToday + 1,
                lastStatus = AiUsageStatus.QUOTA_EXCEEDED,
                lastErrorMessage = "AI allowance may be used up.",
                usingUserAccount = accessMode == AiAccessMode.USER_ACCOUNT,
                userAccountStatus = if (accessMode == AiAccessMode.USER_ACCOUNT) UserAccountStatus.QUOTA_EXCEEDED else current.userAccountStatus,
            )
        }
    }

    override suspend fun recordUnauthorized(feature: AiFeature, accessMode: AiAccessMode) {
        updateState(AiUsageEvent(feature = feature, status = AiUsageEventStatus.UNAUTHORIZED, estimatedPollenCost = 0, retryAfterMillis = null, shortMessage = "AI account connection needs to be updated.")) { current ->
            current.copy(
                selectedAccessMode = accessMode,
                lastStatus = AiUsageStatus.SETUP_REQUIRED,
                lastErrorMessage = "AI account connection needs to be updated.",
                usingUserAccount = accessMode == AiAccessMode.USER_ACCOUNT,
                userAccountStatus = if (accessMode == AiAccessMode.USER_ACCOUNT) UserAccountStatus.INVALID else current.userAccountStatus,
            )
        }
    }

    override suspend fun recordNetworkError(feature: AiFeature, message: String, accessMode: AiAccessMode) = recordError(feature, AiUsageEventStatus.NETWORK_ERROR, AiUsageStatus.NETWORK_ERROR, message, accessMode)
    override suspend fun recordInvalidResponse(feature: AiFeature, message: String, accessMode: AiAccessMode) = recordError(feature, AiUsageEventStatus.INVALID_RESPONSE, AiUsageStatus.UNKNOWN, message, accessMode)
    override suspend fun recordSetupRequired(feature: AiFeature, message: String, accessMode: AiAccessMode) = recordError(feature, AiUsageEventStatus.SETUP_REQUIRED, AiUsageStatus.SETUP_REQUIRED, message, accessMode)

    override suspend fun resetDailyCountersIfNeeded(nowMillis: Long) {
        val dayStart = TimeBuckets.startOfLocalDay(nowMillis)
        if (state.value.dayStartMillis < dayStart) {
            updateState { it.copy(successfulRequestsToday = 0, failedRateLimitCountToday = 0, quotaExceededCountToday = 0, dayStartMillis = dayStart) }
        }
    }

    override suspend fun resetCurrentHourCountersIfNeeded(nowMillis: Long) {
        val start = state.value.hourWindowStartMillis ?: return
        if (nowMillis - start >= TimeBuckets.ONE_HOUR_MILLIS) {
            updateState { it.copy(requestsInCurrentHour = 0, hourWindowStartMillis = nowMillis) }
        }
    }

    private suspend fun recordError(feature: AiFeature, eventStatus: AiUsageEventStatus, usageStatus: AiUsageStatus, message: String, accessMode: AiAccessMode) {
        updateState(AiUsageEvent(feature = feature, status = eventStatus, estimatedPollenCost = 0, retryAfterMillis = null, shortMessage = message.safeShortMessage())) { current ->
            current.copy(selectedAccessMode = accessMode, lastStatus = usageStatus, lastErrorMessage = message.safeShortMessage(), usingUserAccount = accessMode == AiAccessMode.USER_ACCOUNT)
        }
    }

    private suspend fun updateState(event: AiUsageEvent? = null, reducer: (AiUsageState) -> AiUsageState) {
        val next = reducer(state.value)
        state.value = next
        persistState(next)
        if (event != null) events.value = (listOf(event) + events.value).take(20)
    }

    private fun String.safeShortMessage(): String = take(140)
}

private fun SharedPreferences.getLongOrNull(key: String): Long? = if (contains(key)) getLong(key, 0L) else null

private fun SharedPreferences.Editor.putLongOrRemove(key: String, value: Long?): SharedPreferences.Editor = if (value == null) remove(key) else putLong(key, value)