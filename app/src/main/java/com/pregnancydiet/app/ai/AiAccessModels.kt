package com.pregnancydiet.app.ai

enum class AiAccessMode {
    FREE_HOURLY,
    USER_ACCOUNT,
}

sealed class SaveCredentialResult {
    data object Success : SaveCredentialResult()
    data object Blank : SaveCredentialResult()
    data object UnsafeSecretKey : SaveCredentialResult()
    data object InvalidFormat : SaveCredentialResult()
    data object StorageError : SaveCredentialResult()
}

sealed class AiCredentialResolution {
    data class FreeHourlyCredential(val publicKey: String) : AiCredentialResolution()
    data class UserAccountCredential(val credential: String) : AiCredentialResolution()
    data object MissingFreeHourlyKey : AiCredentialResolution()
    data object MissingUserCredential : AiCredentialResolution()
    data object InvalidUserCredential : AiCredentialResolution()
    data object UnsafeCredential : AiCredentialResolution()
}

data class AiCredentialState(
    val selectedAccessMode: AiAccessMode,
    val hasUserCredential: Boolean,
    val usingUserAccount: Boolean,
    val setupRequired: Boolean,
    val message: String?,
)

object AiCredentialValidator {
    fun isUnsafeServerSecret(credential: String): Boolean = credential.trim().startsWith("sk_")

    fun isValidPublishableKey(credential: String): Boolean {
        val trimmed = credential.trim()
        return trimmed.isNotBlank() && trimmed.startsWith("pk_") && trimmed.length >= MIN_CLIENT_CREDENTIAL_LENGTH
    }

    fun isValidUserCredential(credential: String): Boolean {
        val trimmed = credential.trim()
        if (trimmed.isBlank() || isUnsafeServerSecret(trimmed)) return false
        // Pollinations user/client credentials must be client-safe. Accept publishable keys and
        // non-secret user credentials with enough entropy; reject obvious server-only sk_ secrets.
        return trimmed.length >= MIN_CLIENT_CREDENTIAL_LENGTH && trimmed.none(Char::isWhitespace)
    }

    private const val MIN_CLIENT_CREDENTIAL_LENGTH = 8
}