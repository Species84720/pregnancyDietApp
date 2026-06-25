package com.pregnancydiet.app.model

data class PregnancyProfile(
    val id: String,
    val dateFoundOut: String,
    val lastMenstrualPeriod: String?,
    val estimatedDueDate: String?,
    val doctorConfirmedWeek: Int?,
    val pregnancyType: PregnancyType,
    val heightCm: Double?,
    val prePregnancyWeightKg: Double?,
    val currentWeightKg: Double,
    val weightUnit: WeightUnit,
    val allergies: List<String>,
    val dietaryRestrictions: List<String>,
    val medicalConditions: List<String>,
    val currentStatus: String = "active",
)

enum class PregnancyType(val firestoreValue: String) {
    Singleton("singleton"),
    Twins("twins"),
    Multiple("multiple"),
    Unknown("unknown"),
}

enum class WeightUnit(val firestoreValue: String) {
    Kg("kg"),
    Lb("lb"),
}