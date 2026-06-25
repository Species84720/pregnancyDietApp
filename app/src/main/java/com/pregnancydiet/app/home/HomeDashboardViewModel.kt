package com.pregnancydiet.app.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.HomeDashboardRepository
import com.pregnancydiet.app.firebase.FirestoreHomeDashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeDashboardViewModel(
    private val repository: HomeDashboardRepository = FirestoreHomeDashboardRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeDashboardUiState(isLoading = true))
    val uiState: StateFlow<HomeDashboardUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = HomeDashboardUiState(errorMessage = "Sign in again to load your dashboard.")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loadHomeDashboard(uid)
            _uiState.value = HomeDashboardUiState(
                isLoading = false,
                dashboard = result.getOrNull(),
                errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
            )
        }
    }
}

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Could not load your pregnancy dashboard. Please try again."