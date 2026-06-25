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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import com.pregnancydiet.app.supplements.SupplementFormState
import com.pregnancydiet.app.supplements.SupplementStatusMapper
import com.pregnancydiet.app.supplements.SupplementTrackingViewModel

private const val SUPPLEMENT_SAFETY_COPY =
    "Do not stop or change prescribed supplements without consulting your gynecologist."

@Composable
fun SupplementTrackingScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SupplementTrackingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    when {
        state.isLoading -> SupplementLoadingState(modifier)
        else -> SupplementContent(
            form = state.form,
            activeSupplements = state.activeSupplements,
            inactiveSupplements = state.inactiveSupplements,
            isSaving = state.isSaving,
            successMessage = state.successMessage,
            errorMessage = state.errorMessage,
            onNameChange = viewModel::updateName,
            onDoseChange = viewModel::updateDose,
            onFrequencyChange = viewModel::updateFrequency,
            onTimeOfDayChange = viewModel::updateTimeOfDay,
            onPrescribedByChange = viewModel::updatePrescribedBy,
            onInstructionsChange = viewModel::updateInstructions,
            onStartDateChange = viewModel::updateStartDate,
            onEndDateChange = viewModel::updateEndDate,
            onToggleActive = viewModel::toggleActive,
            onSave = { viewModel.save(uid.orEmpty()) },
            onCancelEdit = viewModel::cancelEdit,
            onEdit = viewModel::edit,
            onDeactivate = { viewModel.deactivate(uid.orEmpty(), it) },
            onMarkTaken = { viewModel.markTaken(uid.orEmpty(), it) },
            onBackToHome = onBackToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun SupplementLoadingState(modifier: Modifier = Modifier) {
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
            text = "Loading supplement tracker...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SupplementContent(
    form: SupplementFormState,
    activeSupplements: List<SupplementWithTodayStatus>,
    inactiveSupplements: List<SupplementWithTodayStatus>,
    isSaving: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onNameChange: (String) -> Unit,
    onDoseChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onTimeOfDayChange: (String) -> Unit,
    onPrescribedByChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onToggleActive: () -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
    onEdit: (SupplementWithTodayStatus) -> Unit,
    onDeactivate: (String) -> Unit,
    onMarkTaken: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val allSupplements = activeSupplements + inactiveSupplements
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
                text = "Supplements",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        SafetyCopyCard()
        TodaySupplementStatusCard(activeSupplements)
        SupplementFormCard(
            form = form,
            isSaving = isSaving,
            onNameChange = onNameChange,
            onDoseChange = onDoseChange,
            onFrequencyChange = onFrequencyChange,
            onTimeOfDayChange = onTimeOfDayChange,
            onPrescribedByChange = onPrescribedByChange,
            onInstructionsChange = onInstructionsChange,
            onStartDateChange = onStartDateChange,
            onEndDateChange = onEndDateChange,
            onToggleActive = onToggleActive,
            onSave = onSave,
            onCancelEdit = onCancelEdit,
        )
        ReminderPlaceholderCard()

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

        SupplementListSection(
            title = "Active supplements",
            emptyText = "No active prescribed supplements or pills added yet.",
            items = activeSupplements,
            isSaving = isSaving,
            onEdit = onEdit,
            onDeactivate = onDeactivate,
            onMarkTaken = onMarkTaken,
        )
        if (inactiveSupplements.isNotEmpty()) {
            SupplementListSection(
                title = "Inactive supplements",
                emptyText = "No inactive supplements.",
                items = inactiveSupplements,
                isSaving = isSaving,
                onEdit = onEdit,
                onDeactivate = onDeactivate,
                onMarkTaken = onMarkTaken,
            )
        }
        Text(
            text = "Total tracked supplements: ${allSupplements.size}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SafetyCopyCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Medication safety",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(
                text = SUPPLEMENT_SAFETY_COPY,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun TodaySupplementStatusCard(items: List<SupplementWithTodayStatus>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = SupplementStatusMapper.todayStatus(items),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SupplementFormCard(
    form: SupplementFormState,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onDoseChange: (String) -> Unit,
    onFrequencyChange: (String) -> Unit,
    onTimeOfDayChange: (String) -> Unit,
    onPrescribedByChange: (String) -> Unit,
    onInstructionsChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onToggleActive: () -> Unit,
    onSave: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (form.isEditing) "Edit supplement" else "Add prescribed supplement or pill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                placeholder = { Text("Folic Acid") },
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.dose,
                onValueChange = onDoseChange,
                label = { Text("Dose") },
                placeholder = { Text("Use prescribed wording, e.g. 400 mcg") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.frequency,
                    onValueChange = onFrequencyChange,
                    label = { Text("Frequency") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.timeOfDay,
                    onValueChange = onTimeOfDayChange,
                    label = { Text("Time") },
                    supportingText = { Text("HH:MM") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.prescribedBy,
                onValueChange = onPrescribedByChange,
                label = { Text("Prescribed by") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.startDate,
                    onValueChange = onStartDateChange,
                    label = { Text("Start date") },
                    supportingText = { Text("YYYY-MM-DD") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = form.endDate,
                    onValueChange = onEndDateChange,
                    label = { Text("End date") },
                    supportingText = { Text("Optional") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.instructions,
                onValueChange = onInstructionsChange,
                label = { Text("Instructions") },
                placeholder = { Text("Take after breakfast") },
                minLines = 2,
            )
            OutlinedButton(onClick = onToggleActive) {
                Text(if (form.active) "Active: yes" else "Active: no")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onSave,
                    enabled = !isSaving,
                ) {
                    Text(if (isSaving) "Saving..." else if (form.isEditing) "Update" else "Add")
                }
                if (form.isEditing) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onCancelEdit,
                        enabled = !isSaving,
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderPlaceholderCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Reminder architecture",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Times are stored with each supplement so notification reminders can be connected in the reminders phase.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SupplementListSection(
    title: String,
    emptyText: String,
    items: List<SupplementWithTodayStatus>,
    isSaving: Boolean,
    onEdit: (SupplementWithTodayStatus) -> Unit,
    onDeactivate: (String) -> Unit,
    onMarkTaken: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (items.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = emptyText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items.forEach { item ->
                SupplementCard(
                    item = item,
                    isSaving = isSaving,
                    onEdit = onEdit,
                    onDeactivate = onDeactivate,
                    onMarkTaken = onMarkTaken,
                )
            }
        }
    }
}

@Composable
private fun SupplementCard(
    item: SupplementWithTodayStatus,
    isSaving: Boolean,
    onEdit: (SupplementWithTodayStatus) -> Unit,
    onDeactivate: (String) -> Unit,
    onMarkTaken: (String) -> Unit,
) {
    val supplement = item.supplement
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = supplement.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = listOf(supplement.dose, supplement.frequency, supplement.timeOfDay)
                    .filter { it.isNotBlank() }
                    .joinToString(separator = " · "),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Prescribed by: ${supplement.prescribedBy}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            supplement.instructions.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "Dates: ${supplement.startDate}${supplement.endDate?.let { " to $it" }.orEmpty()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (item.isTakenToday) "Taken today" else "Not marked taken today",
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.isTakenToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Medium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onEdit(item) },
                    enabled = !isSaving,
                ) {
                    Text("Edit")
                }
                if (supplement.active) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onDeactivate(supplement.id) },
                        enabled = !isSaving,
                    ) {
                        Text("Deactivate")
                    }
                }
            }
            if (supplement.active) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onMarkTaken(supplement.id) },
                    enabled = !isSaving && !item.isTakenToday,
                ) {
                    Text(if (item.isTakenToday) "Already marked taken" else "Mark taken today")
                }
            }
        }
    }
}
