# Pregnancy Diet & Symptom Tracker Android App

This repository plan describes how to build an Android application for pregnant mothers that tracks pregnancy progress, symptoms, supplements, meals, weight, and stage-aware nutrition needs.

The app uses:

- Android native app, recommended Kotlin + Jetpack Compose
- Google Sign-In via Firebase Authentication
- Firebase Firestore for user data
- Firebase Cloud Functions or backend proxy for AI calls
- Pollinations.ai for text AI responses
- A structured nutrition engine for deterministic nutrient calculations

The app must be built in phases from MVP to final product. Health and pregnancy safety are core requirements. The AI must provide educational support only and must not diagnose, prescribe, or override gynecologist guidance.

## Android project setup

### Prerequisites

- Android Studio with Android SDK 35 installed
- JDK 17
- No Firebase secrets are committed. Add environment-specific Firebase configuration only when implementing the Firebase phase.

### Build and test

From the repository root:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The app launches to a Jetpack Compose authentication shell. Signed-out users see Google sign-in, and signed-in users route to onboarding or home based on their Firestore profile state.

Onboarding collects pregnancy dating information, pregnancy type, current weight, optional height and pre-pregnancy weight, allergies, dietary restrictions, and medical conditions. Profiles are stored under `users/{uid}/pregnancyProfiles`, and the first onboarding weight is stored under `users/{uid}/weightLogs`.

After onboarding, the home dashboard loads the active pregnancy profile from Firestore and shows pregnancy week/day, trimester, due date countdown, current weight, quick actions, and placeholder status cards for symptoms, meals, supplements, and nutrition.

The symptom screen allows daily symptom logging with date, symptom name, severity, duration, and notes. Logs are stored under `users/{uid}/symptomLogs`, include current pregnancy week and trimester where available, and run local red-flag safety checks before any AI feature is used. Red-flag symptoms show urgent guidance to contact a gynecologist, midwife, or emergency services.

The supplement screen tracks prescribed pills, vitamins, and supplements with dose, frequency, time, prescriber, instructions, start date, optional end date, and active status. Users can edit or deactivate supplements and mark them as taken for the current date. Supplements are stored under `users/{uid}/supplements`, intake logs are stored under `users/{uid}/supplementLogs`, and the home dashboard shows today’s supplement taken status. Safety copy reminds users not to stop or change prescribed supplements without consulting their gynecologist.

The meal screen logs meals by date and type with one or more food items. Each item records food name, quantity, unit, optional weight in grams, and local nutrition estimate placeholders that match the future nutrition engine fields. Meal logs are stored under `users/{uid}/mealLogs`, support edit/delete, include pregnancy week and trimester where available, and the home dashboard shows today’s meal count.

The nutrition screen generates deterministic daily nutrition summaries from logged meals. Targets adjust by pregnancy week, trimester, current weight, optional pre-pregnancy weight and height, pregnancy type, dietary restrictions, and medical conditions. Daily summaries are stored under `users/{uid}/dailyNutritionSummaries/{date}`, include totals, targets, gaps, and stage priorities, and the screen shows daily gaps plus seven-day trend context using non-diagnostic, food-first language.

The AI integration contract is defined on the Android side without embedding provider secrets or calling Pollinations.ai directly. The app has structured request payloads for daily nutrition summaries, symptom explanations, and weekly summaries, plus response models, prompt guardrails, backend service/repository interfaces, response parsing, validation, and safe fallback behavior. Until a secure backend or Firebase Cloud Function is configured, the default AI service returns the local fallback message and preserves deterministic nutrition and red-flag safety guidance.

The AI insights screen connects pregnancy profile, symptoms, meals, supplements, and deterministic nutrition summaries into daily, symptom, and weekly AI request flows. Generated output is validated before display, unsafe medical or medication-change advice is suppressed into a fallback, urgent warnings are shown prominently, and summaries are saved into user-scoped Firestore documents such as `dailyNutritionSummaries/{date}.aiSummary`, `symptomLogs/{logId}.aiSummary`, and `weeklySummaries/{weekId}`. AI failures do not block meal, symptom, supplement, or nutrition logging.

### Firebase and Google Sign-In setup

For authentication builds, create a Firebase Android app for package `com.pregnancydiet.app`, enable Google as a Firebase Authentication provider, and place the downloaded `google-services.json` at `app/google-services.json`. This file is intentionally ignored by git.

Set the OAuth web client ID locally with either a Gradle property or `local.properties` entry:

```properties
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

Deploy the included Firestore rules before using real user data:

```bash
firebase deploy --only firestore:rules
```

Start with:

1. `agentic/MASTER_PLAN.md`
2. `agentic/PHASE_TASKS.md`
3. `.github/copilot-instructions.md`
4. `docs/REQUIREMENTS.md`
5. `docs/FIREBASE_SCHEMA.md`
6. `docs/AI_CONTRACT.md`

## Agentic build workflow

This repository is designed so a coding agent can build the app phase by phase.

Use this command with GitHub Copilot Agent / GPT-5.5:

```text
Implement the next uncompleted phase using agentic/IMPLEMENT_NEXT_PHASE.md. Commit and push when done.
```

The agent should then:

1. Read `agentic/PHASE_STATE.md`.
2. Find the next phase that is not `DONE`.
3. Read the matching file in `agentic/phases/`.
4. Implement only that phase.
5. Run build/tests.
6. Update `agentic/PHASE_STATE.md`.
7. Commit and push.

Phase files are stored in `agentic/phases/`.
