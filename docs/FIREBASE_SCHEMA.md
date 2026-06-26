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
  "aiSummary": {
    "type": "daily_nutrition_summary",
    "summary": "Your intake today appears lower in iron and calcium...",
    "urgentWarning": false,
    "disclaimer": "This is educational guidance and does not replace medical advice."
  },
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

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
  "aiSummary": {
    "type": "weekly_summary",
    "summary": "Educational weekly summary...",
    "urgentWarning": false,
    "fallback": false,
    "disclaimer": "This is educational guidance and does not replace medical advice."
  },
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

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
  "type": "daily_nutrition",
  "date": "2026-06-25",
  "pregnancyProfileId": "profile_123",
  "inputContextVersion": "ai_context_v1",
  "summary": "Your intake today appears lower in iron and calcium...",
  "nutritionGaps": [
    {
      "nutrient": "iron",
      "status": "low",
      "foodSuggestions": ["lentils", "beans", "lean meat", "fortified cereals"]
    }
  ],
  "symptomGuidance": null,
  "urgentWarning": false,
  "disclaimer": "This is educational guidance and does not replace medical advice.",
  "createdAt": "timestamp"
}
```

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

