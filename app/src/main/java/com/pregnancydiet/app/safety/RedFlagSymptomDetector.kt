package com.pregnancydiet.app.safety

import java.util.Locale

data class SymptomSafetyInput(
    val name: String,
    val severity: Int,
    val duration: String = "",
    val notes: String = "",
    val pregnancyWeek: Int? = null,
)

data class SymptomSafetyResult(
    val urgentFlag: Boolean,
    val urgentReasons: List<String>,
) {
    companion object {
        val Safe = SymptomSafetyResult(urgentFlag = false, urgentReasons = emptyList())
    }
}

object RedFlagSymptomDetector {
    private const val SEVERE_THRESHOLD = 8

    fun evaluate(input: SymptomSafetyInput): SymptomSafetyResult {
        val text = listOf(input.name, input.duration, input.notes)
            .joinToString(separator = " ")
            .normalize()
        val reasons = buildList {
            if (text.containsAny("vaginal bleeding", "bleeding", "blood")) {
                add("Bleeding during pregnancy can require urgent medical advice.")
            }
            if (text.containsAny("abdominal pain", "stomach pain", "belly pain") && input.severity >= SEVERE_THRESHOLD) {
                add("Severe abdominal pain should be checked urgently.")
            }
            if (text.contains("cramp") && input.severity >= SEVERE_THRESHOLD) {
                add("Severe cramps or abdominal pain should be checked urgently.")
            }
            if (text.contains("headache") && input.severity >= SEVERE_THRESHOLD) {
                add("A severe headache can be a pregnancy warning sign.")
            }
            if (text.containsAny("vision", "blurred", "seeing spots", "spots in vision")) {
                add("Vision changes during pregnancy can require urgent medical advice.")
            }
            if (text.containsAny("high fever", "fever") && input.severity >= SEVERE_THRESHOLD) {
                add("A high fever in pregnancy should be checked promptly.")
            }
            if (text.containsAny("fainting", "fainted", "passed out", "pass out")) {
                add("Fainting during pregnancy should be discussed urgently with a clinician.")
            }
            if (text.contains("chest pain")) {
                add("Chest pain can require urgent medical attention.")
            }
            if (text.contains("vomit") && input.severity >= SEVERE_THRESHOLD) {
                add("Severe vomiting can cause dehydration and may need urgent care.")
            }
            if (text.containsAny("cannot keep fluids", "can't keep fluids", "unable to keep fluids", "cannot keep water", "can't keep water", "unable to keep water")) {
                add("Being unable to keep fluids down can cause dehydration and may need urgent care.")
            }
            if (text.contains("reduced fetal movement") && input.pregnancyWeek.isLaterPregnancyOrUnknown()) {
                add("Reduced fetal movement in later pregnancy should be checked urgently.")
            }
            if (text.contains("swelling") && (text.containsAny("face", "hand", "hands", "sudden") || input.severity >= SEVERE_THRESHOLD)) {
                add("Sudden swelling of the face or hands can be a pregnancy warning sign.")
            }
            if (text.containsAny("allergic reaction", "trouble breathing", "difficulty breathing", "swollen lips", "swollen tongue")) {
                add("Signs of an allergic reaction can require emergency care.")
            }
        }.distinct()

        return if (reasons.isEmpty()) {
            SymptomSafetyResult.Safe
        } else {
            SymptomSafetyResult(urgentFlag = true, urgentReasons = reasons)
        }
    }
}

private fun String.normalize(): String = lowercase(Locale.US).trim()

private fun String.containsAny(vararg values: String): Boolean = values.any(::contains)

private fun Int?.isLaterPregnancyOrUnknown(): Boolean = this == null || this >= 20
