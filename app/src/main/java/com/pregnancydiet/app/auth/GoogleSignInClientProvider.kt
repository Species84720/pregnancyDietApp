package com.pregnancydiet.app.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.pregnancydiet.app.R

object GoogleSignInClientProvider {
    fun create(
        context: Context,
        webClientId: String,
    ): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, options)
    }

    fun resolveWebClientId(context: Context): String {
        val explicitClientId = context.getString(R.string.google_web_client_id).trim()
        if (explicitClientId.isNotBlank()) return explicitClientId

        val generatedResourceId = context.resources.getIdentifier(
            "default_web_client_id",
            "string",
            context.packageName,
        )

        return if (generatedResourceId != 0) {
            context.getString(generatedResourceId).trim()
        } else {
            ""
        }
    }
}