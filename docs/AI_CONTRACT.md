# AI Contract

## Purpose

The AI provides educational summaries for pregnancy symptoms, meals, supplements, weight context, and nutrition gaps. It must not diagnose, prescribe, or replace medical professionals.

## Architecture

Android app supports a frontend Pollinations provider for normal app AI use plus an optional backend provider for compatibility. Do not expose server-only credentials or unsafe prompt logic in the Android app.

Before building a new AI payload, the Android app checks the signed-in user's privacy setting at `users/{uid}/privacySettings/default.aiProcessingAllowed`. If it is false, new AI summary generation is blocked and no AI payload is created.

```text
Android App -> Pollinations.ai frontend endpoint -> Android validation -> Firestore/App
Android App -> Optional Backend/Firebase Function -> Pollinations.ai -> Backend Validation -> Firestore/App
```

## Pollinations access modes

The app uses the lowest-cost available Pollinations path per access mode:

- Free hourly mode uses the anonymous legacy `text.pollinations.ai/openai` endpoint with `openai-fast` because the current legacy model list does not expose `nova-micro` or `nova-fast`.
- User/account mode uses the new `gen.pollinations.ai/v1/chat/completions` endpoint with `nova-fast`, the available low-cost Nova text model. If Pollinations adds a documented `nova-micro` text model, the client default can be changed to that model id.
- If Pollinations returns `402 PAYMENT_REQUIRED` or a budget/pollen error, the app treats it as quota exceeded and shows a safe fallback instead of a parser failure.

### Free hourly AI

- Uses `POLLINATIONS_PUBLIC_KEY`, a bundled client-safe publishable key expected to start with `pk_`.
- Must never use a Pollinations `sk_` server secret key.
- Intended for the free Pollinations option, usually limited to about 1 pollen per IP per hour.
- The app tracks usage locally as an estimate only; Pollinations enforces the real allowance by IP/key.

### User Pollinations account

- User can connect a Pollinations account or client-safe user credential from Settings → AI Usage.
- The credential is stored in encrypted local storage on the device.
- Requests use the user credential instead of the bundled free hourly key.
- If the credential is missing, invalid, unauthorized, or quota-limited, the app returns structured setup/quota/rate-limit UI states.

### Secret-key rule

Frontend/client calls may only use a `pk_` publishable key or a Pollinations-supported user/client credential. If a credential starts with `sk_`, frontend code rejects it because it is treated as a server-only secret key unless Pollinations officially documents that exact type as client-safe.

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

## Privacy and usage history

AI usage history stores metadata only: feature, status, estimated pollen cost, retry time, and a short non-sensitive message. It must not store prompts, responses, symptoms, weights, pregnancy dates, medications, supplements, credentials, or free-text health data.

