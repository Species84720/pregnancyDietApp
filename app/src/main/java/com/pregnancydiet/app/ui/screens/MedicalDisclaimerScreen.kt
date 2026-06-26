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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pregnancydiet.app.common.AppConstants

@Composable
fun MedicalDisclaimerScreen(
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
                text = "Medical disclaimer",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBack) {
                Text("Settings")
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Educational wellness support only",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = AppConstants.MEDICAL_DISCLAIMER,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        DisclaimerCard(
            title = "When to seek urgent care",
            body = "Contact your gynecologist, maternity unit, or local emergency services urgently for red-flag symptoms such as vaginal bleeding, severe abdominal pain, severe headache, vision changes, high fever, fainting, chest pain, severe vomiting, sudden swelling of face or hands, allergic reaction symptoms, or reduced fetal movement later in pregnancy.",
        )
        DisclaimerCard(
            title = "Medication and supplement safety",
            body = "The app tracks prescribed pills and supplements but does not recommend starting, stopping, changing, or adjusting doses. Confirm any medication or supplement changes with your gynecologist.",
        )
        DisclaimerCard(
            title = "AI and nutrition summaries",
            body = "AI summaries are educational explanations based on structured logs. Deterministic app logic handles nutrition calculations and red-flag escalation; AI should not be used for diagnosis or emergency decisions.",
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DisclaimerCard(
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