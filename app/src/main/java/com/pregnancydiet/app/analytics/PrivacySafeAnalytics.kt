package com.pregnancydiet.app.analytics

interface PrivacySafeAnalytics {
    fun track(event: AnalyticsEvent)
}

object NoOpPrivacySafeAnalytics : PrivacySafeAnalytics {
    override fun track(event: AnalyticsEvent) = Unit
}