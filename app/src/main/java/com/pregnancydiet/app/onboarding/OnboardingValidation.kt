package com.pregnancydiet.app.onboarding

import com.pregnancydiet.app.model.WeightUnit
import java.time.LocalDate

object OnboardingValidation {
    fun validate(form: OnboardingFormState): Result<ValidatedOnboardingInput> = runCatching {
        val dateFoundOut = parseRequiredDate(form.dateFoundOut, "Date found out")
        val lmp = parseOptionalDate(form.lastMenstrualPeriod, "Last menstrual period")
        val dueDate = parseOptionalDate(form.estimatedDueDate, "Estimated due date")
        val doctorWeek = parseOptionalInt(form.doctorConfirmedWeek, "Doctor-confirmed week")?.also {
            require(it in 1..42) { "Doctor-confirmed week must be between 1 and 42." }
        }
        val currentWeight = parseRequiredDouble(form.currentWeight, "Current weight")
        require(currentWeight > 0.0) { "Current weight must be greater than zero." }

        val heightCm = parseOptionalDouble(form.heightCm, "Height")?.also {
            require(it > 0.0) { "Height must be greater than zero." }
        }
        val prePregnancyWeight = parseOptionalDouble(form.prePregnancyWeight, "Pre-pregnancy weight")?.also {
            require(it > 0.0) { "Pre-pregnancy weight must be greater than zero." }
        }

        ValidatedOnboardingInput(
            dateFoundOut = dateFoundOut,
            lastMenstrualPeriod = lmp,
            estimatedDueDate = dueDate,
            doctorConfirmedWeek = doctorWeek,
            heightCm = heightCm,
            prePregnancyWeightKg = prePregnancyWeight?.toKilograms(form.weightUnit),
            currentWeightKg = currentWeight.toKilograms(form.weightUnit),
            allergies = form.allergies.toListValues(),
            dietaryRestrictions = form.dietaryRestrictions.toListValues(),
            medicalConditions = form.medicalConditions.toListValues(),
        )
    }

    private fun parseRequiredDate(value: String, label: String): LocalDate =
        parseOptionalDate(value, label) ?: error("$label is required. Use YYYY-MM-DD.")

    private fun parseOptionalDate(value: String, label: String): LocalDate? {
        if (value.isBlank()) return null
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { error("$label must use YYYY-MM-DD.") }
    }

    private fun parseRequiredDouble(value: String, label: String): Double =
        parseOptionalDouble(value, label) ?: error("$label is required.")

    private fun parseOptionalDouble(value: String, label: String): Double? {
        if (value.isBlank()) return null
        return value.trim().toDoubleOrNull() ?: error("$label must be a valid number.")
    }

    private fun parseOptionalInt(value: String, label: String): Int? {
        if (value.isBlank()) return null
        return value.trim().toIntOrNull() ?: error("$label must be a whole number.")
    }

    private fun Double.toKilograms(unit: WeightUnit): Double = when (unit) {
        WeightUnit.Kg -> this
        WeightUnit.Lb -> this * 0.45359237
    }

    private fun String.toListValues(): List<String> = split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

data class ValidatedOnboardingInput(
    val dateFoundOut: LocalDate,
    val lastMenstrualPeriod: LocalDate?,
    val estimatedDueDate: LocalDate?,
    val doctorConfirmedWeek: Int?,
    val heightCm: Double?,
    val prePregnancyWeightKg: Double?,
    val currentWeightKg: Double,
    val allergies: List<String>,
    val dietaryRestrictions: List<String>,
    val medicalConditions: List<String>,
)