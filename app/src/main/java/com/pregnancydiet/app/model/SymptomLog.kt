package com.pregnancydiet.app.model

data class SymptomLog(
    val id: String,
    val date: String,
    val pregnancyProfileId: String?,
    val pregnancyWeek: Int?,
    val trimester: Int?,
    val symptoms: List<SymptomEntry>,
    val urgentFlag: Boolean,
    val urgentReasons: List<String>,
) {
    val primarySymptom: SymptomEntry? = symptoms.firstOrNull()
}

data class SymptomEntry(
    val name: String,
    val severity: Int,
    val duration: String,
    val notes: String,
)
