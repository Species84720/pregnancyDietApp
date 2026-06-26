package com.pregnancydiet.app.ai

import com.pregnancydiet.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AiCredentialResolver(
    private val accessModeRepository: AiAccessModeRepository,
    private val bundledPublicKey: String = BuildConfig.POLLINATIONS_PUBLIC_KEY,
    private val isDebug: Boolean = BuildConfig.DEBUG,
) {
    init {
        if (isDebug && AiCredentialValidator.isUnsafeServerSecret(bundledPublicKey)) {
            error("POLLINATIONS_PUBLIC_KEY must be a client-safe pk_ publishable key, never an sk_ server secret.")
        }
    }

    suspend fun resolveCredential(): AiCredentialResolution {
        val mode = accessModeRepository.getAccessMode()
        return when (mode) {
            AiAccessMode.FREE_HOURLY -> resolveFreeHourlyCredential()
            AiAccessMode.USER_ACCOUNT -> resolveUserCredential()
        }
    }

    fun observeCredentialState(): Flow<AiCredentialState> = combine(
        accessModeRepository.observeAccessMode(),
        accessModeRepository.hasUserAccountCredential(),
    ) { mode, hasCredential ->
        when (mode) {
            AiAccessMode.USER_ACCOUNT -> AiCredentialState(
                selectedAccessMode = mode,
                hasUserCredential = hasCredential,
                usingUserAccount = hasCredential,
                setupRequired = !hasCredential,
                message = if (hasCredential) "Using your Pollinations account." else "Reconnect your Pollinations account or switch to free hourly AI.",
            )
            AiAccessMode.FREE_HOURLY -> {
                val freeReady = resolveFreeHourlyCredential() is AiCredentialResolution.FreeHourlyCredential
                AiCredentialState(
                    selectedAccessMode = mode,
                    hasUserCredential = hasCredential,
                    usingUserAccount = false,
                    setupRequired = !freeReady,
                    message = if (freeReady) "Free hourly AI is selected." else "Free hourly AI is not configured.",
                )
            }
        }
    }

    private fun resolveFreeHourlyCredential(): AiCredentialResolution {
        val key = bundledPublicKey.trim()
        if (key.isBlank()) return AiCredentialResolution.MissingFreeHourlyKey
        if (AiCredentialValidator.isUnsafeServerSecret(key)) return AiCredentialResolution.UnsafeCredential
        if (!AiCredentialValidator.isValidPublishableKey(key)) return AiCredentialResolution.MissingFreeHourlyKey
        return AiCredentialResolution.FreeHourlyCredential(key)
    }

    private suspend fun resolveUserCredential(): AiCredentialResolution {
        val credential = accessModeRepository.getUserAccountCredentialOrNull()?.trim()
            ?: return AiCredentialResolution.MissingUserCredential
        if (AiCredentialValidator.isUnsafeServerSecret(credential)) return AiCredentialResolution.UnsafeCredential
        if (!AiCredentialValidator.isValidUserCredential(credential)) return AiCredentialResolution.InvalidUserCredential
        return AiCredentialResolution.UserAccountCredential(credential)
    }
}