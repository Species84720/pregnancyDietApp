package com.pregnancydiet.app.analytics

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacySafeAnalyticsTest {
    @Test
    fun `analytics placeholder uses allowlisted event names only`() {
        val eventNames = AnalyticsEvent.entries.map { it.eventName }

        assertTrue(eventNames.all { it in privacySafeAnalyticsEvents })
        assertFalse(eventNames.any { it.contains("uid", ignoreCase = true) })
        assertFalse(eventNames.any { it.contains("symptom", ignoreCase = true) && it != AnalyticsEvent.SymptomLogOpened.eventName })
    }

    @Test
    fun `no op analytics accepts allowlisted events without payload`() {
        NoOpPrivacySafeAnalytics.track(AnalyticsEvent.HomeViewed)
    }
}