package com.pregnancydiet.app.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pregnancydiet.app.auth.AuthRepository
import com.pregnancydiet.app.model.AuthenticatedUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {
    override fun observeAuthState(): Flow<AuthenticatedUser?> = callbackFlow {
        val firebaseAuth = FirebaseConfiguration.authOrNull()
        if (firebaseAuth == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthenticatedUser())
        }

        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthenticatedUser> = runCatching {
        val firebaseAuth = FirebaseConfiguration.authOrNull()
            ?: error(FirebaseConfiguration.NOT_CONFIGURED_MESSAGE)
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = firebaseAuth.signInWithCredential(credential).await()
        authResult.user?.toAuthenticatedUser() ?: error("Google sign-in did not return a Firebase user.")
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        FirebaseConfiguration.authOrNull()?.signOut()
        Unit
    }
}

private fun com.google.firebase.auth.FirebaseUser.toAuthenticatedUser(): AuthenticatedUser = AuthenticatedUser(
    uid = uid,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl?.toString(),
)