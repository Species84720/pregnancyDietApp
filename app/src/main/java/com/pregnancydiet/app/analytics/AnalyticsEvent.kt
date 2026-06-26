package com.pregnancydiet.app.analytics

enum class AnalyticsEvent(val eventName: String) {
    AppOpened("app_opened"),
    HomeViewed("home_viewed"),
    MealLogOpened("meal_log_opened"),
    SymptomLogOpened("symptom_log_opened"),
    SupplementTrackerOpened("supplement_tracker_opened"),
    NutritionOpened("nutrition_opened"),
    ReportsOpened("reports_opened"),
    SettingsOpened("settings_opened"),
}

val privacySafeAnalyticsEvents: Set<String> = AnalyticsEvent.entries.map { it.eventName }.toSet()