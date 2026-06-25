# Master Plan: MVP to Final Product

## Product vision

Build a pregnancy companion Android app that helps pregnant mothers track pregnancy progress, symptoms, prescribed supplements, food intake, weight, and changing dietary needs across pregnancy weeks. The app should combine deterministic nutrition calculations with AI-generated educational summaries.

The end product should support safe, personalized, pregnancy-stage-aware nutrition guidance while clearly staying within wellness and educational boundaries.

## Guiding principles

1. Safety first.
2. Doctor guidance overrides app guidance.
3. AI explains and summarizes; deterministic systems calculate and validate.
4. Pregnancy nutrition changes by week, trimester, body weight, symptoms, conditions, and restrictions.
5. User data is sensitive and must be protected.
6. MVP should be usable without every advanced feature.
7. Every phase should leave the app in a working state.

## Recommended implementation phases

### Phase 0: Repository and architecture setup

Goal: Create the Android project foundation and coding conventions.

Deliverables:

- Android Kotlin project
- Jetpack Compose setup
- Navigation structure
- Firebase dependencies configured but not fully implemented
- Package structure
- Theme, typography, and base UI components
- App constants and environment configuration
- Basic CI lint/test workflow if desired

Exit criteria:

- App launches locally
- Home placeholder screen renders
- Navigation shell exists
- Project builds successfully

---

### Phase 1: Authentication and user profile foundation

Goal: Allow users to sign in and store basic profile information.

Deliverables:

- Firebase Authentication with Google Sign-In
- Auth state management
- User document creation in Firestore
- Sign out flow
- Basic settings/profile screen

Exit criteria:

- User can sign in with Google
- User record is created in Firestore
- User can sign out
- Firestore rules restrict user data

---

### Phase 2: Pregnancy onboarding and tracker MVP

Goal: Let the user create a pregnancy profile and see pregnancy progress.

Deliverables:

- Onboarding flow
- Required date inputs:
  - Date found out pregnant
  - Last menstrual period, optional but recommended
  - Estimated due date, optional
  - Doctor-confirmed pregnancy week, optional
- Weight inputs:
  - Current weight
  - Pre-pregnancy weight
  - Height
  - Weight unit
- Allergies, dietary restrictions, medical conditions
- Pregnancy week/day/trimester calculation
- Due date calculation or use of doctor-provided due date
- Pregnancy dashboard

Exit criteria:

- User can complete onboarding
- App calculates pregnancy week and trimester
- Dashboard shows current pregnancy state
- Weight history starts with onboarding weight

---

### Phase 3: Symptoms and red-flag safety MVP

Goal: Allow daily symptom logging and basic safety escalation.

Deliverables:

- Symptom selection list
- Severity, duration, notes
- Daily symptom log storage
- Red-flag detection engine
- Red-flag UI warning component
- Symptom history screen

Exit criteria:

- User can log symptoms
- Severe or risky symptoms trigger warning
- Non-urgent symptoms are saved normally
- Symptoms are displayed by date

---

### Phase 4: Supplements and pill reminders MVP

Goal: Track prescribed pills/supplements and daily intake.

Deliverables:

- Add supplement screen
- Supplement fields:
  - Name
  - Dose
  - Frequency
  - Time of day
  - Prescribed by
  - Instructions
  - Start date
  - Optional end date
- Mark supplement as taken
- Supplement log history
- Local notification reminders, if feasible in MVP

Exit criteria:

- User can add prescribed supplements
- User can mark supplements as taken
- Dashboard shows today’s supplement status

---

### Phase 5: Meal logging MVP

Goal: Allow food intake logging with quantities.

Deliverables:

- Add meal screen
- Meal type: breakfast, lunch, dinner, snack, drink
- Food item entry:
  - Food name
  - Quantity
  - Unit
  - Weight in grams
- Edit/delete meal
- Daily meal history
- Frequent meal support can be delayed

Exit criteria:

- User can log meals with food and weight
- Meals are stored in Firestore
- Dashboard shows today's meal count

---

### Phase 6: Deterministic nutrition engine MVP

Goal: Estimate nutrient intake from logged foods and compare with stage-aware targets.

Deliverables:

- Initial curated food nutrient database
- Nutrient calculation by grams
- Daily nutrient totals
- Nutrition targets based on:
  - Pregnancy week
  - Trimester
  - Current weight
  - Pre-pregnancy weight
  - Height/BMI category where available
  - Pregnancy type if known
- Daily nutrition summary
- Weekly nutrition summary

Exit criteria:

- App estimates daily nutrient intake
- App displays low/adequate/high status for tracked nutrients
- Targets change based on pregnancy week/trimester and weight inputs

---

### Phase 7: AI integration MVP

Goal: Generate safe educational summaries using Pollinations.ai through backend proxy.

Deliverables:

- Backend AI proxy function
- AI input DTO
- AI output DTO
- Safety prompt
- AI response validation
- Daily AI nutrition summary
- Symptom explanation summary
- Fallback message when AI unavailable

Exit criteria:

- App can request AI summary for the day
- AI receives structured context
- AI response is parsed as structured JSON
- Red-flag logic is not dependent on AI only

---

### Phase 8: Weekly intelligence and plan adjustment

Goal: Use weekly patterns to provide more meaningful advice.

Deliverables:

- Weekly nutrient trend analysis
- Weekly symptom trend analysis
- Repeated deficiency detection
- Weight trend view by pregnancy week
- AI weekly summary
- Food suggestions based on gaps and restrictions

Exit criteria:

- User can view weekly summary
- Repeated nutrient gaps are highlighted
- AI explains current week priorities

---

### Phase 9: User experience polish

Goal: Make the MVP feel like a real app.

Deliverables:

- Improved dashboard
- Empty states
- Loading states
- Error states
- Offline-friendly writes if feasible
- Better onboarding copy
- Gentle, non-shaming weight and nutrition language
- Accessibility improvements

Exit criteria:

- App is usable by non-technical users
- Screens have clear feedback and validation
- App handles network failures gracefully

---

### Phase 10: Export, privacy, and account controls

Goal: Add trust and data ownership features.

Deliverables:

- Export report for gynecologist
- Export data as PDF or CSV, optional
- Delete account
- Delete all user data
- Privacy policy screen
- Medical disclaimer screen
- Consent for AI processing

Exit criteria:

- User can remove their data
- User understands app limitations
- User can share a summary with a clinician

---

### Phase 11: Advanced features

Goal: Move from MVP to end product.

Possible features:

- Natural language meal entry
- Barcode scanning
- Image-based food logging
- Regional food databases
- Multi-language support
- Doctor appointment tracker
- Lab result tracking
- Gestational diabetes mode
- Blood pressure tracking
- Hydration tracking
- Fetal movement tracking for later pregnancy
- Partner/family read-only support
- Wearable integration
- Doctor report generation

Exit criteria:

- App supports broader pregnancy wellness needs
- Advanced features remain medically safe and clearly scoped

## End product definition

The end product should include:

- Google login
- Pregnancy profile and tracker
- Week-aware nutrition engine
- Weight-aware and symptom-aware nutrition guidance
- Meal logging
- Supplement tracking
- Symptom tracking
- AI daily and weekly summaries
- Red-flag safety warnings
- Data export
- Account deletion
- Privacy and medical safety controls
- Scalable architecture for future modules

