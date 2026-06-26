package com.pregnancydiet.app.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AiUsageViewModel(
    private val accessModeRepository: AiAccessModeRepository = AiDependencyProvider.aiAccessModeRepository(),
    private val usageRepository: AiUsageRepository = AiDependencyProvider.aiUsageRepository(),
    private val credentialResolver: AiCredentialResolver = AiDependencyProvider.aiCredentialResolver(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiUsageUiState())
    val uiState: StateFlow<AiUsageUiState> = _uiState.asStateFlow()

    init {
        observeState()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            usageRepository.resetDailyCountersIfNeeded(System.currentTimeMillis())
            usageRepository.resetCurrentHourCountersIfNeeded(System.currentTimeMillis())
            _uiState.update { it.copy(successMessage = "AI usage status refreshed.", errorMessage = null) }
        }
    }

    fun updateCredentialInput(value: String) {
        _uiState.update { it.copy(userCredentialInput = value, successMessage = null, errorMessage = null) }
    }

    fun useFreeHourly() {
        viewModelScope.launch {
            accessModeRepository.setAccessMode(AiAccessMode.FREE_HOURLY)
            _uiState.update { it.copy(successMessage = "Free hourly AI is now active.", errorMessage = null) }
        }
    }

    fun connectAccount() {
        val credential = _uiState.value.userCredentialInput
        viewModelScope.launch {
            when (accessModeRepository.saveUserAccountCredential(credential)) {
                SaveCredentialResult.Success -> _uiState.update {
                    it.copy(userCredentialInput = "", successMessage = "Using your Pollinations account.", errorMessage = null)
                }
                SaveCredentialResult.Blank -> _uiState.update { it.copy(errorMessage = "Pollinations user key is required.", successMessage = null) }
                SaveCredentialResult.UnsafeSecretKey -> _uiState.update {
                    it.copy(
                        errorMessage = "This looks like a server secret key. Secret keys should not be stored in the mobile app. Please use a client-safe/user key if Pollinations provides one.",
                        successMessage = null,
                    )
                }
                SaveCredentialResult.InvalidFormat -> _uiState.update { it.copy(errorMessage = "This key does not look valid. Please check it and try again.", successMessage = null) }
                SaveCredentialResult.StorageError -> _uiState.update { it.copy(errorMessage = "Could not store the Pollinations credential securely on this device.", successMessage = null) }
            }
        }
    }

    fun disconnectAccount() {
        viewModelScope.launch {
            accessModeRepository.clearUserAccountCredential()
            _uiState.update {
                it.copy(
                    userCredentialInput = "",
                    successMessage = "Pollinations account disconnected. Free hourly AI is now active.",
                    errorMessage = null,
                )
            }
        }
    }

    private fun observeState() {
        viewModelScope.launch {
            combine(
                credentialResolver.observeCredentialState(),
                usageRepository.observeUsageState(),
                usageRepository.observeRecentUsageEvents(),
            ) { credentialState, usageState, events ->
                Triple(credentialState, usageState, events)
            }.collectLatest { (credentialState, usageState, events) ->
                _uiState.update {
                    it.copy(
                        credentialState = credentialState,
                        usageState = usageState,
                        recentEvents = events,
                    )
                }
            }
        }
    }
}