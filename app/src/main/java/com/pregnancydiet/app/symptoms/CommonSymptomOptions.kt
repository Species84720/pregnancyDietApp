package com.pregnancydiet.app.symptoms

object CommonSymptomOptions {
    val requiredOptions = listOf(
        "nausea",
        "vomiting",
        "fatigue",
        "headache",
        "back pain",
        "cramps",
        "bleeding",
        "dizziness",
        "heartburn",
        "constipation",
        "swelling",
        "mood changes",
        "food aversions",
        "cravings",
        "reduced fetal movement",
    )

    val safetyOptions = listOf(
        "severe abdominal pain",
        "vision changes",
        "high fever",
        "fainting",
        "chest pain",
        "unable to keep fluids down",
        "sudden swelling of face or hands",
    )

    val allOptions = requiredOptions + safetyOptions
}
