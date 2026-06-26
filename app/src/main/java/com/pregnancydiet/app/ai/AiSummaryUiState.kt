package com.pregnancydiet.app.ai

import java.time.LocalDate

data class AiSummaryUiState(
    val selectedDate: String = LocalDate.now().toString(),
    val isLoading: Boolean = false,
    val activeAction: AiSummaryAction? = null,
    val latestSummary: AiSummaryRecord? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
) {
    val isBusy: Boolean = isLoading || activeAction != null
}

enum class AiSummaryAction(val label: String) {
    DailyInsight("daily insight"),
    SymptomGuidance("symptom guidance"),
    WeeklySummary("weekly summary"),
}
