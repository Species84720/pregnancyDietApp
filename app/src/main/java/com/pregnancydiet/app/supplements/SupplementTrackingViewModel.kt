package com.pregnancydiet.app.supplements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.SupplementRepository
import com.pregnancydiet.app.firebase.FirestoreSupplementRepository
import com.pregnancydiet.app.model.Supplement
import com.pregnancydiet.app.model.SupplementWithTodayStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class SupplementTrackingViewModel(
    private val repository: SupplementRepository = FirestoreSupplementRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupplementTrackingUiState())
    val uiState: StateFlow<SupplementTrackingUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = SupplementTrackingUiState(
                isLoading = false,
                errorMessage = "Sign in again to manage supplements.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loadSupplementsForDate(uid)
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    supplements = result.getOrNull().orEmpty(),
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun updateName(value: String) = updateForm { copy(name = value) }
    fun updateDose(value: String) = updateForm { copy(dose = value) }
    fun updateFrequency(value: String) = updateForm { copy(frequency = value) }
    fun updateTimeOfDay(value: String) = updateForm { copy(timeOfDay = value) }
    fun updatePrescribedBy(value: String) = updateForm { copy(prescribedBy = value) }
    fun updateInstructions(value: String) = updateForm { copy(instructions = value) }
    fun updateStartDate(value: String) = updateForm { copy(startDate = value) }
    fun updateEndDate(value: String) = updateForm { copy(endDate = value) }
    fun toggleActive() = updateForm { copy(active = !active) }

    fun edit(item: SupplementWithTodayStatus) {
        val supplement = item.supplement
        _uiState.update {
            it.copy(
                form = SupplementFormState(
                    editingSupplementId = supplement.id,
                    name = supplement.name,
                    dose = supplement.dose,
                    frequency = supplement.frequency,
                    timeOfDay = supplement.timeOfDay,
                    prescribedBy = supplement.prescribedBy,
                    instructions = supplement.instructions,
                    startDate = supplement.startDate.ifBlank { LocalDate.now().toString() },
                    endDate = supplement.endDate.orEmpty(),
                    active = supplement.active,
                ),
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(form = SupplementFormState(), successMessage = null, errorMessage = null) }
    }

    fun save(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to manage supplements.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val validation = SupplementValidation.validate(_uiState.value.form)
            val input = validation.getOrNull()
            if (input == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = validation.exceptionOrNull()?.message ?: "Check supplement fields and try again.",
                    )
                }
                return@launch
            }

            val saveResult = repository.saveSupplement(
                uid = uid,
                supplement = Supplement(
                    id = input.id.orEmpty(),
                    name = input.name,
                    dose = input.dose,
                    frequency = input.frequency,
                    timeOfDay = input.timeOfDay,
                    prescribedBy = input.prescribedBy,
                    instructions = input.instructions,
                    startDate = input.startDate.toString(),
                    endDate = input.endDate?.toString(),
                    active = input.active,
                ),
            )
            val savedSupplement = saveResult.getOrNull()
            if (savedSupplement == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = saveResult.exceptionOrNull()?.toUserFacingMessage()
                            ?: "Could not save supplement. Please try again.",
                    )
                }
                return@launch
            }

            val refreshed = repository.loadSupplementsForDate(uid).getOrNull()
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    form = SupplementFormState(),
                    supplements = refreshed ?: state.supplements.upsert(savedSupplement),
                    successMessage = if (input.id == null) "Supplement added." else "Supplement updated.",
                    errorMessage = null,
                )
            }
        }
    }

    fun deactivate(uid: String, supplementId: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to manage supplements.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val result = repository.deactivateSupplement(uid, supplementId)
            val refreshed = if (result.isSuccess) repository.loadSupplementsForDate(uid).getOrNull() else null
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    supplements = refreshed ?: state.supplements,
                    successMessage = if (result.isSuccess) "Supplement deactivated." else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun markTaken(uid: String, supplementId: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to manage supplements.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val result = repository.markSupplementTaken(uid, supplementId)
            val refreshed = if (result.isSuccess) repository.loadSupplementsForDate(uid).getOrNull() else null
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    supplements = refreshed ?: state.supplements,
                    successMessage = if (result.isSuccess) "Marked as taken for today." else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    private fun updateForm(reducer: SupplementFormState.() -> SupplementFormState) {
        _uiState.update {
            it.copy(
                form = it.form.reducer(),
                successMessage = null,
                errorMessage = null,
            )
        }
    }
}

private fun List<SupplementWithTodayStatus>.upsert(supplement: Supplement): List<SupplementWithTodayStatus> {
    val replacement = SupplementWithTodayStatus(supplement = supplement, todayLog = firstOrNull { it.supplement.id == supplement.id }?.todayLog)
    return if (any { it.supplement.id == supplement.id }) {
        map { if (it.supplement.id == supplement.id) replacement else it }
    } else {
        this + replacement
    }.sortedBy { it.supplement.name.lowercase() }
}

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
