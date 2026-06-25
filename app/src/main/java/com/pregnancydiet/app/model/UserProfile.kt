package com.pregnancydiet.app.model

data class UserProfile(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val onboardingCompleted: Boolean,
    val activePregnancyProfileId: String?,
)