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

- Android Studio with Android SDK 35 installed; the app targets SDK 34 and supports Android 6.0/API 23+
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

The AI integration supports direct frontend Pollinations access through a provider abstraction without embedding server-only secrets. The default access mode is “Free hourly AI,” which uses a bundled client-safe Pollinations publishable key and local estimated cooldown tracking for the free option, usually about 1 pollen per IP per hour. Users can also connect a Pollinations account or client-safe user key from Settings → AI Usage so AI requests use their own allowance. The app rejects `sk_` server secret keys in frontend code, never stores prompts/responses in usage history, and keeps backend AI access isolated as an optional compatibility provider.

The AI insights screen connects pregnancy profile, symptoms, meals, supplements, and nutrition summaries into daily, symptom, and weekly AI request flows. Generated output is validated before display, unsafe medical or medication-change advice is suppressed into a fallback, urgent warnings are shown prominently, and summaries are saved into user-scoped Firestore documents such as `dailyNutritionSummaries/{date}.aiSummary`, `symptomLogs/{logId}.aiSummary`, `weeklySummaries/{weekId}`, and deterministic log documents in `aiSummaries/{summaryId}`. AI nutrition estimates are stored with `aiNutritionProcessed`, `nutritionProcessedBy`, and `nutritionProcessingStatus` markers, and AI-processed daily or weekly summaries are reused for the same day or week instead of calling AI again. AI failures do not block meal, symptom, supplement, or nutrition logging.

The reminders screen lets users enable or disable optional supplement reminders, one daily meal logging reminder, and one daily symptom check-in reminder. Reminder preferences are stored under `users/{uid}/reminderPreferences/default`, Android notification permission is requested before reminders are enabled, and scheduled notification copy is gentle, non-alarming, and never medical advice.

The reports screen creates a factual date-range report for the user or gynecologist. It summarizes symptom history, supplement adherence, meal history, nutrition gaps, weight logs, and saved weekly AI summaries from only the signed-in user’s Firestore data. Reports can be shared as clean text and include dates, pregnancy weeks where available, and a clear non-diagnostic disclaimer.

The settings screen shows profile details, lets users edit active pregnancy profile fields including allergies, dietary restrictions, and medical conditions, links to reminders and report export, and includes privacy and medical disclaimer screens. Privacy controls are stored under `users/{uid}/privacySettings/default`; disabling AI summaries blocks new AI generation requests. Account deletion uses confirmation, deletes known user-owned Firestore subcollections under `users/{uid}`, then attempts Firebase Authentication account deletion, which may require a recent sign-in.

Production polish improves route navigation so Home and Settings links do not create duplicate back-stack entries, tightens form validation against non-finite or unrealistic meal, weight, and height values, and adds a privacy-safe no-op analytics placeholder that allows only coarse screen/event names without payloads or health details.

### Firebase and Google Sign-In setup

For authentication builds, create a Firebase Android app for package `com.pregnancydiet.app`, enable Google as a Firebase Authentication provider, and place the downloaded `google-services.json` at `app/google-services.json`. This file is intentionally ignored by git.

Set the OAuth web client ID locally with either a Gradle property or `local.properties` entry:

```properties
GOOGLE_WEB_CLIENT_ID=your-web-client-id.apps.googleusercontent.com
```

Set the Pollinations publishable key locally with either a Gradle property or `local.properties` entry. This must be a client-safe publishable key that starts with `pk_`; never use a server-only `sk_` secret key in Android code, resources, Gradle files, logs, crash reports, public URLs, or git.

```properties
POLLINATIONS_PUBLIC_KEY=pk_your-client-safe-key
```

Deploy the included Firestore rules before using real user data:

```bash
firebase deploy --only firestore:rules
```

### MVP release notes

- Build before release with `./gradlew :app:assembleDebug` and run unit tests with `./gradlew :app:testDebugUnitTest`.
- The latest GitHub release is `v0.1.5` and includes a signed APK asset for installation testing.
- If Android reports “App not installed” while updating from `v0.1.0` through `v0.1.4`, uninstall the older sideloaded debug build first. Earlier GitHub APKs were debug-signed by different build machines, and Android blocks same-package updates signed with a different certificate.
- Configure Firebase Auth, Firestore, and local `GOOGLE_WEB_CLIENT_ID` before testing real sign-in.
- Deploy Firestore rules so all reads and writes remain scoped to `request.auth.uid == userId`.
- Do not add Pollinations.ai or other AI provider `sk_` secrets to Android app code. Frontend mode may use only a bundled `pk_` publishable key or a Pollinations-supported client/user credential.
- Users can change AI access from Settings → AI Usage, connect/disconnect a Pollinations account key, and see local usage estimates. Free hourly usage is estimated locally; Pollinations enforces the real limit by IP/key.
- Review privacy and medical disclaimer copy before distribution; the app is educational wellness support and does not replace professional medical advice.
- See `docs/FINAL_BUILD_REPORT.md` for final verification status, implemented MVP coverage, and deferred production prerequisites.

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
