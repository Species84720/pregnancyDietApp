package com.pregnancydiet.app.supplements

import java.time.LocalDate

data class SupplementFormState(
    val editingSupplementId: String? = null,
    val name: String = "",
    val dose: String = "",
    val frequency: String = "daily",
    val timeOfDay: String = "09:00",
    val prescribedBy: String = "Gynecologist",
    val instructions: String = "",
    val startDate: String = LocalDate.now().toString(),
    val endDate: String = "",
    val active: Boolean = true,
) {
    val isEditing: Boolean = editingSupplementId != null
}

data class ValidatedSupplementInput(
    val id: String?,
    val name: String,
    val dose: String,
    val frequency: String,
    val timeOfDay: String,
    val prescribedBy: String,
    val instructions: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val active: Boolean,
)
