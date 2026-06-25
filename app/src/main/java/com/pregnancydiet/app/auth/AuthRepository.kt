package com.pregnancydiet.app.auth

import com.pregnancydiet.app.model.AuthenticatedUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthState(): Flow<AuthenticatedUser?>

    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthenticatedUser>

    suspend fun signOut(): Result<Unit>
}