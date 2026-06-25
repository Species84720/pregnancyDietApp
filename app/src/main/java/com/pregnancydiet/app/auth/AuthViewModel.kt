package com.pregnancydiet.app.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.UserProfileRepository
import com.pregnancydiet.app.firebase.FirebaseAuthRepository
import com.pregnancydiet.app.firebase.FirestoreUserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
    private val userProfileRepository: UserProfileRepository = FirestoreUserProfileRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        observeAuthState()
    }

    fun signInWithGoogle(idToken: String) {
        if (idToken.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Google sign-in did not return a valid token.",
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signInWithGoogleIdToken(idToken)
            result.exceptionOrNull()?.let { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        destination = AuthDestination.SignedOut,
                        errorMessage = error.toUserFacingMessage(),
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = authRepository.signOut()
            _uiState.value = AuthUiState(
                isLoading = false,
                destination = AuthDestination.SignedOut,
                errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
            )
        }
    }

    fun showError(message: String) {
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun observeAuthState() {
        viewModelScope.launch {
            authRepository.observeAuthState().collectLatest { user ->
                if (user == null) {
                    _uiState.value = AuthUiState(
                        isLoading = false,
                        destination = AuthDestination.SignedOut,
                    )
                    return@collectLatest
                }

                _uiState.update {
                    it.copy(
                        isLoading = true,
                        user = user,
                        errorMessage = null,
                    )
                }

                val profileResult = userProfileRepository.createOrUpdateUser(user)
                val profile = profileResult.getOrNull()
                _uiState.value = AuthUiState(
                    isLoading = false,
                    user = user,
                    userProfile = profile,
                    destination = AuthRouteResolver.resolve(user, profile),
                    errorMessage = profileResult.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }
}

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Authentication failed. Please try again."