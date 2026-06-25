package com.pregnancydiet.app.model

data class WeightLog(
    val id: String,
    val date: String,
    val pregnancyProfileId: String,
    val pregnancyWeek: Int?,
    val weightKg: Double,
    val source: String = "onboarding",
    val notes: String = "Initial onboarding weight",
)