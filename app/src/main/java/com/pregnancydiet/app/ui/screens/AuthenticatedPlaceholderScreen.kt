package com.pregnancydiet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.model.AuthenticatedUser

@Composable
fun AuthenticatedPlaceholderScreen(
    title: String,
    description: String,
    user: AuthenticatedUser?,
    errorMessage: String?,
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
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            modifier = Modifier.padding(top = 12.dp),
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        user?.email?.let { email ->
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = "Signed in as $email",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        errorMessage?.let {
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}