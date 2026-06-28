# Firebase Firestore Schema

All user data should be stored under the authenticated user path:

```text
users/{uid}
```

## users/{uid}

```json
{
  "uid": "firebase_uid",
  "email": "user@example.com",
  "displayName": "Maria",
  "photoUrl": "https://...",
  "createdAt": "timestamp",
  "lastLoginAt": "timestamp",
  "onboardingCompleted": true,
  "activePregnancyProfileId": "profile_123"
}
```

## users/{uid}/pregnancyProfiles/{profileId}

```json
{
  "dateFoundOut": "2026-06-25",
  "lastMenstrualPeriod": "2026-05-01",
  "estimatedDueDate": "2027-02-05",
  "doctorConfirmedWeek": 8,
  "doctorConfirmedWeekDate": "2026-06-25",
  "pregnancyType": "singleton",
  "currentStatus": "active",

  "heightCm": 165,
  "prePregnancyWeightKg": 68,
  "currentWeightKg": 70,
  "weightUnit": "kg",

  "allergies": ["peanuts"],
  "dietaryRestrictions": ["vegetarian"],
  "medicalConditions": ["gestational_diabetes"],
  "doctorNotes": "Avoid X if doctor instructed.",

  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## users/{uid}/weightLogs/{weightLogId}

```json
{
  "date": "2026-06-25",
  "pregnancyProfileId": "profile_123",
  "pregnancyWeek": 8,
  "weightKg": 70,
  "source": "manual",
  "notes": "Morning weight",
  "createdAt": "timestamp"
}
```

## users/{uid}/symptomLogs/{logId}

```json
{
  "date": "2026-06-25",
  "pregnancyProfileId": "profile_123",
  "pregnancyWeek": 8,
  "symptoms": [
    {
      "name": "nausea",
      "severity": 6,
      "duration": "3 hours",
      "notes": "Worse after breakfast"
    }
  ],
  "urgentFlag": false,
  "urgentReasons": [],
  "aiSummary": {
    "type": "symptom_explanation",
    "summary": "Educational symptom context...",
    "urgentWarning": false,
    "disclaimer": "This is educational guidance and does not replace medical advice."
  },
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## users/{uid}/supplements/{supplementId}

```json
{
  "name": "Folic Acid",
  "dose": "400 mcg",
  "frequency": "daily",
  "timeOfDay": "09:00",
  "prescribedBy": "Gynecologist",
  "instructions": "Take after breakfast",
  "startDate": "2026-06-25",
  "endDate": null,
  "active": true,
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## users/{uid}/supplementLogs/{logId}

```json
{
  "supplementId": "supplement_123",
  "date": "2026-06-25",
  "taken": true,
  "takenAt": "2026-06-25T09:15:00",
  "notes": "",
  "createdAt": "timestamp"
}
```

## users/{uid}/reminderPreferences/default

```json
{
  "supplementRemindersEnabled": true,
  "mealRemindersEnabled": false,
  "symptomCheckInEnabled": true,
  "mealReminderTime": "12:30",
  "symptomReminderTime": "20:00",
  "updatedAtIso": "2026-06-26T10:15:00",
  "updatedAt": "timestamp"
}
```

## users/{uid}/privacySettings/default

```json
{
  "aiProcessingAllowed": true,
  "updatedAtIso": "2026-06-26T10:15:00Z",
  "updatedAt": "timestamp"
}
```

When `aiProcessingAllowed` is false, the Android app blocks new AI summary generation before building a backend AI payload. Existing logs remain available for deterministic app features and local reports.

AI access mode, Pollinations user credentials, and local AI usage estimates are stored locally on the device. Pollinations user credentials are not stored in Firestore. Usage history stores metadata only and does not include prompts, responses, symptoms, weights, pregnancy dates, medications, supplements, or credentials.

## users/{uid}/mealLogs/{mealId}

```json
{
  "date": "2026-06-25",
  "pregnancyProfileId": "profile_123",
  "pregnancyWeek": 8,
  "mealType": "breakfast",
  "items": [
    {
      "foodName": "banana",
      "quantity": 1,
      "unit": "piece",
      "weightGrams": 120,
      "nutrition": {
        "calories": 105,
        "proteinGrams": 1.3,
        "fiberGrams": 3.1,
        "folateMcg": 24,
        "ironMg": 0.3,
        "calciumMg": 6,
        "vitaminDMcg": 0,
        "vitaminB12Mcg": 0,
        "iodineMcg": 0,
        "omega3Mg": 0,
        "cholineMg": 0
      }
    }
  ],
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## users/{uid}/dailyNutritionSummaries/{date}

Document ID should be ISO date, for example `2026-06-25`.

```json
{
  "date": "2026-06-25",
  "pregnancyProfileId": "profile_123",
  "pregnancyWeek": 8,
  "trimester": 1,
  "currentWeightKg": 70,
  "nutritionProfileVersion": "pregnancy_targets_v1",
  "totals": {
    "calories": 1900,
    "proteinGrams": 62,
    "fiberGrams": 24,
    "folateMcg": 520,
    "ironMg": 14,
    "calciumMg": 760,
    "vitaminDMcg": 7,
    "vitaminB12Mcg": 2.1,
    "iodineMcg": 120,
    "omega3Mg": 180,
    "cholineMg": 260
  },
  "targets": {
    "proteinGrams": 75,
    "fiberGrams": 28,
    "folateMcg": 600,
    "ironMg": 27,
    "calciumMg": 1000,
    "vitaminDMcg": 15,
    "vitaminB12Mcg": 2.6,
    "iodineMcg": 220,
    "omega3Mg": 200,
    "cholineMg": 450
  },
  "gaps": [
    {
      "nutrient": "iron",
      "status": "low",
      "severity": "moderate"
    }
  ],
  "stagePriorities": ["folate", "iodine", "protein", "hydration"],
  "aiSummaryId": "daily_nutrition_summary_2026-06-25",
  "aiSummary": {
    "id": "daily_nutrition_summary_2026-06-25",
    "type": "daily_nutrition_summary",
    "summary": "Your intake today appears lower in iron and calcium...",
    "nutritionEstimates": {
      "proteinGrams": {
        "value": 72.5,
        "confidence": "medium",
        "explanation": "Estimated from logged foods.",
        "source": "ai"
      }
    },
    "nutritionEstimateSource": "ai_assisted",
    "nutritionProcessed": true,
    "nutritionProcessedBy": "ai",
    "nutritionProcessingStatus": "ai_processed",
    "urgentWarning": false,
    "processingStatus": "ai_processed",
    "processedBy": "ai",
    "disclaimer": "This is educational guidance and does not replace medical advice."
  },
  "aiNutritionEstimates": {
    "proteinGrams": {
      "value": 72.5,
      "confidence": "medium",
      "explanation": "Estimated from logged foods.",
      "source": "ai"
    }
  },
  "aiNutritionTotals": {
    "proteinGrams": 72.5
  },
  "aiNutritionProcessed": true,
  "nutritionProcessedBy": "ai",
  "nutritionProcessingStatus": "ai_processed",
  "nutritionProcessedAt": "timestamp",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

When `dailyNutritionSummaries/{date}.aiSummary` exists with `nutritionProcessingStatus: "ai_processed"`, the app reuses it for repeat daily AI insight requests for that date instead of calling the AI provider again. Fallback summaries are logged but are not treated as AI-processed nutrition.

Local daily nutrition regeneration uses merge writes so the saved AI fields remain attached to the date document. When `aiNutritionProcessed` is true and `aiNutritionTotals` is present, app nutrition summary, weekly summary, and report reads prefer `aiNutritionTotals` over deterministic local `totals`.

## users/{uid}/weeklySummaries/{weekId}

Phase 9 stores the displayed weekly AI summary and safe fallback state here.

```json
{
  "weekId": "2026-W26",
  "pregnancyProfileId": "profile_123",
  "pregnancyWeek": 8,
  "trimester": 1,
  "daysIncluded": 7,
  "repeatedGaps": ["iron", "calcium"],
  "aiSummaryId": "weekly_summary_2026-W26",
  "aiSummary": {
    "id": "weekly_summary_2026-W26",
    "type": "weekly_summary",
    "summary": "Educational weekly summary...",
    "nutritionEstimates": {
      "proteinGrams": {
        "value": 66,
        "confidence": "low",
        "explanation": "Estimated from saved weekly averages.",
        "source": "ai"
      }
    },
    "nutritionEstimateSource": "ai_assisted",
    "nutritionProcessed": true,
    "nutritionProcessedBy": "ai",
    "nutritionProcessingStatus": "ai_processed",
    "urgentWarning": false,
    "fallback": false,
    "processingStatus": "ai_processed",
    "processedBy": "ai",
    "disclaimer": "This is educational guidance and does not replace medical advice."
  },
  "aiNutritionEstimates": {
    "proteinGrams": {
      "value": 66,
      "confidence": "low",
      "explanation": "Estimated from saved weekly averages.",
      "source": "ai"
    }
  },
  "aiNutritionProcessed": true,
  "nutritionProcessedBy": "ai",
  "nutritionProcessingStatus": "ai_processed",
  "nutritionProcessedAt": "timestamp",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

When `weeklySummaries/{weekId}.aiSummary` exists with `nutritionProcessingStatus: "ai_processed"`, the app reuses it for repeat weekly AI summary requests for that ISO week instead of calling the AI provider again.

Weekly summaries are built from the effective daily nutrition totals. If daily records in the week have already saved AI nutrition totals, those saved AI totals are reused for weekly averaging instead of reprocessing the same meal nutrition values.

## users/{uid}/weeklyNutritionSummaries/{weekId}

```json
{
  "weekId": "2026-W26",
  "pregnancyProfileId": "profile_123",
  "pregnancyWeek": 8,
  "daysIncluded": 7,
  "averageTotals": {
    "proteinGrams": 66,
    "ironMg": 15,
    "calciumMg": 820
  },
  "repeatedGaps": ["iron", "calcium"],
  "symptomPatterns": ["nausea after breakfast"],
  "weightTrend": {
    "startWeightKg": 69.5,
    "endWeightKg": 70.0,
    "changeKg": 0.5
  },
  "aiSummaryId": "ai_789",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

## users/{uid}/aiSummaries/{summaryId}

```json
{
  "type": "daily_nutrition_summary",
  "analysisDate": "2026-06-25",
  "analysisWeekId": null,
  "date": "2026-06-25",
  "pregnancyProfileId": "profile_123",
  "inputContextVersion": "ai_context_v1",
  "summary": "Your intake today appears lower in iron and calcium...",
  "nutritionEstimates": {
    "proteinGrams": {
      "value": 72.5,
      "confidence": "medium",
      "explanation": "Estimated from logged foods.",
      "source": "ai"
    }
  },
  "nutritionEstimateSource": "ai_assisted",
  "nutritionProcessed": true,
  "nutritionProcessedBy": "ai",
  "nutritionProcessingStatus": "ai_processed",
  "nutritionGaps": [
    {
      "nutrient": "iron",
      "status": "low",
      "foodSuggestions": ["lentils", "beans", "lean meat", "fortified cereals"]
    }
  ],
  "symptomGuidance": null,
  "urgentWarning": false,
  "processingStatus": "ai_processed",
  "processedBy": "ai",
  "disclaimer": "This is educational guidance and does not replace medical advice.",
  "loggedAt": "timestamp",
  "updatedAt": "timestamp"
}
```

AI summary log document IDs are deterministic for repeatable analysis and idempotent writes: `daily_nutrition_summary_{date}`, `weekly_summary_{weekId}`, and `symptom_explanation_{symptomLogId}`.

## Reports and export reads

The reports feature does not create a separate Firestore report document in Phase 11. It reads the signed-in user's existing scoped data from:

- `pregnancyProfiles`
- `weightLogs`
- `symptomLogs`
- `supplements`
- `supplementLogs`
- `mealLogs`
- `dailyNutritionSummaries`
- `weeklySummaries`

The export is generated locally as factual shareable text for the selected date range.

## Settings and account deletion writes

The settings feature reads `users/{uid}` and the signed-in user's active pregnancy profile. It updates only the active pregnancy profile under `users/{uid}/pregnancyProfiles/{profileId}` and writes privacy controls to `users/{uid}/privacySettings/default`.

Account deletion deletes known user-owned subcollections under `users/{uid}` before deleting the user document and then attempts Firebase Authentication account deletion. Known subcollections include:

- `pregnancyProfiles`
- `weightLogs`
- `symptomLogs`
- `supplements`
- `supplementLogs`
- `mealLogs`
- `dailyNutritionSummaries`
- `weeklySummaries`
- `weeklyNutritionSummaries`
- `aiSummaries`
- `reminderPreferences`
- `privacySettings`

Firebase Authentication may require recent sign-in before the account itself can be deleted. If auth deletion requires reauthentication, user-scoped Firestore data has already been removed and the user should sign in again before retrying account deletion.

## Firestore security rule concept

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /{document=**} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```
