package com.pregnancydiet.app.settings

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PrivacySettings
import com.pregnancydiet.app.model.UserProfile

data class SettingsUiState(
    val isLoading: Boolean = true,
    val isSavingProfile: Boolean = false,
    val isSavingPrivacy: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val userProfile: UserProfile? = null,
    val pregnancyProfile: PregnancyProfile? = null,
    val profileForm: SettingsFormState = SettingsFormState(),
    val privacySettings: PrivacySettings = PrivacySettings(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
)