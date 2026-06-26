package com.pregnancydiet.app.reminders

import com.pregnancydiet.app.model.ReminderPreferences

data class ReminderSettingsUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val preferences: ReminderPreferences = ReminderPreferences(),
    val activeSupplementCount: Int = 0,
    val notificationPermissionGranted: Boolean = false,
    val scheduledMessage: String? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    val remindersNeedPermission: Boolean = preferences.anyEnabled && !notificationPermissionGranted
}
