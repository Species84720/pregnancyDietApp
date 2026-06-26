package com.pregnancydiet.app.settings

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.WeightUnit
import java.util.Locale

data class SettingsFormState(
    val pregnancyProfileId: String = "",
    val dateFoundOut: String = "",
    val lastMenstrualPeriod: String = "",
    val estimatedDueDate: String = "",
    val doctorConfirmedWeek: String = "",
    val pregnancyType: PregnancyType = PregnancyType.Unknown,
    val heightCm: String = "",
    val prePregnancyWeight: String = "",
    val currentWeight: String = "",
    val weightUnit: WeightUnit = WeightUnit.Kg,
    val allergies: String = "",
    val dietaryRestrictions: String = "",
    val medicalConditions: String = "",
)

fun PregnancyProfile.toSettingsFormState(): SettingsFormState = SettingsFormState(
    pregnancyProfileId = id,
    dateFoundOut = dateFoundOut,
    lastMenstrualPeriod = lastMenstrualPeriod.orEmpty(),
    estimatedDueDate = estimatedDueDate.orEmpty(),
    doctorConfirmedWeek = doctorConfirmedWeek?.toString().orEmpty(),
    pregnancyType = pregnancyType,
    heightCm = heightCm?.toDisplayNumber().orEmpty(),
    prePregnancyWeight = prePregnancyWeightKg?.fromKilograms(weightUnit)?.toDisplayNumber().orEmpty(),
    currentWeight = currentWeightKg.fromKilograms(weightUnit).toDisplayNumber(),
    weightUnit = weightUnit,
    allergies = allergies.joinToString(", "),
    dietaryRestrictions = dietaryRestrictions.joinToString(", "),
    medicalConditions = medicalConditions.joinToString(", "),
)

private fun Double.fromKilograms(unit: WeightUnit): Double = when (unit) {
    WeightUnit.Kg -> this
    WeightUnit.Lb -> this / 0.45359237
}

private fun Double.toDisplayNumber(): String = String.format(Locale.US, "%.2f", this)
    .trimEnd('0')
    .trimEnd('.')