package com.pregnancydiet.app.ai

data class AiUsageUiState(
    val credentialState: AiCredentialState = AiCredentialState(
        selectedAccessMode = AiAccessMode.FREE_HOURLY,
        hasUserCredential = false,
        usingUserAccount = false,
        setupRequired = true,
        message = "Loading AI usage settings...",
    ),
    val usageState: AiUsageState = AiUsageState(),
    val recentEvents: List<AiUsageEvent> = emptyList(),
    val userCredentialInput: String = "",
    val successMessage: String? = null,
    val errorMessage: String? = null,
)

val AiUsageUiState.compactStatus: String
    get() = when {
        credentialState.usingUserAccount -> "Using your Pollinations account"
        credentialState.setupRequired -> credentialState.message ?: "AI setup required"
        usageState.lastStatus == AiUsageStatus.RATE_LIMITED -> "Rate limited — try later"
        usageState.nextEstimatedFreeRequestAtMillis?.let { it > System.currentTimeMillis() } == true -> "Next free AI request later"
        else -> "AI available"
    }