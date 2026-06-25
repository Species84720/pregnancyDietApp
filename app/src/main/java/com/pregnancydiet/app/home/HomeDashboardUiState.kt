package com.pregnancydiet.app.home

data class HomeDashboardUiState(
    val isLoading: Boolean = false,
    val dashboard: HomeDashboard? = null,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean = !isLoading && dashboard == null && errorMessage == null
}