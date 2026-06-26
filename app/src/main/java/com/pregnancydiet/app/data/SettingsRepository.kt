package com.pregnancydiet.app.data

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PrivacySettings
import com.pregnancydiet.app.model.UserProfile

interface SettingsRepository {
    suspend fun loadSettings(uid: String): Result<SettingsData>

    suspend fun savePregnancyProfile(
        uid: String,
        pregnancyProfile: PregnancyProfile,
    ): Result<PregnancyProfile>

    suspend fun savePrivacySettings(
        uid: String,
        privacySettings: PrivacySettings,
    ): Result<PrivacySettings>

    suspend fun deleteUserData(uid: String): Result<Unit>
}

data class SettingsData(
    val userProfile: UserProfile?,
    val pregnancyProfile: PregnancyProfile?,
    val privacySettings: PrivacySettings,
)