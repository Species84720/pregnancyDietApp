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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit,
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
                text = "Privacy policy",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBack) {
                Text("Settings")
            }
        }
        PolicyCard(
            title = "Sensitive health-related data",
            body = "Pregnancy profile details, symptoms, supplements, meals, weight logs, nutrition summaries, reminders, reports, and AI summary records are treated as sensitive data.",
        )
        PolicyCard(
            title = "User-scoped storage",
            body = "App data is stored under users/{uid} in Cloud Firestore. Security rules allow reads and writes only when the authenticated user ID matches that path.",
        )
        PolicyCard(
            title = "AI processing control",
            body = "AI summaries use minimal structured context through a backend boundary. You can turn off new AI summary generation from Settings. Provider secrets must never be stored in the Android app.",
        )
        PolicyCard(
            title = "Exports and deletion",
            body = "Reports are generated locally from your scoped data for sharing with a clinician. You can delete account data from Settings; Firebase Authentication may require recent sign-in to complete account deletion.",
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PolicyCard(
    title: String,
    body: String,
) {
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
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}