# Software Requirements Specification

## 1. Scope

The application is an Android pregnancy wellness tracker focused on pregnancy progress, symptoms, supplements, meals, weight, and nutrition. It uses AI to produce educational summaries and food-based suggestions.

The application is not a diagnostic or medical decision-making system.

## 2. User roles

### Pregnant user

Primary user. Can create profile, log meals, symptoms, supplements, weight, and view AI summaries.

### Future role: clinician/export recipient

Not a login role in MVP. User may export summaries to share with gynecologist.

## 3. Functional requirements

### Authentication

| ID | Requirement |
|---|---|
| AUTH-01 | User can sign in with Google. |
| AUTH-02 | User can sign out. |
| AUTH-03 | User profile is linked to Firebase Authentication UID. |
| AUTH-04 | User data is scoped to authenticated UID. |

### Pregnancy profile

| ID | Requirement |
|---|---|
| PREG-01 | User can enter date they found out they are pregnant. |
| PREG-02 | User can enter last menstrual period date. |
| PREG-03 | User can enter gynecologist-provided estimated due date. |
| PREG-04 | User can enter doctor-confirmed pregnancy week. |
| PREG-05 | App calculates pregnancy week, day, trimester, and estimated due date. |
| PREG-06 | User can edit pregnancy profile. |
| PREG-07 | User can enter singleton/twins/multiple pregnancy if known. |
| PREG-08 | User can enter current weight. |
| PREG-09 | User can enter pre-pregnancy weight. |
| PREG-10 | User can enter height. |
| PREG-11 | User can update weight during pregnancy. |
| PREG-12 | App stores weight history logs. |
| PREG-13 | User can enter allergies, dietary restrictions, and medical conditions. |

### Pregnancy tracker

| ID | Requirement |
|---|---|
| TRACK-01 | App shows current pregnancy week and day. |
| TRACK-02 | App shows trimester. |
| TRACK-03 | App shows estimated due date. |
| TRACK-04 | App shows countdown to due date. |
| TRACK-05 | App shows today’s meal, supplement, symptom, and nutrition status. |

### Symptoms

| ID | Requirement |
|---|---|
| SYM-01 | User can log symptoms by date. |
| SYM-02 | User can select symptom type. |
| SYM-03 | User can enter severity from 1 to 10. |
| SYM-04 | User can enter duration and notes. |
| SYM-05 | App detects red-flag symptoms. |
| SYM-06 | App shows urgent warning for red-flag symptoms. |
| SYM-07 | AI can generate educational symptom explanation. |
| SYM-08 | User can view symptom history. |

### Supplements

| ID | Requirement |
|---|---|
| SUP-01 | User can add prescribed supplements or pills. |
| SUP-02 | User can enter dose, frequency, time, start date, end date, instructions, and prescriber. |
| SUP-03 | User can mark supplement as taken. |
| SUP-04 | App stores supplement intake history. |
| SUP-05 | App may remind user to take supplement. |
| SUP-06 | AI must not recommend stopping or changing prescribed medication. |

### Meal logging

| ID | Requirement |
|---|---|
| FOOD-01 | User can log meals by date and meal type. |
| FOOD-02 | User can enter food name. |
| FOOD-03 | User can enter quantity and unit. |
| FOOD-04 | User can enter weight in grams. |
| FOOD-05 | User can edit or delete meal entries. |
| FOOD-06 | App estimates nutrients from food entries. |

### Nutrition

| ID | Requirement |
|---|---|
| NUT-01 | App generates daily nutrition targets. |
| NUT-02 | Nutrition targets depend on pregnancy week and trimester. |
| NUT-03 | Nutrition targets use current weight where relevant. |
| NUT-04 | App uses pre-pregnancy weight and height where available. |
| NUT-05 | App compares daily food intake against targets. |
| NUT-06 | App compares weekly food intake against targets. |
| NUT-07 | App identifies nutrient gaps. |
| NUT-08 | App suggests food-based ways to improve gaps. |
| NUT-09 | App considers supplements already logged. |
| NUT-10 | App respects allergies, restrictions, and medical conditions. |
| NUT-11 | App avoids excessive supplement recommendations. |
| NUT-12 | App explains why nutrients matter at current pregnancy stage. |

### AI

| ID | Requirement |
|---|---|
| AI-01 | App can call Pollinations.ai through a frontend provider using only client-safe credentials, with an optional backend proxy retained as compatibility fallback. |
| AI-02 | AI receives structured context only. |
| AI-03 | AI returns structured JSON. |
| AI-04 | AI response is validated before display. |
| AI-05 | AI output includes medical disclaimer. |
| AI-06 | AI does not diagnose or prescribe. |
| AI-07 | AI defers to gynecologist for medical decisions. |
| AI-08 | AI fallback message appears if service unavailable. |
| AI-09 | User can choose free hourly AI or connect a Pollinations account/client-safe key. |
| AI-10 | App rejects server-only `sk_` credentials in frontend code. |
| AI-11 | AI usage history stores metadata only and excludes prompts, responses, credentials, and health details. |

## 4. Non-functional requirements

| Category | Requirement |
|---|---|
| Security | All user data must be scoped to authenticated UID. |
| Privacy | Health-related data must not be logged unnecessarily. |
| Reliability | Logging should work even if AI fails. |
| Performance | Dashboard should load quickly from cached data where possible. |
| Accessibility | UI should support large text and readable contrast. |
| Safety | Red-flag detection must not depend only on AI. |
| Maintainability | Nutrition and safety logic must be outside UI layer. |

## 5. MVP requirements

MVP includes:

- Google login
- Pregnancy onboarding
- Weight input and weight logs
- Pregnancy tracker
- Symptom logs with red-flag warnings
- Supplement logs
- Meal logs
- Basic nutrition calculations
- Week-aware nutrition targets
- AI daily summary

## 6. Out of scope for MVP

- Doctor portal
- Diagnosis
- Medication prescribing
- Lab result interpretation
- Image-based meal recognition
- Barcode scanning
- Wearables
- Community features

