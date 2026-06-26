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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.ai.AiAccessMode
import com.pregnancydiet.app.ai.AiUsageEvent
import com.pregnancydiet.app.ai.AiUsageStatus
import com.pregnancydiet.app.ai.AiUsageUiState
import com.pregnancydiet.app.ai.AiUsageViewModel
import com.pregnancydiet.app.ai.compactStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AiUsageScreen(
    onBackToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiUsageViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AiUsageContent(
        state = state,
        onBackToSettings = onBackToSettings,
        onRefreshStatus = viewModel::refreshStatus,
        onUseFreeHourly = viewModel::useFreeHourly,
        onCredentialChange = viewModel::updateCredentialInput,
        onConnectAccount = viewModel::connectAccount,
        onDisconnectAccount = viewModel::disconnectAccount,
        modifier = modifier,
    )
}

@Composable
fun AiUsageCompactIndicator(
    onOpenAiUsage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AiUsageViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("AI status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(state.compactStatus, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OutlinedButton(onClick = onOpenAiUsage) {
                Text("AI Usage")
            }
        }
    }
}

@Composable
private fun AiUsageContent(
    state: AiUsageUiState,
    onBackToSettings: () -> Unit,
    onRefreshStatus: () -> Unit,
    onUseFreeHourly: () -> Unit,
    onCredentialChange: (String) -> Unit,
    onConnectAccount: () -> Unit,
    onDisconnectAccount: () -> Unit,
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
            Text("AI Usage", style = MaterialTheme.typography.headlineMedium)
            OutlinedButton(onClick = onBackToSettings) { Text("Settings") }
        }

        StatusCard(state)
        FreeHourlyCard(state, onUseFreeHourly)
        UserAccountCard(state, onCredentialChange, onConnectAccount, onDisconnectAccount)
        UsageCountersCard(state)
        RecentActivityCard(state.recentEvents)

        Text(
            text = "For free hourly AI, usage is estimated on this device. Pollinations applies the actual limit by IP/key, so usage on the same Wi-Fi or network may affect availability. For connected accounts, usage depends on your own Pollinations allowance and provider limits.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = onRefreshStatus) { Text("Refresh status") }
        state.successMessage?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun StatusCard(state: AiUsageUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Current provider: Pollinations AI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Access mode: ${if (state.credentialState.selectedAccessMode == AiAccessMode.USER_ACCOUNT) "Pollinations account" else "Free hourly AI"}")
            Text("Key/account mode: ${if (state.credentialState.usingUserAccount) "Using your Pollinations account" else "App publishable key"}")
            Text("Status: ${state.compactStatus}", color = if (state.credentialState.setupRequired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            state.usageState.nextEstimatedFreeRequestAtMillis?.takeIf { it > System.currentTimeMillis() }?.let {
                Text("Countdown: next estimated free request at ${it.formatTime()}")
            }
        }
    }
}

@Composable
private fun FreeHourlyCard(
    state: AiUsageUiState,
    onUseFreeHourly: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Free hourly AI", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Use the free Pollinations option. This is usually limited to about 1 pollen per IP per hour, so availability may be affected by other people on the same Wi-Fi or network.")
            Text("Estimated allowance: about 1 free pollen per IP per hour", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onUseFreeHourly, enabled = state.credentialState.selectedAccessMode != AiAccessMode.FREE_HOURLY) {
                Text("Use free hourly AI")
            }
        }
    }
}

@Composable
private fun UserAccountCard(
    state: AiUsageUiState,
    onCredentialChange: (String) -> Unit,
    onConnectAccount: () -> Unit,
    onDisconnectAccount: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Use my Pollinations account", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("Connect your own Pollinations account or key so AI requests use your own allowance instead of the shared free hourly option.")
            Text("Estimated allowance: depends on your Pollinations account allowance", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.userCredentialInput,
                onValueChange = onCredentialChange,
                label = { Text("Pollinations user key") },
                supportingText = { Text("Paste your Pollinations user key. This will be stored securely on this device and used only for AI requests.") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
            )
            Button(modifier = Modifier.fillMaxWidth(), onClick = onConnectAccount) { Text("Connect account") }
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onDisconnectAccount, enabled = state.credentialState.hasUserCredential) { Text("Disconnect account") }
        }
    }
}

@Composable
private fun UsageCountersCard(state: AiUsageUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Usage today", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Successful AI requests: ${state.usageState.successfulRequestsToday}")
            Text("Rate-limited attempts: ${state.usageState.failedRateLimitCountToday}")
            Text("Quota exceeded count: ${state.usageState.quotaExceededCountToday}")
        }
    }
}

@Composable
private fun RecentActivityCard(events: List<AiUsageEvent>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            if (events.isEmpty()) {
                Text("No AI usage activity yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                events.forEach { event ->
                    Text("${event.timestampMillis.formatTime()} • ${event.feature.name.replace('_', ' ')} • ${event.status.name.replace('_', ' ')}", style = MaterialTheme.typography.bodySmall)
                    Text(event.shortMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Start)
                }
            }
        }
    }
}

private fun Long.formatTime(): String = DateTimeFormatter.ofPattern("HH:mm")
    .withZone(ZoneId.systemDefault())
    .format(Instant.ofEpochMilli(this))