package com.pregnancydiet.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.pregnancydiet.app.auth.GoogleSignInClientProvider
import com.pregnancydiet.app.common.AppConstants

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onGoogleIdToken: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val webClientId = remember(context) { GoogleSignInClientProvider.resolveWebClientId(context) }
    val googleSignInClient = remember(context, webClientId) {
        if (webClientId.isBlank()) null else GoogleSignInClientProvider.create(context, webClientId)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                onError("Google sign-in did not return a valid token.")
            } else {
                onGoogleIdToken(idToken)
            }
        } catch (_: ApiException) {
            onError("Google sign-in was cancelled or failed. Please try again.")
        }
    }

    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Welcome to ${AppConstants.APP_NAME}",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = "Sign in to keep pregnancy, symptom, supplement, meal, and weight records private to your account.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                enabled = !isLoading,
                onClick = {
                    val client = googleSignInClient
                    if (client == null) {
                        onError("Google Sign-In is not configured. Add app/google-services.json and GOOGLE_WEB_CLIENT_ID.")
                    } else {
                        launcher.launch(client.signInIntent)
                    }
                },
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Continue with Google")
                }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}