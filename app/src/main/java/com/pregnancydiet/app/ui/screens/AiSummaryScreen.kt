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
import com.pregnancydiet.app.ai.AiNutritionGapGuidance
import com.pregnancydiet.app.ai.AiNutritionEstimate
import com.pregnancydiet.app.ai.AiNutritionEstimateSource
import com.pregnancydiet.app.ai.AiNutritionEstimates
import com.pregnancydiet.app.ai.AiSummaryAction
import com.pregnancydiet.app.ai.AiSummaryRecord
import com.pregnancydiet.app.ai.AiSummaryViewModel
import com.pregnancydiet.app.ai.AiSymptomGuidance
import com.pregnancydiet.app.ai.AiWeightContext
import com.pregnancydiet.app.common.AppConstants
import java.util.Locale

@Composable
fun AiSummaryScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    onOpenAiUsage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiSummaryViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    AiSummaryContent(
        selectedDate = state.selectedDate,
        activeAction = state.activeAction,
        latestSummary = state.latestSummary,
        successMessage = state.successMessage,
        errorMessage = state.errorMessage,
        isBusy = state.isBusy,
        onDateChange = viewModel::updateSelectedDate,
        onGenerateDailyInsight = { viewModel.generateDailyInsight(uid.orEmpty()) },
        onGenerateSymptomGuidance = { viewModel.generateSymptomGuidance(uid.orEmpty()) },
        onGenerateWeeklySummary = { viewModel.generateWeeklySummary(uid.orEmpty()) },
        onBackToHome = onBackToHome,
        onOpenAiUsage = onOpenAiUsage,
        modifier = modifier,
    )
}

@Composable
private fun AiSummaryContent(
    selectedDate: String,
    activeAction: AiSummaryAction?,
    latestSummary: AiSummaryRecord?,
    successMessage: String?,
    errorMessage: String?,
    isBusy: Boolean,
    onDateChange: (String) -> Unit,
    onGenerateDailyInsight: () -> Unit,
    onGenerateSymptomGuidance: () -> Unit,
    onGenerateWeeklySummary: () -> Unit,
    onBackToHome: () -> Unit,
    onOpenAiUsage: () -> Unit,
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
                text = "AI insights",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        AiSafetyIntroCard()
        AiUsageCompactIndicator(onOpenAiUsage = onOpenAiUsage)
        AiSummaryControls(
            selectedDate = selectedDate,
            activeAction = activeAction,
            isBusy = isBusy,
            onDateChange = onDateChange,
            onGenerateDailyInsight = onGenerateDailyInsight,
            onGenerateSymptomGuidance = onGenerateSymptomGuidance,
            onGenerateWeeklySummary = onGenerateWeeklySummary,
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

        if (latestSummary == null) {
            EmptyAiSummaryCard()
        } else {
            AiSummaryResultSection(latestSummary)
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
private fun AiSafetyIntroCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Educational summaries only",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "AI uses your structured logs through a backend boundary. It cannot diagnose, prescribe, or change medications. Local red-flag checks still control urgent warnings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AiSummaryControls(
    selectedDate: String,
    activeAction: AiSummaryAction?,
    isBusy: Boolean,
    onDateChange: (String) -> Unit,
    onGenerateDailyInsight: () -> Unit,
    onGenerateSymptomGuidance: () -> Unit,
    onGenerateWeeklySummary: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Generate insight",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = selectedDate,
                onValueChange = onDateChange,
                label = { Text("Date") },
                supportingText = { Text("Use YYYY-MM-DD. Weekly summaries use the 7 days ending on this date.") },
                singleLine = true,
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onGenerateDailyInsight,
                enabled = !isBusy,
            ) {
                Text(activeAction.labelOr("Generate Daily Insight", AiSummaryAction.DailyInsight))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onGenerateSymptomGuidance,
                enabled = !isBusy,
            ) {
                Text(activeAction.labelOr("Generate Symptom Guidance", AiSummaryAction.SymptomGuidance))
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onGenerateWeeklySummary,
                enabled = !isBusy,
            ) {
                Text(activeAction.labelOr("Generate Weekly Summary", AiSummaryAction.WeeklySummary))
            }
            if (activeAction != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Generating ${activeAction.label}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAiSummaryCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "No AI summary shown yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Generate a daily insight after logging meals, symptoms, supplements, and nutrition data. If the backend is unavailable, a safe local fallback appears and logging remains unaffected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AiSummaryResultSection(summary: AiSummaryRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (summary.urgentWarning) {
            UrgentAiWarning(summary)
        }
        AiSummaryCard(summary)
        NutritionEstimateSection(summary)
        if (summary.nutritionGaps.isNotEmpty()) {
            NutritionGapGuidanceSection(summary.nutritionGaps)
        }
        if (summary.recommendations.isNotEmpty()) {
            GuidanceListCard("Recommendations", summary.recommendations)
        }
        if (summary.safetyWarnings.isNotEmpty()) {
            GuidanceListCard("Safety reminders", summary.safetyWarnings)
        }
        summary.symptomGuidance?.let { SymptomGuidanceCard(it) }
        summary.weightContext?.let { WeightContextCard(it) }
        if (summary.nextSteps.isNotEmpty()) {
            NextStepsCard(summary.nextSteps)
        }
        DisclaimerCard(summary.disclaimer)
    }
}

@Composable
private fun UrgentAiWarning(summary: AiSummaryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Seek medical advice urgently",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = "A red-flag symptom was detected. This app cannot assess emergencies. Contact your gynecologist, maternity unit, or local emergency services now, especially if symptoms are severe, sudden, or worsening.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            summary.urgentReasons.forEach { reason ->
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
private fun AiSummaryCard(summary: AiSummaryRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = summary.requestType.wireValue.replace('_', ' ').replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (summary.fallback) {
                Text(
                    text = "Safe fallback shown",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = summary.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            summary.stageContext.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            if (summary.fallback) {
                Text(
                    text = "AI details were unavailable, so this safe local fallback is shown. Raw technical details are kept out of the app screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun NutritionEstimateSection(summary: AiSummaryRecord) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Nutrition estimates",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = summary.nutritionEstimateSource.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (summary.nutritionEstimateSource == AiNutritionEstimateSource.MixedAiLocal) {
                    Text(
                        text = "Some values were estimated locally because the AI response was incomplete.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                summary.nutritionEstimateNote.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "Nutrition values are estimates based on your logged foods. AI-assisted estimates may be approximate and should not replace medical advice.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        summary.nutritionEstimates.rows().forEach { row ->
            NutritionEstimateRow(row)
        }
    }
}

@Composable
private fun NutritionEstimateRow(row: NutritionEstimateDisplayRow) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = row.label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${row.estimate.value.formatShort()} ${row.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${row.estimate.source.displaySourceLabel()} · Confidence: ${row.estimate.confidence}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = row.estimate.explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NutritionGapGuidanceSection(gaps: List<AiNutritionGapGuidance>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Food-based suggestions",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        gaps.forEach { gap ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = gap.nutrient,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Status: ${gap.status}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = gap.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (gap.foodSuggestions.isNotEmpty()) {
                        Text(
                            text = "Foods: ${gap.foodSuggestions.joinToString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    gap.safetyNote.takeIf { it.isNotBlank() }?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SymptomGuidanceCard(guidance: AiSymptomGuidance) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Symptom guidance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text("Severity context: ${guidance.severity}")
            Text(
                text = guidance.commonContext,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GuidanceList("Self-care", guidance.selfCare)
            GuidanceList("Contact doctor if", guidance.contactDoctorIf)
        }
    }
}

@Composable
private fun WeightContextCard(context: AiWeightContext) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Weight context",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = context.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (context.doctorDiscussionRecommended) {
                Text(
                    text = "Consider discussing this trend with your gynecologist.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun NextStepsCard(nextSteps: List<String>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Next steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            nextSteps.forEach { step ->
                Text(
                    text = "• $step",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun DisclaimerCard(disclaimer: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = disclaimer.ifBlank { AppConstants.MEDICAL_DISCLAIMER },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
    }
}

@Composable
private fun GuidanceList(
    title: String,
    items: List<String>,
) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        items.forEach { item ->
            Text(
                text = "• $item",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GuidanceListCard(
    title: String,
    items: List<String>,
) {
    if (items.isEmpty()) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class NutritionEstimateDisplayRow(
    val label: String,
    val unit: String,
    val estimate: AiNutritionEstimate,
)

private fun AiNutritionEstimates.rows(): List<NutritionEstimateDisplayRow> = listOf(
    NutritionEstimateDisplayRow("Calories", "kcal", caloriesKcal),
    NutritionEstimateDisplayRow("Protein", "g", proteinGrams),
    NutritionEstimateDisplayRow("Carbs", "g", carbsGrams),
    NutritionEstimateDisplayRow("Fat", "g", fatGrams),
    NutritionEstimateDisplayRow("Fiber", "g", fiberGrams),
    NutritionEstimateDisplayRow("Folate", "mcg", folateMcg),
    NutritionEstimateDisplayRow("Iron", "mg", ironMg),
    NutritionEstimateDisplayRow("Calcium", "mg", calciumMg),
    NutritionEstimateDisplayRow("Vitamin D", "mcg", vitaminDMcg),
    NutritionEstimateDisplayRow("Vitamin B12", "mcg", vitaminB12Mcg),
    NutritionEstimateDisplayRow("Iodine", "mcg", iodineMcg),
    NutritionEstimateDisplayRow("Omega-3", "mg", omega3Mg),
    NutritionEstimateDisplayRow("Choline", "mg", cholineMg),
    NutritionEstimateDisplayRow("Water", "ml", waterMl),
)

private fun String.displaySourceLabel(): String = when (this) {
    "ai" -> "AI-assisted"
    "local" -> "Local fallback"
    else -> replaceFirstChar { it.uppercase() }
}

private fun Double.formatShort(): String = String.format(Locale.US, "%.1f", this)

private fun AiSummaryAction?.labelOr(
    defaultLabel: String,
    action: AiSummaryAction,
): String = if (this == action) "Generating..." else defaultLabel
