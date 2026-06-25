package com.pregnancydiet.app.auth

import com.pregnancydiet.app.model.AuthenticatedUser
import com.pregnancydiet.app.model.UserProfile

data class AuthUiState(
    val isLoading: Boolean = true,
    val user: AuthenticatedUser? = null,
    val userProfile: UserProfile? = null,
    val destination: AuthDestination = AuthDestination.Loading,
    val errorMessage: String? = null,
)