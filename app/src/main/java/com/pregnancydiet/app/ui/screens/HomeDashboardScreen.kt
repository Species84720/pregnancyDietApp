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
import com.pregnancydiet.app.home.DashboardPlaceholderCard
import com.pregnancydiet.app.home.HomeDashboard
import com.pregnancydiet.app.home.HomeDashboardMapper
import com.pregnancydiet.app.home.HomeDashboardViewModel

@Composable
fun HomeDashboardScreen(
    uid: String?,
    onAddMeal: () -> Unit,
    onAddSymptom: () -> Unit,
    onAddSupplement: () -> Unit,
    onViewNutrition: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeDashboardViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val dashboard = state.dashboard

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    when {
        state.isLoading -> DashboardLoadingState(modifier)
        state.errorMessage != null -> DashboardErrorState(
            message = state.errorMessage.orEmpty(),
            onRetry = { uid?.let(viewModel::load) },
            modifier = modifier,
        )
        state.isEmpty -> DashboardEmptyState(modifier)
        dashboard != null -> DashboardContent(
            dashboard = dashboard,
            onAddMeal = onAddMeal,
            onAddSymptom = onAddSymptom,
            onAddSupplement = onAddSupplement,
            onViewNutrition = onViewNutrition,
            modifier = modifier,
        )
    }
}

@Composable
private fun DashboardLoadingState(modifier: Modifier = Modifier) {
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
            text = "Loading your pregnancy dashboard...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DashboardErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Could not load dashboard",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Button(
            modifier = Modifier.padding(top = 24.dp),
            onClick = onRetry,
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun DashboardEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No active pregnancy profile",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = "Complete onboarding to see pregnancy week, trimester, due date, and weight details here.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DashboardContent(
    dashboard: HomeDashboard,
    onAddMeal: () -> Unit,
    onAddSymptom: () -> Unit,
    onAddSupplement: () -> Unit,
    onViewNutrition: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Home dashboard",
            style = MaterialTheme.typography.headlineMedium,
        )
        PregnancyOverviewCard(dashboard)
        QuickActionsCard(
            onAddMeal = onAddMeal,
            onAddSymptom = onAddSymptom,
            onAddSupplement = onAddSupplement,
            onViewNutrition = onViewNutrition,
        )
        TodayPlaceholderSection(dashboard)
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PregnancyOverviewCard(dashboard: HomeDashboard) {
    val progress = dashboard.progress
    val profile = dashboard.pregnancyProfile

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Pregnancy progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = progress.pregnancyWeek?.let { week ->
                    "Week $week${progress.dayWithinWeek?.let { ", day $it" }.orEmpty()}"
                } ?: "Pregnancy week not available",
                style = MaterialTheme.typography.headlineSmall,
            )
            InfoRow("Trimester", progress.trimester?.toString() ?: "Not available")
            InfoRow("Estimated due date", progress.estimatedDueDate?.toString() ?: "Not available")
            InfoRow("Countdown", HomeDashboardMapper.countdownLabel(dashboard.countdownDays))
            InfoRow("Current weight", HomeDashboardMapper.currentWeightLabel(profile))
            progress.message?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    onAddMeal: () -> Unit,
    onAddSymptom: () -> Unit,
    onAddSupplement: () -> Unit,
    onViewNutrition: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Quick actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onAddMeal,
                ) { Text("Add Meal") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onAddSymptom,
                ) { Text("Add Symptom") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onAddSupplement,
                ) { Text("Add Supplement") }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onViewNutrition,
                ) { Text("View Nutrition") }
            }
        }
    }
}

@Composable
private fun TodayPlaceholderSection(dashboard: HomeDashboard) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        HomeDashboardMapper.placeholderCards(dashboard).forEach { card ->
            PlaceholderStatusCard(card)
        }
    }
}

@Composable
private fun PlaceholderStatusCard(card: DashboardPlaceholderCard) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = card.status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
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
        )
    }
}