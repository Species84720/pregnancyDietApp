package com.pregnancydiet.app.reports

import com.pregnancydiet.app.model.PregnancyProfile
import com.pregnancydiet.app.model.PregnancyType
import com.pregnancydiet.app.model.SymptomEntry
import com.pregnancydiet.app.model.SymptomLog
import com.pregnancydiet.app.model.WeightUnit
import com.pregnancydiet.app.pregnancy.PregnancyDatingMethod
import com.pregnancydiet.app.pregnancy.PregnancyProgress
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ReportExportFormatterTest {
    @Test
    fun `export report includes factual sections and disclaimer`() {
        val report = GynecologistReport(
            generatedAt = LocalDateTime.parse("2026-06-26T10:00:00"),
            dateRange = ReportDateRange(
                startDate = LocalDate.parse("2026-06-20"),
                endDate = LocalDate.parse("2026-06-26"),
            ),
            pregnancyProfile = PregnancyProfile(
                id = "profile-1",
                dateFoundOut = "2026-06-01",
                lastMenstrualPeriod = "2026-04-01",
                estimatedDueDate = "2027-01-06",
                doctorConfirmedWeek = null,
                pregnancyType = PregnancyType.Singleton,
                heightCm = 165.0,
                prePregnancyWeightKg = 68.0,
                currentWeightKg = 70.0,
                weightUnit = WeightUnit.Kg,
                allergies = listOf("peanuts"),
                dietaryRestrictions = listOf("vegetarian"),
                medicalConditions = emptyList(),
            ),
            pregnancyProgress = PregnancyProgress(
                pregnancyWeek = 12,
                dayWithinWeek = 2,
                trimester = 1,
                estimatedDueDate = LocalDate.parse("2027-01-06"),
                datingMethod = PregnancyDatingMethod.LastMenstrualPeriod,
                message = null,
            ),
            symptomLogs = listOf(
                SymptomLog(
                    id = "symptom-1",
                    date = "2026-06-25",
                    pregnancyProfileId = "profile-1",
                    pregnancyWeek = 12,
                    trimester = 1,
                    symptoms = listOf(SymptomEntry("nausea", 5, "2 hours", "Morning")),
                    urgentFlag = false,
                    urgentReasons = emptyList(),
                ),
            ),
            supplements = emptyList(),
            supplementLogs = emptyList(),
            mealLogs = emptyList(),
            nutritionSummaries = emptyList(),
            weightLogs = emptyList(),
            weeklyAiSummaries = emptyList(),
        )

        val text = ReportExportFormatter.format(report)

        assertTrue(text.contains("Pregnancy Diet Tracker report"))
        assertTrue(text.contains("Symptom history (1 logs)"))
        assertTrue(text.contains("nausea severity 5/10"))
        assertTrue(text.contains("This report contains factual user-entered tracking data"))
        assertFalse(text.contains("diagnosed", ignoreCase = true))
    }
}
