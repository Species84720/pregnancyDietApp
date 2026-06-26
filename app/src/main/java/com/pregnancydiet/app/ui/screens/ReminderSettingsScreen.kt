package com.pregnancydiet.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.ReminderPreferences
import com.pregnancydiet.app.reminders.ReminderSettingsViewModel

@Composable
fun ReminderSettingsScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReminderSettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    var permissionGranted by remember {
        mutableStateOf(context.notificationsPermissionGranted())
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        permissionGranted = granted
        viewModel.updateNotificationPermission(granted)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid, permissionGranted) {
        viewModel.load(
            uid = uid.orEmpty(),
            notificationPermissionGranted = permissionGranted,
        )
    }

    when {
        state.isLoading -> ReminderLoadingState(modifier)
        else -> ReminderSettingsContent(
            preferences = state.preferences,
            activeSupplementCount = state.activeSupplementCount,
            permissionGranted = state.notificationPermissionGranted,
            remindersNeedPermission = state.remindersNeedPermission,
            isSaving = state.isSaving,
            successMessage = state.successMessage,
            scheduledMessage = state.scheduledMessage,
            errorMessage = state.errorMessage,
            onRequestPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    permissionGranted = true
                    viewModel.updateNotificationPermission(true)
                }
            },
            onToggleSupplementReminders = viewModel::toggleSupplementReminders,
            onToggleMealReminders = viewModel::toggleMealReminders,
            onToggleSymptomCheckIn = viewModel::toggleSymptomCheckIn,
            onMealReminderTimeChange = viewModel::updateMealReminderTime,
            onSymptomReminderTimeChange = viewModel::updateSymptomReminderTime,
            onSave = { viewModel.save(uid.orEmpty()) },
            onDisableAll = { viewModel.disableAll(uid.orEmpty()) },
            onBackToHome = onBackToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun ReminderLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Loading reminder settings...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReminderSettingsContent(
    preferences: ReminderPreferences,
    activeSupplementCount: Int,
    permissionGranted: Boolean,
    remindersNeedPermission: Boolean,
    isSaving: Boolean,
    successMessage: String?,
    scheduledMessage: String?,
    errorMessage: String?,
    onRequestPermission: () -> Unit,
    onToggleSupplementReminders: () -> Unit,
    onToggleMealReminders: () -> Unit,
    onToggleSymptomCheckIn: () -> Unit,
    onMealReminderTimeChange: (String) -> Unit,
    onSymptomReminderTimeChange: (String) -> Unit,
    onSave: () -> Unit,
    onDisableAll: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Reminders",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        ReminderIntroCard()
        NotificationPermissionCard(
            permissionGranted = permissionGranted,
            remindersNeedPermission = remindersNeedPermission,
            onRequestPermission = onRequestPermission,
        )
        ReminderTypesCard(
            preferences = preferences,
            activeSupplementCount = activeSupplementCount,
            onToggleSupplementReminders = onToggleSupplementReminders,
            onToggleMealReminders = onToggleMealReminders,
            onToggleSymptomCheckIn = onToggleSymptomCheckIn,
            onMealReminderTimeChange = onMealReminderTimeChange,
            onSymptomReminderTimeChange = onSymptomReminderTimeChange,
        )
        ReminderActionsCard(
            isSaving = isSaving,
            anyEnabled = preferences.anyEnabled,
            onSave = onSave,
            onDisableAll = onDisableAll,
        )

        successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        scheduledMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ReminderIntroCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Gentle optional reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Turn on only the reminders you want. Notifications are phrased gently and are never urgent medical alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NotificationPermissionCard(
    permissionGranted: Boolean,
    remindersNeedPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (remindersNeedPermission) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Notification permission",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (permissionGranted) {
                    "Notifications are allowed on this device."
                } else {
                    "Allow notifications before enabling reminders. You can still keep all reminders off."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (remindersNeedPermission) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!permissionGranted) {
                Button(onClick = onRequestPermission) {
                    Text("Allow notifications")
                }
            }
        }
    }
}

@Composable
private fun ReminderTypesCard(
    preferences: ReminderPreferences,
    activeSupplementCount: Int,
    onToggleSupplementReminders: () -> Unit,
    onToggleMealReminders: () -> Unit,
    onToggleSymptomCheckIn: () -> Unit,
    onMealReminderTimeChange: (String) -> Unit,
    onSymptomReminderTimeChange: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Reminder types",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            ReminderToggleRow(
                title = "Supplement reminders",
                status = if (preferences.supplementRemindersEnabled) "On" else "Off",
                description = "Uses each active supplement's saved time. Active supplements: $activeSupplementCount.",
                onToggle = onToggleSupplementReminders,
            )
            ReminderToggleRow(
                title = "Meal logging reminder",
                status = if (preferences.mealRemindersEnabled) "On" else "Off",
                description = "One optional daily prompt to log meals or snacks.",
                onToggle = onToggleMealReminders,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = preferences.mealReminderTime,
                onValueChange = onMealReminderTimeChange,
                label = { Text("Meal reminder time") },
                supportingText = { Text("Use HH:MM, for example 12:30") },
                singleLine = true,
                enabled = preferences.mealRemindersEnabled,
            )
            ReminderToggleRow(
                title = "Daily symptom check-in",
                status = if (preferences.symptomCheckInEnabled) "On" else "Off",
                description = "One gentle daily prompt to log symptoms if you noticed any.",
                onToggle = onToggleSymptomCheckIn,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = preferences.symptomReminderTime,
                onValueChange = onSymptomReminderTimeChange,
                label = { Text("Symptom check-in time") },
                supportingText = { Text("Use HH:MM, for example 20:00") },
                singleLine = true,
                enabled = preferences.symptomCheckInEnabled,
            )
        }
    }
}

@Composable
private fun ReminderToggleRow(
    title: String,
    status: String,
    description: String,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(onClick = onToggle) {
            Text(status)
        }
    }
}

@Composable
private fun ReminderActionsCard(
    isSaving: Boolean,
    anyEnabled: Boolean,
    onSave: () -> Unit,
    onDisableAll: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSave,
                enabled = !isSaving,
            ) {
                Text(if (isSaving) "Saving..." else "Save reminder settings")
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDisableAll,
                enabled = !isSaving && anyEnabled,
            ) {
                Text("Disable all reminders")
            }
        }
    }
}

private fun android.content.Context.notificationsPermissionGranted(): Boolean =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
