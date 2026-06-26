package com.pregnancydiet.app.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementLog
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightLog
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import com.pregnancydiet.app.reports.GynecologistReport
import com.pregnancydiet.app.reports.ReportWeeklyAiSummary
import com.pregnancydiet.app.reports.ReportsViewModel
import java.util.Locale

@Composable
fun ReportsScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }
    LaunchedEffect(state.exportText) {
        state.exportText?.let { exportText ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Pregnancy Diet Tracker report")
                putExtra(Intent.EXTRA_TEXT, exportText)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share factual report"))
            viewModel.exportConsumed()
        }
    }

    when {
        state.isLoading -> ReportsLoadingState(modifier)
        else -> ReportsContent(
            startDateText = state.startDateText,
            endDateText = state.endDateText,
            report = state.report,
            successMessage = state.successMessage,
            errorMessage = state.errorMessage,
            onStartDateChange = viewModel::updateStartDate,
            onEndDateChange = viewModel::updateEndDate,
            onLoadReport = { viewModel.generateReport(uid.orEmpty()) },
            onPrepareExport = viewModel::prepareExport,
            onBackToHome = onBackToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun ReportsLoadingState(modifier: Modifier = Modifier) {
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
            text = "Loading report...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ReportsContent(
    startDateText: String,
    endDateText: String,
    report: GynecologistReport?,
    successMessage: String?,
    errorMessage: String?,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onLoadReport: () -> Unit,
    onPrepareExport: () -> Unit,
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
                text = "Reports",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        ReportIntroCard()
        DateRangeCard(
            startDateText = startDateText,
            endDateText = endDateText,
            onStartDateChange = onStartDateChange,
            onEndDateChange = onEndDateChange,
            onLoadReport = onLoadReport,
            onPrepareExport = onPrepareExport,
            canExport = report != null,
        )
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

        if (report == null) {
            EmptyReportCard()
        } else {
            ReportOverviewCard(report)
            PregnancyReportCard(report.pregnancyProfile, report.pregnancyProgress)
            SymptomsReportCard(report.symptomLogs)
            SupplementsReportCard(report.supplements, report.supplementLogs)
            MealsReportCard(report.mealLogs)
            NutritionReportCard(report.nutritionSummaries)
            WeightReportCard(report.weightLogs)
            WeeklyAiReportCard(report.weeklyAiSummaries)
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
private fun ReportIntroCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Factual tracking report",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Use this report to review logged symptoms, supplements, meals, nutrition gaps, weight logs, and weekly AI summaries. It avoids diagnosis and medical conclusions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DateRangeCard(
    startDateText: String,
    endDateText: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onLoadReport: () -> Unit,
    onPrepareExport: () -> Unit,
    canExport: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Date range",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = startDateText,
                    onValueChange = onStartDateChange,
                    label = { Text("Start date") },
                    supportingText = { Text("YYYY-MM-DD") },
                    singleLine = true,
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = endDateText,
                    onValueChange = onEndDateChange,
                    label = { Text("End date") },
                    supportingText = { Text("YYYY-MM-DD") },
                    singleLine = true,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onLoadReport,
                ) { Text("Load report") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onPrepareExport,
                    enabled = canExport,
                ) { Text("Share text") }
            }
        }
    }
}

@Composable
private fun EmptyReportCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "No report loaded yet. Choose a date range and load a report.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReportOverviewCard(report: GynecologistReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Report summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            InfoLine("Range", report.dateRange.label)
            InfoLine("Symptoms", "${report.symptomLogs.size} logs · ${report.urgentSymptomCount} urgent warnings")
            InfoLine("Supplements", "${report.activeSupplements.size} active · ${report.supplementTakenCount} taken logs")
            InfoLine("Meals", "${report.mealLogs.size} logged meals")
            InfoLine("Nutrition summaries", report.nutritionSummaries.size.toString())
            InfoLine("Weight logs", report.weightLogs.size.toString())
            Text(
                text = "This summary is factual tracking data only.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun PregnancyReportCard(
    profile: PregnancyProfile?,
    progress: PregnancyProgress?,
) {
    ReportSectionCard(title = "Pregnancy context") {
        if (profile == null) {
            Text("No active pregnancy profile found.")
        } else {
            Text("Type: ${profile.pregnancyType.firestoreValue}")
            Text("Current calculated week: ${progress?.pregnancyWeek?.toString() ?: "not available"}")
            Text("Trimester: ${progress?.trimester?.toString() ?: "not available"}")
            Text("Estimated due date: ${progress?.estimatedDueDate?.toString() ?: profile.estimatedDueDate ?: "not available"}")
            Text("Current weight: ${profile.currentWeightKg.formatOne()} kg")
        }
    }
}

@Composable
private fun SymptomsReportCard(symptoms: List<SymptomLog>) {
    ReportSectionCard(title = "Symptom history") {
        if (symptoms.isEmpty()) {
            Text("No symptoms logged in this range.")
        } else {
            symptoms.take(MAX_PREVIEW_ITEMS).forEach { log ->
                Text("${log.date} · week ${log.pregnancyWeek ?: "?"}: ${log.symptoms.joinToString { "${it.name} ${it.severity}/10" }}")
                if (log.urgentFlag) Text("Urgent warning shown: ${log.urgentReasons.joinToString()}", color = MaterialTheme.colorScheme.error)
            }
            PreviewFooter(symptoms.size)
        }
    }
}

@Composable
private fun SupplementsReportCard(
    supplements: List<Supplement>,
    logs: List<SupplementLog>,
) {
    ReportSectionCard(title = "Supplement adherence") {
        if (supplements.isEmpty()) {
            Text("No supplements tracked.")
        } else {
            supplements.take(MAX_PREVIEW_ITEMS).forEach { supplement ->
                val takenCount = logs.count { it.supplementId == supplement.id && it.taken }
                Text("${supplement.name}: ${supplement.dose} · ${supplement.frequency} · taken logs $takenCount")
            }
            PreviewFooter(supplements.size)
        }
    }
}

@Composable
private fun MealsReportCard(meals: List<MealLog>) {
    ReportSectionCard(title = "Meal history") {
        if (meals.isEmpty()) {
            Text("No meals logged in this range.")
        } else {
            meals.take(MAX_PREVIEW_ITEMS).forEach { meal ->
                Text("${meal.date} · ${meal.mealType.label}: ${meal.items.joinToString { it.foodName }}")
            }
            PreviewFooter(meals.size)
        }
    }
}

@Composable
private fun NutritionReportCard(summaries: List<DailyNutritionSummary>) {
    ReportSectionCard(title = "Nutrition gaps") {
        if (summaries.isEmpty()) {
            Text("No nutrition summaries generated in this range.")
        } else {
            summaries.take(MAX_PREVIEW_ITEMS).forEach { summary ->
                val gaps = if (summary.gaps.isEmpty()) "No detected low/high gaps" else summary.gaps.joinToString { it.label + " " + it.status.firestoreValue }
                Text("${summary.date} · week ${summary.pregnancyWeek ?: "?"}: $gaps")
            }
            PreviewFooter(summaries.size)
        }
    }
}

@Composable
private fun WeightReportCard(weightLogs: List<WeightLog>) {
    ReportSectionCard(title = "Weight trend") {
        if (weightLogs.isEmpty()) {
            Text("No weight logs in this range.")
        } else {
            weightLogs.forEach { log ->
                Text("${log.date} · week ${log.pregnancyWeek ?: "?"}: ${log.weightKg.formatOne()} kg")
            }
            if (weightLogs.size >= 2) {
                val change = weightLogs.last().weightKg - weightLogs.first().weightKg
                Text("Range change: ${change.formatOne()} kg")
            }
        }
    }
}

@Composable
private fun WeeklyAiReportCard(summaries: List<ReportWeeklyAiSummary>) {
    ReportSectionCard(title = "Weekly AI summaries") {
        if (summaries.isEmpty()) {
            Text("No weekly AI summaries saved in this range.")
        } else {
            summaries.take(MAX_PREVIEW_ITEMS).forEach { summary ->
                Text("${summary.weekId} · week ${summary.pregnancyWeek ?: "?"}: ${summary.summary}")
                if (summary.fallback) Text("Safe fallback summary was used.")
            }
            PreviewFooter(summaries.size)
        }
    }
}

@Composable
private fun ReportSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            content()
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun PreviewFooter(totalCount: Int) {
    if (totalCount > MAX_PREVIEW_ITEMS) {
        Text(
            text = "Showing first $MAX_PREVIEW_ITEMS of $totalCount. Shared report includes all items.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun Double.formatOne(): String = String.format(Locale.US, "%.1f", this)

private const val MAX_PREVIEW_ITEMS = 5
