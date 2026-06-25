# Copilot / Coding Agent Instructions

You are building a pregnancy diet, symptom, supplement, and weight tracking Android application.

## Core stack

- Android: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM or clean architecture
- Auth: Firebase Authentication with Google provider
- Database: Cloud Firestore
- Backend AI proxy: Firebase Cloud Functions or equivalent backend
- AI provider: Pollinations.ai text model endpoint through backend only

## Product purpose

The app helps pregnant mothers:

- Track pregnancy week, day, trimester, and estimated due date
- Log symptoms daily
- Add gynecologist-prescribed pills and supplements
- Mark supplements as taken
- Log meals by food, quantity, unit, and weight
- Track user weight through pregnancy
- Estimate nutrient intake from foods
- Compare intake against week-aware and trimester-aware pregnancy nutrition targets
- Receive AI-generated educational summaries and food suggestions

## Medical safety rules

The app is a wellness and tracking assistant, not a medical device or diagnostic tool.

Never implement AI copy or app copy that:

- Diagnoses a condition
- Says a symptom is definitely safe
- Tells the user to ignore severe symptoms
- Recommends stopping, starting, or changing prescribed medication
- Gives exact supplement dosage changes without doctor confirmation
- Replaces a gynecologist, midwife, dietitian, or emergency service

Always escalate red-flag symptoms. Examples include:

- Vaginal bleeding
- Severe abdominal pain
- Severe headache
- Vision changes
- High fever
- Fainting
- Chest pain
- Severe vomiting or inability to keep fluids down
- Sudden swelling of face or hands
- Reduced fetal movement in later pregnancy
- Signs of allergic reaction

Use language such as:

- "This can happen in pregnancy, but contact your gynecologist if it is severe, persistent, or concerning."
- "This app provides educational guidance and does not replace medical advice."
- "Because you reported a red-flag symptom, seek medical advice urgently."

## Architecture rules

Use layered architecture:

- `ui`: Compose screens and components
- `viewmodel`: state holders and user actions
- `domain`: use cases and business logic
- `data`: repositories, Firebase data sources, nutrition data sources
- `model`: shared data classes
- `ai`: AI request/response DTOs and backend client
- `safety`: red-flag symptom detection and medical disclaimer logic

Do not call Pollinations.ai directly from Android if secrets, rate limits, or safety validation are needed. Route AI calls through a backend.

## Privacy rules

Pregnancy, symptoms, meals, supplement usage, weight, and medical conditions are sensitive health-related data.

Implement:

- Authenticated-only reads/writes
- Firestore rules using `request.auth.uid == userId`
- Delete account and delete data support in later phases
- Minimal AI payloads
- No unnecessary logging of sensitive data

## Development behavior

Build phase by phase. Do not skip ahead.

For each phase:

1. Implement data models first.
2. Implement repositories and fake/local test data where useful.
3. Implement UI screens.
4. Add validation.
5. Add tests for business logic.
6. Only then integrate Firebase or backend services.

Keep code modular and testable. Avoid hardcoding nutrition logic directly in UI components.


## Agentic phase workflow

When the user says "implement the next phase", do not ask which phase. Follow this workflow:

1. Read `agentic/IMPLEMENT_NEXT_PHASE.md`.
2. Read `agentic/PHASE_STATE.md`.
3. Find the first phase not marked `DONE`.
4. Read the matching phase file under `agentic/phases/`.
5. Implement only that phase.
6. Run available build/tests.
7. Update `agentic/PHASE_STATE.md`.
8. Commit with the required commit message from the phase file.
9. Push to the current remote branch.
10. Report the result and the next phase.

Do not skip phases. Do not implement future phases early unless required to keep the current phase compiling.
