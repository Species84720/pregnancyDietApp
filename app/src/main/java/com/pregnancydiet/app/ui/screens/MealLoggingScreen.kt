package com.pregnancydiet.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pregnancydiet.app.common.AppConstants
import com.pregnancydiet.app.meals.MealFoodItemFormState
import com.pregnancydiet.app.meals.MealFormState
import com.pregnancydiet.app.meals.MealLoggingViewModel
import com.pregnancydiet.app.meals.MealStatusMapper
import com.pregnancydiet.app.model.FoodNutrition
import com.pregnancydiet.app.model.MealFoodItem
import com.pregnancydiet.app.model.MealLog
import com.pregnancydiet.app.model.MealType
import java.util.Locale

@Composable
fun MealLoggingScreen(
    uid: String?,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealLoggingViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uid) {
        viewModel.load(uid.orEmpty())
    }

    when {
        state.isLoading -> MealLoadingState(modifier)
        else -> MealContent(
            form = state.form,
            pregnancyWeek = state.pregnancyProgress?.pregnancyWeek,
            trimester = state.pregnancyProgress?.trimester,
            mealsForDate = state.mealsForDate,
            isSaving = state.isSaving,
            successMessage = state.successMessage,
            errorMessage = state.errorMessage,
            onDateChange = viewModel::updateDate,
            onMealTypeChange = viewModel::updateMealType,
            onFoodNameChange = viewModel::updateFoodName,
            onQuantityChange = viewModel::updateQuantity,
            onUnitChange = viewModel::updateUnit,
            onWeightGramsChange = viewModel::updateWeightGrams,
            onAddItem = viewModel::addCurrentItem,
            onRemoveDraftItem = viewModel::removeDraftItem,
            onSaveMeal = { viewModel.save(uid.orEmpty()) },
            onCancelEdit = viewModel::cancelEdit,
            onEditMeal = viewModel::editMeal,
            onDeleteMeal = { viewModel.delete(uid.orEmpty(), it) },
            onBackToHome = onBackToHome,
            modifier = modifier,
        )
    }
}

@Composable
private fun MealLoadingState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Loading meal logger...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun MealContent(
    form: MealFormState,
    pregnancyWeek: Int?,
    trimester: Int?,
    mealsForDate: List<MealLog>,
    isSaving: Boolean,
    successMessage: String?,
    errorMessage: String?,
    onDateChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onFoodNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onWeightGramsChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveDraftItem: (Int) -> Unit,
    onSaveMeal: () -> Unit,
    onCancelEdit: () -> Unit,
    onEditMeal: (MealLog) -> Unit,
    onDeleteMeal: (String) -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Meals",
                style = MaterialTheme.typography.headlineMedium,
            )
            OutlinedButton(onClick = onBackToHome) {
                Text("Home")
            }
        }

        MealContextCard(pregnancyWeek, trimester, mealsForDate)
        MealFormCard(
            form = form,
            isSaving = isSaving,
            onDateChange = onDateChange,
            onMealTypeChange = onMealTypeChange,
            onFoodNameChange = onFoodNameChange,
            onQuantityChange = onQuantityChange,
            onUnitChange = onUnitChange,
            onWeightGramsChange = onWeightGramsChange,
            onAddItem = onAddItem,
            onRemoveDraftItem = onRemoveDraftItem,
            onSaveMeal = onSaveMeal,
            onCancelEdit = onCancelEdit,
        )

        successMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        MealHistorySection(
            meals = mealsForDate,
            isSaving = isSaving,
            onEditMeal = onEditMeal,
            onDeleteMeal = onDeleteMeal,
        )
        Text(
            text = "Nutrition values are estimates based on your logged foods. AI-assisted estimates may be approximate and should not replace medical advice.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = AppConstants.MEDICAL_DISCLAIMER,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun MealContextCard(
    pregnancyWeek: Int?,
    trimester: Int?,
    meals: List<MealLog>,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = MealStatusMapper.todayStatus(meals),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Week: ${pregnancyWeek?.toString() ?: "not available"} · Trimester: ${trimester?.toString() ?: "not available"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MealFormCard(
    form: MealFormState,
    isSaving: Boolean,
    onDateChange: (String) -> Unit,
    onMealTypeChange: (MealType) -> Unit,
    onFoodNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onWeightGramsChange: (String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveDraftItem: (Int) -> Unit,
    onSaveMeal: () -> Unit,
    onCancelEdit: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (form.isEditing) "Edit meal" else "Add meal",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = form.date,
                onValueChange = onDateChange,
                label = { Text("Date") },
                supportingText = { Text("Use YYYY-MM-DD") },
                singleLine = true,
            )
            MealTypeButtons(selected = form.mealType, onSelect = onMealTypeChange)
            FoodItemInputCard(
                itemForm = form.currentItem,
                onFoodNameChange = onFoodNameChange,
                onQuantityChange = onQuantityChange,
                onUnitChange = onUnitChange,
                onWeightGramsChange = onWeightGramsChange,
                onAddItem = onAddItem,
            )
            DraftItemsSection(
                draftItems = form.draftItems,
                onRemoveDraftItem = onRemoveDraftItem,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = onSaveMeal,
                    enabled = !isSaving,
                ) {
                    Text(if (isSaving) "Saving..." else if (form.isEditing) "Update meal" else "Save meal")
                }
                if (form.isEditing) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onCancelEdit,
                        enabled = !isSaving,
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun MealTypeButtons(
    selected: MealType,
    onSelect: (MealType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Meal type",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        MealType.entries.chunked(3).forEach { rowTypes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowTypes.forEach { type ->
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(type) },
                    ) {
                        Text(if (selected == type) "✓ ${type.label}" else type.label)
                    }
                }
                repeat(3 - rowTypes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FoodItemInputCard(
    itemForm: MealFoodItemFormState,
    onFoodNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onWeightGramsChange: (String) -> Unit,
    onAddItem: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Food item",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = itemForm.foodName,
            onValueChange = onFoodNameChange,
            label = { Text("Food name") },
            placeholder = { Text("banana") },
            singleLine = true,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = itemForm.quantity,
                onValueChange = onQuantityChange,
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = itemForm.unit,
                onValueChange = onUnitChange,
                label = { Text("Unit") },
                placeholder = { Text("piece") },
                singleLine = true,
            )
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = itemForm.weightGrams,
            onValueChange = onWeightGramsChange,
            label = { Text("Weight in grams") },
            supportingText = { Text("Optional but recommended for nutrition estimates") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onAddItem,
        ) {
            Text("Add food item to meal")
        }
    }
}

@Composable
private fun DraftItemsSection(
    draftItems: List<MealFoodItem>,
    onRemoveDraftItem: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Meal draft items (${draftItems.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
        )
        if (draftItems.isEmpty()) {
            Text(
                text = "Add one or more food items before saving this meal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            draftItems.forEachIndexed { index, item ->
                FoodItemSummaryCard(
                    item = item,
                    trailingAction = {
                        OutlinedButton(onClick = { onRemoveDraftItem(index) }) {
                            Text("Remove")
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun MealHistorySection(
    meals: List<MealLog>,
    isSaving: Boolean,
    onEditMeal: (MealLog) -> Unit,
    onDeleteMeal: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Daily meal history",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        if (meals.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "No meals logged for this date.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            meals.forEach { meal ->
                MealHistoryCard(
                    meal = meal,
                    isSaving = isSaving,
                    onEditMeal = onEditMeal,
                    onDeleteMeal = onDeleteMeal,
                )
            }
        }
    }
}

@Composable
private fun MealHistoryCard(
    meal: MealLog,
    isSaving: Boolean,
    onEditMeal: (MealLog) -> Unit,
    onDeleteMeal: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "${meal.mealType.label} · ${meal.date}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            meal.items.forEach { item ->
                FoodItemSummaryCard(item = item)
            }
            NutritionSummary(meal.items.map { it.nutrition }.total())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onEditMeal(meal) },
                    enabled = !isSaving,
                ) {
                    Text("Edit")
                }
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { onDeleteMeal(meal.id) },
                    enabled = !isSaving,
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun FoodItemSummaryCard(
    item: MealFoodItem,
    trailingAction: @Composable (() -> Unit)? = null,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = item.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${item.quantity.formatShort()} ${item.unit}${item.weightGrams?.let { " · ${it.formatShort()} g" }.orEmpty()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Estimated: ${item.nutrition.calories.formatShort()} kcal, ${item.nutrition.proteinGrams.formatShort()} g protein",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            trailingAction?.invoke()
        }
    }
}

@Composable
private fun NutritionSummary(nutrition: FoodNutrition) {
    Text(
        text = "Meal estimate: ${nutrition.calories.formatShort()} kcal · ${nutrition.proteinGrams.formatShort()} g protein · ${nutrition.fiberGrams.formatShort()} g fiber · ${nutrition.ironMg.formatShort()} mg iron · ${nutrition.calciumMg.formatShort()} mg calcium",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

private fun List<FoodNutrition>.total(): FoodNutrition = fold(FoodNutrition()) { total, next ->
    FoodNutrition(
        calories = total.calories + next.calories,
        proteinGrams = total.proteinGrams + next.proteinGrams,
        fiberGrams = total.fiberGrams + next.fiberGrams,
        folateMcg = total.folateMcg + next.folateMcg,
        ironMg = total.ironMg + next.ironMg,
        calciumMg = total.calciumMg + next.calciumMg,
        vitaminDMcg = total.vitaminDMcg + next.vitaminDMcg,
        vitaminB12Mcg = total.vitaminB12Mcg + next.vitaminB12Mcg,
        iodineMcg = total.iodineMcg + next.iodineMcg,
        omega3Mg = total.omega3Mg + next.omega3Mg,
        cholineMg = total.cholineMg + next.cholineMg,
    )
}

private fun Double.formatShort(): String = String.format(Locale.US, "%.1f", this)
