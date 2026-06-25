# AI Contract

## Purpose

The AI provides educational summaries for pregnancy symptoms, meals, supplements, weight context, and nutrition gaps. It must not diagnose, prescribe, or replace medical professionals.

## Architecture

Android app should call backend. Backend calls Pollinations.ai. Do not expose AI credentials or unsafe prompt logic in the Android app.

```text
Android App -> Backend/Firebase Function -> Pollinations.ai -> Backend Validation -> Firestore/App
```

## System prompt

Use this as the baseline system instruction:

```text
You are a pregnancy wellness assistant. You provide educational, non-diagnostic guidance for pregnant users. You do not replace a gynecologist, midwife, dietitian, emergency service, or other medical professional.

You must follow these rules:

1. Never diagnose a condition.
2. Never prescribe medication or supplements.
3. Never tell the user to stop, start, or change prescribed pills or supplements.
4. Always recommend contacting a gynecologist or urgent medical service for red-flag symptoms.
5. Explain uncertainty clearly.
6. Prefer food-based nutrition suggestions where safe.
7. Respect allergies, dietary restrictions, medical conditions, and doctor notes.
8. Use gentle, non-shaming language about weight and diet.
9. Keep responses concise, practical, and reassuring without dismissing risks.
10. Return valid JSON only.
```

## AI input payload

```json
{
  "requestType": "daily_nutrition_summary",
  "date": "2026-06-25",
  "pregnancyWeek": 12,
  "trimester": 1,
  "estimatedDueDate": "2027-01-08",
  "pregnancyType": "singleton",

  "heightCm": 165,
  "prePregnancyWeightKg": 68,
  "currentWeightKg": 70,

  "dietaryRestrictions": ["vegetarian"],
  "allergies": ["peanuts"],
  "medicalConditions": [],
  "doctorNotes": "",

  "symptomsToday": [
    {
      "name": "nausea",
      "severity": 5,
      "duration": "2 hours",
      "notes": "Worse in morning"
    }
  ],

  "foodsToday": [
    {
      "foodName": "lentil soup",
      "weightGrams": 300,
      "nutrition": {
        "proteinGrams": 18,
        "ironMg": 5,
        "calciumMg": 80,
        "folateMcg": 180
      }
    }
  ],

  "supplementsToday": [
    {
      "name": "Folic Acid",
      "dose": "400 mcg",
      "taken": true
    }
  ],

  "nutritionTotals": {
    "proteinGrams": 62,
    "ironMg": 14,
    "calciumMg": 760,
    "folateMcg": 520,
    "vitaminDMcg": 7
  },

  "nutritionTargets": {
    "proteinGrams": 75,
    "ironMg": 27,
    "calciumMg": 1000,
    "folateMcg": 600,
    "vitaminDMcg": 15
  },

  "detectedGaps": ["iron", "calcium", "vitaminD"],
  "redFlagDetectedByApp": false,
  "redFlagReasons": []
}
```

## AI output schema

The AI must return JSON matching this structure:

```json
{
  "summary": "Short user-friendly summary.",
  "stageContext": "Why nutrition needs matter at this pregnancy week or trimester.",
  "nutritionGaps": [
    {
      "nutrient": "iron",
      "status": "low",
      "explanation": "Iron needs often increase during pregnancy.",
      "foodSuggestions": ["lentils", "beans", "lean meat", "fortified cereals"],
      "safetyNote": "Confirm supplement changes with your gynecologist."
    }
  ],
  "symptomGuidance": {
    "severity": "mild | moderate | urgent | none",
    "commonContext": "Educational explanation only.",
    "selfCare": ["Small frequent meals", "Hydration"],
    "contactDoctorIf": ["Symptoms become severe", "Vomiting prevents fluids"]
  },
  "weightContext": {
    "summary": "Gentle weight trend message if relevant.",
    "doctorDiscussionRecommended": false
  },
  "urgentWarning": false,
  "urgentReasons": [],
  "nextSteps": ["Log dinner", "Include an iron-rich food"],
  "disclaimer": "This is educational guidance and does not replace medical advice."
}
```

## Backend validation rules

Before showing AI response:

- Ensure JSON parses.
- Ensure disclaimer exists.
- Ensure urgent warning is true if app red-flag detector found a red flag.
- Remove any text that says the user is definitely safe.
- Remove any medication change advice.
- Remove any diagnosis language.
- Fallback to safe generic message if validation fails.

## Fallback message

```text
I could not generate a personalized AI summary right now. Your logs were still saved. For any severe, unusual, or worrying symptoms, contact your gynecologist or urgent medical services.
```

