package com.pregnancydiet.app.meals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pregnancydiet.app.data.MealLogRepository
import com.pregnancydiet.app.firebase.FirestoreMealLogRepository
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.MealType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class MealLoggingViewModel(
    private val repository: MealLogRepository = FirestoreMealLogRepository(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(MealLoggingUiState())
    val uiState: StateFlow<MealLoggingUiState> = _uiState.asStateFlow()

    fun load(uid: String) {
        if (uid.isBlank()) {
            _uiState.value = MealLoggingUiState(
                isLoading = false,
                errorMessage = "Sign in again to log meals.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val contextResult = repository.loadLoggingContext(uid)
            val date = _uiState.value.form.date.toLocalDateOrToday()
            val mealsResult = repository.loadMealLogsForDate(uid, date)
            val context = contextResult.getOrNull()
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    pregnancyProfileId = context?.pregnancyProfileId,
                    pregnancyProgress = context?.progress,
                    mealsForDate = mealsResult.getOrNull().orEmpty(),
                    errorMessage = contextResult.exceptionOrNull()?.toUserFacingMessage()
                        ?: mealsResult.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    fun updateDate(value: String) = updateForm { copy(date = value) }
    fun updateMealType(value: MealType) = updateForm { copy(mealType = value) }
    fun updateFoodName(value: String) = updateItemForm { copy(foodName = value) }
    fun updateQuantity(value: String) = updateItemForm { copy(quantity = value) }
    fun updateUnit(value: String) = updateItemForm { copy(unit = value) }
    fun updateWeightGrams(value: String) = updateItemForm { copy(weightGrams = value) }

    fun addCurrentItem() {
        val validation = MealValidation.validateFoodItem(_uiState.value.form.currentItem)
        val item = validation.getOrNull()
        if (item == null) {
            _uiState.update {
                it.copy(errorMessage = validation.exceptionOrNull()?.message ?: "Check food item fields and try again.")
            }
            return
        }

        _uiState.update { state ->
            state.copy(
                form = state.form.copy(
                    currentItem = MealFoodItemFormState(),
                    draftItems = state.form.draftItems + item,
                ),
                successMessage = "Food item added to meal draft.",
                errorMessage = null,
            )
        }
    }

    fun removeDraftItem(index: Int) {
        _uiState.update { state ->
            state.copy(
                form = state.form.copy(draftItems = state.form.draftItems.filterIndexed { itemIndex, _ -> itemIndex != index }),
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun editMeal(meal: MealLog) {
        _uiState.update {
            it.copy(
                form = MealFormState(
                    editingMealId = meal.id,
                    date = meal.date.ifBlank { LocalDate.now().toString() },
                    mealType = meal.mealType,
                    currentItem = MealFoodItemFormState(),
                    draftItems = meal.items,
                ),
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(form = MealFormState(), successMessage = null, errorMessage = null) }
    }

    fun save(uid: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to log meals.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null) }
            val current = _uiState.value
            val validation = MealValidation.validateMeal(current.form)
            val input = validation.getOrNull()
            if (input == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = validation.exceptionOrNull()?.message ?: "Check meal fields and try again.",
                    )
                }
                return@launch
            }

            val meal = MealLog(
                id = input.id.orEmpty(),
                date = input.date.toString(),
                pregnancyProfileId = current.pregnancyProfileId,
                pregnancyWeek = current.pregnancyProgress?.pregnancyWeek,
                trimester = current.pregnancyProgress?.trimester,
                mealType = input.mealType,
                items = input.items,
            )
            val saveResult = repository.saveMealLog(uid, meal)
            val savedMeal = saveResult.getOrNull()
            if (savedMeal == null) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = saveResult.exceptionOrNull()?.toUserFacingMessage()
                            ?: "Could not save meal. Please try again.",
                    )
                }
                return@launch
            }

            val refreshed = repository.loadMealLogsForDate(uid, input.date).getOrNull()
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    form = MealFormState(date = input.date.toString()),
                    mealsForDate = refreshed ?: state.mealsForDate.upsert(savedMeal),
                    successMessage = if (input.id == null) "Meal saved." else "Meal updated.",
                    errorMessage = null,
                )
            }
        }
    }

    fun delete(uid: String, mealId: String) {
        if (uid.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Sign in again to log meals.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, successMessage = null, errorMessage = null) }
            val result = repository.deleteMealLog(uid, mealId)
            val date = _uiState.value.form.date.toLocalDateOrToday()
            val refreshed = if (result.isSuccess) repository.loadMealLogsForDate(uid, date).getOrNull() else null
            _uiState.update { state ->
                state.copy(
                    isSaving = false,
                    mealsForDate = refreshed ?: state.mealsForDate.filterNot { it.id == mealId },
                    successMessage = if (result.isSuccess) "Meal deleted." else null,
                    errorMessage = result.exceptionOrNull()?.toUserFacingMessage(),
                )
            }
        }
    }

    private fun updateForm(reducer: MealFormState.() -> MealFormState) {
        _uiState.update {
            it.copy(
                form = it.form.reducer(),
                successMessage = null,
                errorMessage = null,
            )
        }
    }

    private fun updateItemForm(reducer: MealFoodItemFormState.() -> MealFoodItemFormState) {
        updateForm { copy(currentItem = currentItem.reducer()) }
    }
}

private fun List<MealLog>.upsert(meal: MealLog): List<MealLog> = if (any { it.id == meal.id }) {
    map { if (it.id == meal.id) meal else it }
} else {
    this + meal
}.sortedWith(compareBy<MealLog> { it.mealType.ordinal }.thenBy { it.id })

private fun String.toLocalDateOrToday(): LocalDate = runCatching { LocalDate.parse(this) }.getOrDefault(LocalDate.now())

private fun Throwable.toUserFacingMessage(): String = message
    ?.takeIf { it.isNotBlank() }
    ?: "Something went wrong. Please try again."
