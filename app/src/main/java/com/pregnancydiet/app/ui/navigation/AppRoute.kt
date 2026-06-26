package com.pregnancydiet.app.ui.navigation

sealed class AppRoute(
    val route: String,
    val title: String,
) {
    data object Splash : AppRoute("splash", "Loading")
    data object Home : AppRoute("home", "Home")
    data object Auth : AppRoute("auth", "Sign in")
    data object Onboarding : AppRoute("onboarding", "Onboarding")
    data object Pregnancy : AppRoute("pregnancy", "Pregnancy")
    data object Symptoms : AppRoute("symptoms", "Symptoms")
    data object Supplements : AppRoute("supplements", "Supplements")
    data object Meals : AppRoute("meals", "Meals")
    data object Nutrition : AppRoute("nutrition", "Nutrition")
    data object AiSummary : AppRoute("ai-summary", "AI Summary")
    data object Reminders : AppRoute("reminders", "Reminders")
    data object Reports : AppRoute("reports", "Reports")
    data object Settings : AppRoute("settings", "Settings")
    data object AiUsage : AppRoute("ai-usage", "AI Usage")
    data object Privacy : AppRoute("privacy", "Privacy")
    data object MedicalDisclaimer : AppRoute("medical-disclaimer", "Medical Disclaimer")

    companion object {
        val topLevelRoutes = listOf(Home, Pregnancy, Symptoms, Supplements, Meals, Nutrition, AiSummary, Reminders, Reports, Settings)
    }
}