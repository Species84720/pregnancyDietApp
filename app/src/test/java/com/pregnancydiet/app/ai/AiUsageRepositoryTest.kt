package com.pregnancydiet.app.ai

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiUsageRepositoryTest {
    @Test
    fun `free hourly success sets next free time`() = runBlocking {
        val repository = InMemoryAiUsageRepository()

        repository.recordSuccess(AiFeature.DAILY_ADVICE, AiAccessMode.FREE_HOURLY)

        val state = repository.getUsageState()
        assertTrue(state.nextEstimatedFreeRequestAtMillis != null)
        assertEquals(1, state.successfulRequestsToday)
    }

    @Test
    fun `user account success does not set free hourly cooldown`() = runBlocking {
        val repository = InMemoryAiUsageRepository()

        repository.recordSuccess(AiFeature.DIET_PLAN, AiAccessMode.USER_ACCOUNT)

        val state = repository.getUsageState()
        assertNull(state.nextEstimatedFreeRequestAtMillis)
        assertEquals(UserAccountStatus.CONNECTED, state.userAccountStatus)
    }

    @Test
    fun `current hour counters reset after one hour`() = runBlocking {
        val repository = InMemoryAiUsageRepository(
            AiUsageState(
                requestsInCurrentHour = 1,
                hourWindowStartMillis = 1_000L,
            ),
        )

        repository.resetCurrentHourCountersIfNeeded(1_000L + TimeBuckets.ONE_HOUR_MILLIS)

        assertEquals(0, repository.getUsageState().requestsInCurrentHour)
    }

    @Test
    fun `daily counters reset at local midnight`() = runBlocking {
        val oldDay = TimeBuckets.startOfLocalDay(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L)
        val repository = InMemoryAiUsageRepository(
            AiUsageState(
                successfulRequestsToday = 3,
                failedRateLimitCountToday = 2,
                quotaExceededCountToday = 1,
                dayStartMillis = oldDay,
            ),
        )

        repository.resetDailyCountersIfNeeded(System.currentTimeMillis())

        val state = repository.getUsageState()
        assertEquals(0, state.successfulRequestsToday)
        assertEquals(0, state.failedRateLimitCountToday)
        assertEquals(0, state.quotaExceededCountToday)
    }

    @Test
    fun `quota exceeded is tracked separately from rate limit`() = runBlocking {
        val repository = InMemoryAiUsageRepository()

        repository.recordQuotaExceeded(AiFeature.DAILY_ADVICE, AiAccessMode.USER_ACCOUNT)
        repository.recordRateLimited(AiFeature.DAILY_ADVICE, null, AiAccessMode.USER_ACCOUNT)

        val state = repository.getUsageState()
        assertEquals(1, state.quotaExceededCountToday)
        assertEquals(1, state.failedRateLimitCountToday)
    }

    @Test
    fun `unauthorized user account sets account invalid`() = runBlocking {
        val repository = InMemoryAiUsageRepository()

        repository.recordUnauthorized(AiFeature.DAILY_ADVICE, AiAccessMode.USER_ACCOUNT)

        assertEquals(UserAccountStatus.INVALID, repository.getUsageState().userAccountStatus)
    }

    @Test
    fun `stale setup required status does not block configured free hourly mode`() = runBlocking {
        val repository = InMemoryAiUsageRepository(
            AiUsageState(
                selectedAccessMode = AiAccessMode.FREE_HOURLY,
                lastStatus = AiUsageStatus.SETUP_REQUIRED,
                lastErrorMessage = "Free hourly AI was not configured.",
            ),
        )

        val availability = repository.canUseAi(AiFeature.DIET_PLAN)

        assertTrue(availability is AiAvailability.Available)
    }
}
