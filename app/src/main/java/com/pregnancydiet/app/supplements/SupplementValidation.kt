package com.pregnancydiet.app.supplements

import java.time.LocalDate
import java.time.LocalTime

object SupplementValidation {
    fun validate(form: SupplementFormState): Result<ValidatedSupplementInput> = runCatching {
        val name = form.name.trim()
        val dose = form.dose.trim()
        val frequency = form.frequency.trim()
        val prescribedBy = form.prescribedBy.trim()
        val instructions = form.instructions.trim()
        require(name.isNotBlank()) { "Supplement name is required." }
        require(dose.isNotBlank()) { "Dose is required. Enter the prescribed dose exactly as provided." }
        require(frequency.isNotBlank()) { "Frequency is required." }
        require(prescribedBy.isNotBlank()) { "Prescriber is required." }

        val timeOfDay = parseTime(form.timeOfDay)
        val startDate = parseRequiredDate(form.startDate, "Start date")
        val endDate = parseOptionalDate(form.endDate, "End date")
        if (endDate != null) {
            require(!endDate.isBefore(startDate)) { "End date cannot be before start date." }
        }

        ValidatedSupplementInput(
            id = form.editingSupplementId,
            name = name,
            dose = dose,
            frequency = frequency,
            timeOfDay = timeOfDay.toString(),
            prescribedBy = prescribedBy,
            instructions = instructions,
            startDate = startDate,
            endDate = endDate,
            active = form.active,
        )
    }

    private fun parseTime(value: String): LocalTime = runCatching { LocalTime.parse(value.trim()) }
        .getOrElse { error("Time of day must use HH:MM, for example 09:00.") }

    private fun parseRequiredDate(value: String, label: String): LocalDate {
        require(value.isNotBlank()) { "$label is required. Use YYYY-MM-DD." }
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { error("$label must use YYYY-MM-DD.") }
    }

    private fun parseOptionalDate(value: String, label: String): LocalDate? {
        if (value.isBlank()) return null
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { error("$label must use YYYY-MM-DD.") }
    }
}
