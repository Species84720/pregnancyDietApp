package com.pregnancydiet.app.symptoms

import com.pregnancydiet.app.model.SymptomEntry
import java.time.LocalDate

data class SymptomFormState(
    val date: String = LocalDate.now().toString(),
    val symptomName: String = CommonSymptomOptions.requiredOptions.first(),
    val severity: String = "5",
    val duration: String = "",
    val notes: String = "",
)

data class ValidatedSymptomInput(
    val date: LocalDate,
    val symptom: SymptomEntry,
)
