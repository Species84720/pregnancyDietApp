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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.onboarding.OnboardingViewModel

@Composable
fun OnboardingScreen(
    user: AuthenticatedUser?,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.completed) {
        if (state.completed) {
            viewModel.markCompletionHandled()
            onCompleted()
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Pregnancy onboarding",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Add your pregnancy dating and weight details. Your gynecologist's dates should override app estimates.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FormTextField(
                value = state.dateFoundOut,
                onValueChange = viewModel::updateDateFoundOut,
                label = "Date found out pregnant *",
                supportingText = "YYYY-MM-DD",
            )
            FormTextField(
                value = state.lastMenstrualPeriod,
                onValueChange = viewModel::updateLastMenstrualPeriod,
                label = "Last menstrual period",
                supportingText = "Optional, YYYY-MM-DD",
            )
            FormTextField(
                value = state.estimatedDueDate,
                onValueChange = viewModel::updateEstimatedDueDate,
                label = "Estimated due date",
                supportingText = "Optional doctor-provided date, YYYY-MM-DD",
            )
            FormTextField(
                value = state.doctorConfirmedWeek,
                onValueChange = viewModel::updateDoctorConfirmedWeek,
                label = "Doctor-confirmed pregnancy week",
                supportingText = "Optional, 1-42",
                keyboardType = KeyboardType.Number,
            )

            Text("Pregnancy type", style = MaterialTheme.typography.titleMedium)
            ChipRow {
                PregnancyType.entries.forEach { type ->
                    FilterChip(
                        selected = state.pregnancyType == type,
                        onClick = { viewModel.updatePregnancyType(type) },
                        label = { Text(type.name) },
                    )
                }
            }

            Text("Weight unit", style = MaterialTheme.typography.titleMedium)
            ChipRow {
                WeightUnit.entries.forEach { unit ->
                    FilterChip(
                        selected = state.weightUnit == unit,
                        onClick = { viewModel.updateWeightUnit(unit) },
                        label = { Text(unit.firestoreValue) },
                    )
                }
            }

            FormTextField(
                value = state.currentWeight,
                onValueChange = viewModel::updateCurrentWeight,
                label = "Current weight *",
                keyboardType = KeyboardType.Decimal,
            )
            FormTextField(
                value = state.prePregnancyWeight,
                onValueChange = viewModel::updatePrePregnancyWeight,
                label = "Pre-pregnancy weight",
                supportingText = "Optional but recommended",
                keyboardType = KeyboardType.Decimal,
            )
            FormTextField(
                value = state.heightCm,
                onValueChange = viewModel::updateHeightCm,
                label = "Height in cm",
                supportingText = "Optional but recommended",
                keyboardType = KeyboardType.Decimal,
            )
            FormTextField(
                value = state.allergies,
                onValueChange = viewModel::updateAllergies,
                label = "Allergies",
                supportingText = "Comma-separated, optional",
            )
            FormTextField(
                value = state.dietaryRestrictions,
                onValueChange = viewModel::updateDietaryRestrictions,
                label = "Dietary restrictions",
                supportingText = "Comma-separated, optional",
            )
            FormTextField(
                value = state.medicalConditions,
                onValueChange = viewModel::updateMedicalConditions,
                label = "Medical conditions",
                supportingText = "Comma-separated, optional",
            )

            Text(
                text = "Date found out alone is not enough for accurate pregnancy dating. Add LMP, due date, or doctor-confirmed week if available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            state.progress?.let { progress ->
                Text(
                    text = listOfNotNull(
                        progress.pregnancyWeek?.let { "Week $it" },
                        progress.dayWithinWeek?.let { "day $it" },
                        progress.trimester?.let { "trimester $it" },
                        progress.estimatedDueDate?.let { "due $it" },
                    ).joinToString(" • ").ifBlank { progress.message.orEmpty() },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            state.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSaving && user != null,
                onClick = { user?.uid?.let(viewModel::submit) },
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Save pregnancy profile")
                }
            }

            Text(
                text = AppConstants.MEDICAL_DISCLAIMER,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = supportingText?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
    )
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