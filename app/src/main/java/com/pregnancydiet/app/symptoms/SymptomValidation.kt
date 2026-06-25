package com.pregnancydiet.app.symptoms

import com.pregnancydiet.app.model.SymptomEntry
import java.time.LocalDate

object SymptomValidation {
    fun validate(form: SymptomFormState): Result<ValidatedSymptomInput> = runCatching {
        val date = parseDate(form.date)
        val name = form.symptomName.trim()
        require(name.isNotBlank()) { "Symptom name is required." }
        val severity = form.severity.trim().toIntOrNull()
            ?: error("Severity must be a whole number from 1 to 10.")
        require(severity in 1..10) { "Severity must be between 1 and 10." }

        ValidatedSymptomInput(
            date = date,
            symptom = SymptomEntry(
                name = name,
                severity = severity,
                duration = form.duration.trim(),
                notes = form.notes.trim(),
            ),
        )
    }

    private fun parseDate(value: String): LocalDate {
        require(value.isNotBlank()) { "Date is required. Use YYYY-MM-DD." }
        return runCatching { LocalDate.parse(value.trim()) }
            .getOrElse { error("Date must use YYYY-MM-DD.") }
    }
}
