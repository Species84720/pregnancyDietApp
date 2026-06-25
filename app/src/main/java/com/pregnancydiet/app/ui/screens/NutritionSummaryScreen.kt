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
import com.pregnancydiet.app.model.DailyNutritionSummary
import com.pregnancydiet.app.model.NutrientAmounts
import com.pregnancydiet.app.model.NutritionGap
import com.pregnancydiet.app.model.NutritionStatus
import com.pregnancydiet.app.model.WeeklyNutritionTrend
import com.pregnancydiet.app.nutrition.NutrientCatalog
import com.pregnancydiet.app.nutrition.NutritionSummaryViewModel
import java.util.Locale

@Composable
fun NutritionSummaryScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NutritionSummaryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    when {
        state.isLoading -> NutritionLoadingState(modifier)
        else -> NutritionContent(
            selectedDate = state.selectedDate,
            dailySummary = state.dailySummary,
            weeklyTrend = state.weeklyTrend,
            isSaving = state.isSaving,
            successMessage = state.successMessage,
            errorMessage = state.errorMessage,
            onDateChange = viewModel::updateSelectedDate,
            onGenerate = { viewModel.generate(uid.orEmpty()) },
            onRefresh = { viewModel.load(uid.orEmpty()) },
            onBackToHome = onBackToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun NutritionLoadingState(modifier: Modifier = Modifier) {
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
            text = "Loading nutrition summary...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NutritionContent(
    selectedDate: String,
    dailySummary: DailyNutritionSummary?,
    weeklyTrend: WeeklyNutritionTrend,
    isSaving: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onDateChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
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
                text = "Nutrition",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        SummaryControls(
            selectedDate = selectedDate,
            isSaving = isSaving,
            onDateChange = onDateChange,
            onGenerate = onGenerate,
            onRefresh = onRefresh,
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

        if (dailySummary == null) {
            EmptyNutritionSummaryCard()
        } else {
            DailySummaryCard(dailySummary)
            StagePrioritiesCard(dailySummary.stagePriorities)
            GapSection(dailySummary.gaps)
            NutrientComparisonSection(
                totals = dailySummary.totals,
                targets = dailySummary.targets,
            )
        }

        WeeklyTrendCard(weeklyTrend)
        SafetyCopyCard()
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SummaryControls(
    selectedDate: String,
    isSaving: Boolean,
    onDateChange: (String) -> Unit,
    onGenerate: () -> Unit,
    onRefresh: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Daily summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = selectedDate,
                onValueChange = onDateChange,
                label = { Text("Date") },
                supportingText = { Text("Use YYYY-MM-DD. Generate after logging meals for this date.") },
                singleLine = true,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onGenerate,
                    enabled = !isSaving,
                ) {
                    Text(if (isSaving) "Generating..." else "Generate")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onRefresh,
                    enabled = !isSaving,
                ) {
                    Text("Refresh")
                }
            }
        }
    }
}

@Composable
private fun EmptyNutritionSummaryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No nutrition summary yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Log meals, then generate a deterministic summary. Targets adjust by pregnancy week, trimester, weight context, pregnancy type, restrictions, and medical conditions.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DailySummaryCard(summary: DailyNutritionSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "${summary.date} summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Week ${summary.pregnancyWeek?.toString() ?: "not available"} · Trimester ${summary.trimester?.toString() ?: "not available"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "Profile: ${summary.nutritionProfileVersion}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "This is a tracking summary, not a diagnosis. Discuss persistent concerns or prescribed supplement questions with your gynecologist.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun StagePrioritiesCard(priorities: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Stage priorities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (priorities.isEmpty()) "No stage priorities available." else priorities.joinToString(separator = " · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GapSection(gaps: List<NutritionGap>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Daily gaps",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (gaps.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "No low or high tracked nutrients were detected from today's logged meals.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            gaps.forEach { gap -> GapCard(gap) }
        }
    }
}

@Composable
private fun GapCard(gap: NutritionGap) {
    val statusText = when (gap.status) {
        NutritionStatus.Low -> "Below target"
        NutritionStatus.High -> "Above target"
        NutritionStatus.Adequate -> "Adequate"
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = gap.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "$statusText · ${gap.severity.firestoreValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (gap.status == NutritionStatus.Low) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = "Logged ${gap.total.formatShort()} ${gap.unit} of target ${gap.target.formatShort()} ${gap.unit}.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (gap.status == NutritionStatus.Low) {
                Text(
                    text = gap.foodSuggestion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NutrientComparisonSection(
    totals: NutrientAmounts,
    targets: NutrientAmounts,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Tracked nutrients",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        NutrientCatalog.tracked.forEach { definition ->
            val total = definition.amount(totals)
            val target = definition.amount(targets)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = definition.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "${total.formatShort()} / ${target.formatShort()} ${definition.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyTrendCard(trend: WeeklyNutritionTrend) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Weekly trends",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Days included: ${trend.daysIncluded}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (trend.repeatedGaps.isEmpty()) {
                    "No repeated low nutrient gaps detected from saved summaries yet."
                } else {
                    "Repeated gaps: ${trend.repeatedGaps.joinToString()}"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Average protein: ${trend.averageTotals.proteinGrams.formatShort()} g · Average iron: ${trend.averageTotals.ironMg.formatShort()} mg · Average calcium: ${trend.averageTotals.calciumMg.formatShort()} mg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SafetyCopyCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Food-first guidance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Use this summary for educational tracking. It does not change prescribed supplements or replace individualized guidance from your gynecologist, midwife, or dietitian.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun Double.formatShort(): String = String.format(Locale.US, "%.1f", this)
