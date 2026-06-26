package com.pregnancydiet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.settings.SettingsFormState
import com.pregnancydiet.app.settings.SettingsUiState
import com.pregnancydiet.app.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    user: AuthenticatedUser?,
    uid: String?,
    onBackToHome: () -> Unit,
    onNotificationSettings: () -> Unit,
    onDataExport: () -> Unit,
    onAiUsage: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onMedicalDisclaimer: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    if (showSignOutDialog) {
        ConfirmActionDialog(
            title = "Sign out?",
            message = "You will need to sign in again to access your pregnancy logs.",
            confirmLabel = "Sign out",
            destructive = false,
            onDismiss = { showSignOutDialog = false },
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
        )
    }

    if (showDeleteDialog) {
        ConfirmActionDialog(
            title = "Delete account and data?",
            message = "This removes user-scoped pregnancy profiles, weight logs, symptoms, supplements, meals, nutrition summaries, reminders, reports source data, and privacy settings. This cannot be undone.",
            confirmLabel = "Delete account",
            destructive = true,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteAccount(uid.orEmpty())
            },
        )
    }

    SettingsContent(
        user = user,
        state = state,
        onBackToHome = onBackToHome,
        onNotificationSettings = onNotificationSettings,
        onDataExport = onDataExport,
        onAiUsage = onAiUsage,
        onPrivacyPolicy = onPrivacyPolicy,
        onMedicalDisclaimer = onMedicalDisclaimer,
        onSaveProfile = { viewModel.savePregnancyProfile(uid.orEmpty()) },
        onSavePrivacy = { viewModel.savePrivacySettings(uid.orEmpty()) },
        onSignOut = { showSignOutDialog = true },
        onDeleteAccount = { showDeleteDialog = true },
        onDateFoundOutChange = viewModel::updateDateFoundOut,
        onLastMenstrualPeriodChange = viewModel::updateLastMenstrualPeriod,
        onEstimatedDueDateChange = viewModel::updateEstimatedDueDate,
        onDoctorConfirmedWeekChange = viewModel::updateDoctorConfirmedWeek,
        onPregnancyTypeChange = viewModel::updatePregnancyType,
        onHeightCmChange = viewModel::updateHeightCm,
        onPrePregnancyWeightChange = viewModel::updatePrePregnancyWeight,
        onCurrentWeightChange = viewModel::updateCurrentWeight,
        onWeightUnitChange = viewModel::updateWeightUnit,
        onAllergiesChange = viewModel::updateAllergies,
        onDietaryRestrictionsChange = viewModel::updateDietaryRestrictions,
        onMedicalConditionsChange = viewModel::updateMedicalConditions,
        onAiProcessingAllowedChange = viewModel::updateAiProcessingAllowed,
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    user: AuthenticatedUser?,
    state: SettingsUiState,
    onBackToHome: () -> Unit,
    onNotificationSettings: () -> Unit,
    onDataExport: () -> Unit,
    onAiUsage: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onMedicalDisclaimer: () -> Unit,
    onSaveProfile: () -> Unit,
    onSavePrivacy: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
    onDateFoundOutChange: (String) -> Unit,
    onLastMenstrualPeriodChange: (String) -> Unit,
    onEstimatedDueDateChange: (String) -> Unit,
    onDoctorConfirmedWeekChange: (String) -> Unit,
    onPregnancyTypeChange: (PregnancyType) -> Unit,
    onHeightCmChange: (String) -> Unit,
    onPrePregnancyWeightChange: (String) -> Unit,
    onCurrentWeightChange: (String) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onDietaryRestrictionsChange: (String) -> Unit,
    onMedicalConditionsChange: (String) -> Unit,
    onAiProcessingAllowedChange: (Boolean) -> Unit,
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
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        when {
            state.isLoading -> LoadingSettingsCard()
            else -> {
                ProfileDetailsCard(user = user, state = state)
                PregnancyProfileEditCard(
                    form = state.profileForm,
                    enabled = state.pregnancyProfile != null && !state.isSavingProfile,
                    isSaving = state.isSavingProfile,
                    onDateFoundOutChange = onDateFoundOutChange,
                    onLastMenstrualPeriodChange = onLastMenstrualPeriodChange,
                    onEstimatedDueDateChange = onEstimatedDueDateChange,
                    onDoctorConfirmedWeekChange = onDoctorConfirmedWeekChange,
                    onPregnancyTypeChange = onPregnancyTypeChange,
                    onHeightCmChange = onHeightCmChange,
                    onPrePregnancyWeightChange = onPrePregnancyWeightChange,
                    onCurrentWeightChange = onCurrentWeightChange,
                    onWeightUnitChange = onWeightUnitChange,
                    onAllergiesChange = onAllergiesChange,
                    onDietaryRestrictionsChange = onDietaryRestrictionsChange,
                    onMedicalConditionsChange = onMedicalConditionsChange,
                    onSaveProfile = onSaveProfile,
                )
                PrivacyControlsCard(
                    aiProcessingAllowed = state.privacySettings.aiProcessingAllowed,
                    isSaving = state.isSavingPrivacy,
                    onAiProcessingAllowedChange = onAiProcessingAllowedChange,
                    onSavePrivacy = onSavePrivacy,
                    onPrivacyPolicy = onPrivacyPolicy,
                    onMedicalDisclaimer = onMedicalDisclaimer,
                )
                SettingsEntryPointsCard(
                    onNotificationSettings = onNotificationSettings,
                    onDataExport = onDataExport,
                    onAiUsage = onAiUsage,
                )
                AccountActionsCard(
                    isDeletingAccount = state.isDeletingAccount,
                    onSignOut = onSignOut,
                    onDeleteAccount = onDeleteAccount,
                )
            }
        }

        state.successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        state.errorMessage?.let {
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
private fun LoadingSettingsCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
            Text("Loading your settings...")
        }
    }
}

@Composable
private fun ProfileDetailsCard(
    user: AuthenticatedUser?,
    state: SettingsUiState,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Profile details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            SettingsInfoRow("Name", user?.displayName ?: state.userProfile?.displayName ?: "Not available")
            SettingsInfoRow("Email", user?.email ?: state.userProfile?.email ?: "Not available")
            SettingsInfoRow("User data scope", "Your app data is stored under your signed-in user ID only.")
        }
    }
}

@Composable
private fun PregnancyProfileEditCard(
    form: SettingsFormState,
    enabled: Boolean,
    isSaving: Boolean,
    onDateFoundOutChange: (String) -> Unit,
    onLastMenstrualPeriodChange: (String) -> Unit,
    onEstimatedDueDateChange: (String) -> Unit,
    onDoctorConfirmedWeekChange: (String) -> Unit,
    onPregnancyTypeChange: (PregnancyType) -> Unit,
    onHeightCmChange: (String) -> Unit,
    onPrePregnancyWeightChange: (String) -> Unit,
    onCurrentWeightChange: (String) -> Unit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onDietaryRestrictionsChange: (String) -> Unit,
    onMedicalConditionsChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Pregnancy profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Edit pregnancy dating, weight, allergies, dietary restrictions, and medical conditions. Doctor-confirmed dates should override app estimates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!enabled && !isSaving) {
                Text(
                    text = "Complete onboarding before editing pregnancy profile settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            SettingsTextField(form.dateFoundOut, onDateFoundOutChange, "Date found out pregnant *", "YYYY-MM-DD", enabled)
            SettingsTextField(form.lastMenstrualPeriod, onLastMenstrualPeriodChange, "Last menstrual period", "Optional, YYYY-MM-DD", enabled)
            SettingsTextField(form.estimatedDueDate, onEstimatedDueDateChange, "Estimated due date", "Optional doctor-provided date, YYYY-MM-DD", enabled)
            SettingsTextField(form.doctorConfirmedWeek, onDoctorConfirmedWeekChange, "Doctor-confirmed pregnancy week", "Optional, 1-42", enabled, KeyboardType.Number)

            Text("Pregnancy type", style = MaterialTheme.typography.titleMedium)
            ChipRow {
                PregnancyType.entries.forEach { type ->
                    FilterChip(
                        selected = form.pregnancyType == type,
                        onClick = { onPregnancyTypeChange(type) },
                        enabled = enabled,
                        label = { Text(type.name) },
                    )
                }
            }

            Text("Weight unit", style = MaterialTheme.typography.titleMedium)
            ChipRow {
                WeightUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = form.weightUnit == unit,
                        onClick = { onWeightUnitChange(unit) },
                        enabled = enabled,
                        label = { Text(unit.firestoreValue) },
                    )
                }
            }

            SettingsTextField(form.currentWeight, onCurrentWeightChange, "Current weight *", null, enabled, KeyboardType.Decimal)
            SettingsTextField(form.prePregnancyWeight, onPrePregnancyWeightChange, "Pre-pregnancy weight", "Optional but recommended", enabled, KeyboardType.Decimal)
            SettingsTextField(form.heightCm, onHeightCmChange, "Height in cm", "Optional but recommended", enabled, KeyboardType.Decimal)
            SettingsTextField(form.allergies, onAllergiesChange, "Allergies", "Comma-separated, optional", enabled)
            SettingsTextField(form.dietaryRestrictions, onDietaryRestrictionsChange, "Dietary restrictions", "Comma-separated, optional", enabled)
            SettingsTextField(form.medicalConditions, onMedicalConditionsChange, "Medical conditions", "Comma-separated, optional", enabled)

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSaveProfile,
                enabled = enabled,
            ) {
                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Save pregnancy settings")
                }
            }
        }
    }
}

@Composable
private fun PrivacyControlsCard(
    aiProcessingAllowed: Boolean,
    isSaving: Boolean,
    onAiProcessingAllowedChange: (Boolean) -> Unit,
    onSavePrivacy: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onMedicalDisclaimer: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Privacy and safety",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Allow AI summaries", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "When enabled, AI requests use minimal structured logs through the backend. Disable this to block new AI summary generation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = aiProcessingAllowed,
                    onCheckedChange = onAiProcessingAllowedChange,
                )
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                onClick = onSavePrivacy,
            ) {
                if (isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Save privacy settings")
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPrivacyPolicy,
            ) { Text("Privacy policy") }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onMedicalDisclaimer,
            ) { Text("Medical disclaimer") }
        }
    }
}

@Composable
private fun SettingsEntryPointsCard(
    onNotificationSettings: () -> Unit,
    onDataExport: () -> Unit,
    onAiUsage: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "More settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNotificationSettings,
            ) { Text("Notification settings") }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDataExport,
            ) { Text("Data export and reports") }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAiUsage,
            ) { Text("AI Usage") }
        }
    }
}

@Composable
private fun AccountActionsCard(
    isDeletingAccount: Boolean,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSignOut,
                enabled = !isDeletingAccount,
            ) { Text("Sign out") }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDeleteAccount,
                enabled = !isDeletingAccount,
            ) {
                if (isDeletingAccount) {
                    CircularProgressIndicator()
                } else {
                    Text("Delete account and data")
                }
            }
            Text(
                text = "Deleting your account removes user-owned Firestore data under your signed-in user ID before deleting the Firebase Authentication account. Recent sign-in may be required by Firebase.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmLabel,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun SettingsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supportingText: String?,
    enabled: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        enabled = enabled,
        singleLine = true,
    )
}

@Composable
private fun SettingsInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(0.35f),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            modifier = Modifier.weight(0.65f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        content()
    }
}