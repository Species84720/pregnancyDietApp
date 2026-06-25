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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.safety.SymptomSafetyResult
import com.pregnancydiet.app.symptoms.CommonSymptomOptions
import com.pregnancydiet.app.symptoms.SymptomFormState
import com.pregnancydiet.app.symptoms.SymptomLoggingViewModel

@Composable
fun SymptomLoggingScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SymptomLoggingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    when {
        state.isLoading -> SymptomLoadingState(modifier)
        else -> SymptomContent(
            form = state.form,
            pregnancyWeek = state.pregnancyProgress?.pregnancyWeek,
            trimester = state.pregnancyProgress?.trimester,
            draftSafetyResult = state.draftSafetyResult,
            lastSavedSafetyResult = state.lastSavedSafetyResult,
            history = state.history,
            isSaving = state.isSaving,
            successMessage = state.successMessage,
            errorMessage = state.errorMessage,
            onDateChange = viewModel::updateDate,
            onSymptomNameChange = viewModel::updateSymptomName,
            onSeverityChange = viewModel::updateSeverity,
            onDurationChange = viewModel::updateDuration,
            onNotesChange = viewModel::updateNotes,
            onSubmit = { viewModel.submit(uid.orEmpty()) },
            onBackToHome = onBackToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun SymptomLoadingState(modifier: Modifier = Modifier) {
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
            text = "Loading symptom logger...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SymptomContent(
    form: SymptomFormState,
    pregnancyWeek: Int?,
    trimester: Int?,
    draftSafetyResult: SymptomSafetyResult,
    lastSavedSafetyResult: SymptomSafetyResult?,
    history: List<SymptomLog>,
    isSaving: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onDateChange: (String) -> Unit,
    onSymptomNameChange: (String) -> Unit,
    onSeverityChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
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
                text = "Symptoms",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        PregnancyContextCard(pregnancyWeek = pregnancyWeek, trimester = trimester)
        SymptomFormCard(
            form = form,
            isSaving = isSaving,
            onDateChange = onDateChange,
            onSymptomNameChange = onSymptomNameChange,
            onSeverityChange = onSeverityChange,
            onDurationChange = onDurationChange,
            onNotesChange = onNotesChange,
            onSubmit = onSubmit,
        )

        if (draftSafetyResult.urgentFlag) {
            UrgentSymptomWarning(
                title = "Safety warning before saving",
                safetyResult = draftSafetyResult,
            )
        }

        if (lastSavedSafetyResult?.urgentFlag == true) {
            UrgentSymptomWarning(
                title = "Urgent warning for saved symptom",
                safetyResult = lastSavedSafetyResult,
            )
        }

        successMessage?.let {
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

        SymptomHistorySection(history)
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PregnancyContextCard(
    pregnancyWeek: Int?,
    trimester: Int?,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Pregnancy context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Week: ${pregnancyWeek?.toString() ?: "not available"} · Trimester: ${trimester?.toString() ?: "not available"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Red-flag checks run locally before any AI summary is used.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SymptomFormCard(
    form: SymptomFormState,
    isSaving: Boolean,
    onDateChange: (String) -> Unit,
    onSymptomNameChange: (String) -> Unit,
    onSeverityChange: (String) -> Unit,
    onDurationChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Log a symptom",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.date,
                onValueChange = onDateChange,
                label = { Text("Date") },
                supportingText = { Text("Use YYYY-MM-DD") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.symptomName,
                onValueChange = onSymptomNameChange,
                label = { Text("Symptom name") },
                singleLine = true,
            )
            SymptomOptionButtons(
                selectedSymptom = form.symptomName,
                onSelect = onSymptomNameChange,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.severity,
                onValueChange = onSeverityChange,
                label = { Text("Severity") },
                supportingText = { Text("Enter 1-10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.duration,
                onValueChange = onDurationChange,
                label = { Text("Duration") },
                placeholder = { Text("For example: 2 hours") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.notes,
                onValueChange = onNotesChange,
                label = { Text("Notes") },
                minLines = 3,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSubmit,
                enabled = !isSaving,
            ) {
                Text(if (isSaving) "Saving..." else "Save symptom")
            }
        }
    }
}

@Composable
private fun SymptomOptionButtons(
    selectedSymptom: String,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Common symptoms",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        CommonSymptomOptions.allOptions.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowOptions.forEach { option ->
                    val selected = option.equals(selectedSymptom, ignoreCase = true)
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(option) },
                    ) {
                        Text(
                            text = if (selected) "✓ $option" else option,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (rowOptions.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun UrgentSymptomWarning(
    title: String,
    safetyResult: SymptomSafetyResult,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "This may require medical attention. Contact your gynecologist, midwife, or emergency services.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            safetyResult.urgentReasons.forEach { reason ->
                Text(
                    text = "• $reason",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun SymptomHistorySection(history: List<SymptomLog>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Recent symptom history",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (history.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "No symptom logs yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            history.forEach { log ->
                SymptomHistoryCard(log)
            }
        }
    }
}

@Composable
private fun SymptomHistoryCard(log: SymptomLog) {
    val symptom = log.primarySymptom
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = log.date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = listOfNotNull(
                    symptom?.name,
                    symptom?.severity?.let { "severity $it/10" },
                    log.pregnancyWeek?.let { "week $it" },
                    log.trimester?.let { "trimester $it" },
                ).joinToString(separator = " · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            symptom?.duration?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = "Duration: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            symptom?.notes?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (log.urgentFlag) {
                Text(
                    text = "Urgent warning shown",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                )
                log.urgentReasons.forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
