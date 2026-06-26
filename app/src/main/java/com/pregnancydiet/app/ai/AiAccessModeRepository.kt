package com.pregnancydiet.app.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface AiAccessModeRepository {
    fun observeAccessMode(): Flow<AiAccessMode>
    suspend fun getAccessMode(): AiAccessMode
    suspend fun setAccessMode(mode: AiAccessMode)
    fun hasUserAccountCredential(): Flow<Boolean>
    suspend fun saveUserAccountCredential(credential: String): SaveCredentialResult
    suspend fun clearUserAccountCredential()
    suspend fun getUserAccountCredentialOrNull(): String?
}

class LocalAiAccessModeRepository(
    context: Context,
) : AiAccessModeRepository {
    private val appContext = context.applicationContext
    private val modePrefs: SharedPreferences = appContext.getSharedPreferences(MODE_PREFS, Context.MODE_PRIVATE)
    private val credentialPrefs: SharedPreferences = createEncryptedPrefs(appContext)
    private val accessMode = MutableStateFlow(readMode())
    private val hasCredential = MutableStateFlow(readCredential().isNullOrBlank().not())

    override fun observeAccessMode(): Flow<AiAccessMode> = accessMode.asStateFlow()

    override suspend fun getAccessMode(): AiAccessMode = accessMode.value

    override suspend fun setAccessMode(mode: AiAccessMode) {
        val nextMode = if (mode == AiAccessMode.USER_ACCOUNT && readCredential().isNullOrBlank()) {
            AiAccessMode.FREE_HOURLY
        } else {
            mode
        }
        modePrefs.edit().putString(KEY_MODE, nextMode.name).apply()
        accessMode.value = nextMode
    }

    override fun hasUserAccountCredential(): Flow<Boolean> = hasCredential.asStateFlow()

    override suspend fun saveUserAccountCredential(credential: String): SaveCredentialResult {
        val trimmed = credential.trim()
        if (trimmed.isBlank()) return SaveCredentialResult.Blank
        if (AiCredentialValidator.isUnsafeServerSecret(trimmed)) return SaveCredentialResult.UnsafeSecretKey
        if (!AiCredentialValidator.isValidUserCredential(trimmed)) return SaveCredentialResult.InvalidFormat

        return runCatching {
            credentialPrefs.edit().putString(KEY_USER_CREDENTIAL, trimmed).apply()
            hasCredential.value = true
            setAccessMode(AiAccessMode.USER_ACCOUNT)
            SaveCredentialResult.Success
        }.getOrElse {
            SaveCredentialResult.StorageError
        }
    }

    override suspend fun clearUserAccountCredential() {
        credentialPrefs.edit().remove(KEY_USER_CREDENTIAL).apply()
        hasCredential.value = false
        setAccessMode(AiAccessMode.FREE_HOURLY)
    }

    override suspend fun getUserAccountCredentialOrNull(): String? = readCredential()

    private fun readMode(): AiAccessMode = modePrefs.getString(KEY_MODE, AiAccessMode.FREE_HOURLY.name)
        ?.let { value -> AiAccessMode.entries.firstOrNull { it.name == value } }
        ?: AiAccessMode.FREE_HOURLY

    private fun readCredential(): String? = credentialPrefs.getString(KEY_USER_CREDENTIAL, null)
        ?.takeIf { it.isNotBlank() }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            CREDENTIAL_PREFS,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private companion object {
        const val MODE_PREFS = "ai_access_mode"
        const val CREDENTIAL_PREFS = "ai_access_credentials"
        const val KEY_MODE = "selected_ai_access_mode"
        const val KEY_USER_CREDENTIAL = "pollinations_user_credential"
    }
}

class InMemoryAiAccessModeRepository(
    initialMode: AiAccessMode = AiAccessMode.FREE_HOURLY,
    initialCredential: String? = null,
) : AiAccessModeRepository {
    private val accessMode = MutableStateFlow(initialMode)
    private val credential = MutableStateFlow(initialCredential)
    private val hasCredential = MutableStateFlow(!initialCredential.isNullOrBlank())

    override fun observeAccessMode(): Flow<AiAccessMode> = accessMode.asStateFlow()

    override suspend fun getAccessMode(): AiAccessMode = accessMode.value
    override suspend fun setAccessMode(mode: AiAccessMode) {
        accessMode.value = if (mode == AiAccessMode.USER_ACCOUNT && credential.value.isNullOrBlank()) {
            AiAccessMode.FREE_HOURLY
        } else {
            mode
        }
    }

    override fun hasUserAccountCredential(): Flow<Boolean> = hasCredential.asStateFlow()

    override suspend fun saveUserAccountCredential(credential: String): SaveCredentialResult {
        val trimmed = credential.trim()
        if (trimmed.isBlank()) return SaveCredentialResult.Blank
        if (AiCredentialValidator.isUnsafeServerSecret(trimmed)) return SaveCredentialResult.UnsafeSecretKey
        if (!AiCredentialValidator.isValidUserCredential(trimmed)) return SaveCredentialResult.InvalidFormat
        this.credential.value = trimmed
        hasCredential.value = true
        accessMode.value = AiAccessMode.USER_ACCOUNT
        return SaveCredentialResult.Success
    }

    override suspend fun clearUserAccountCredential() {
        credential.value = null
        hasCredential.value = false
        accessMode.value = AiAccessMode.FREE_HOURLY
    }

    override suspend fun getUserAccountCredentialOrNull(): String? = credential.value
}