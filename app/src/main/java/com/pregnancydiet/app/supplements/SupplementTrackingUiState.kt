package com.pregnancydiet.app.supplements

import com.pregnancydiet.app.model.SupplementWithTodayStatus

data class SupplementTrackingUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val form: SupplementFormState = SupplementFormState(),
    val supplements: List<SupplementWithTodayStatus> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    val activeSupplements: List<SupplementWithTodayStatus> = supplements.filter { it.supplement.active }
    val inactiveSupplements: List<SupplementWithTodayStatus> = supplements.filterNot { it.supplement.active }
}
