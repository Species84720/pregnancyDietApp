package com.pregnancydiet.app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

internal object FirebaseConfiguration {
    const val NOT_CONFIGURED_MESSAGE =
        "Firebase is not configured. Add app/google-services.json and the Google web client ID before signing in."

    fun authOrNull(): FirebaseAuth? = runCatching { FirebaseAuth.getInstance() }.getOrNull()

    fun firestoreOrNull(): FirebaseFirestore? = runCatching { FirebaseFirestore.getInstance() }.getOrNull()
}