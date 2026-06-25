# Agentic Phase Tasks

Use this file as the coding agent backlog. Complete tasks in order. After each phase, run build and tests.

## Phase 0: Project setup

- [ ] Create Android Kotlin project.
- [ ] Enable Jetpack Compose.
- [ ] Create packages:
  - [ ] `ui`
  - [ ] `ui.navigation`
  - [ ] `ui.components`
  - [ ] `viewmodel`
  - [ ] `domain`
  - [ ] `domain.usecase`
  - [ ] `data`
  - [ ] `data.repository`
  - [ ] `data.firebase`
  - [ ] `model`
  - [ ] `ai`
  - [ ] `safety`
  - [ ] `nutrition`
- [ ] Add base app theme.
- [ ] Add navigation host.
- [ ] Add placeholder routes:
  - [ ] Login
  - [ ] Onboarding
  - [ ] Dashboard
  - [ ] Symptoms
  - [ ] Meals
  - [ ] Supplements
  - [ ] Nutrition
  - [ ] Settings
- [ ] Confirm project builds.

## Phase 1: Authentication

- [ ] Add Firebase project configuration.
- [ ] Add Firebase Auth dependency.
- [ ] Implement Google Sign-In.
- [ ] Create `AuthRepository`.
- [ ] Create `AuthViewModel`.
- [ ] Implement login screen.
- [ ] Implement sign-out in settings.
- [ ] On login, create or update Firestore user document.
- [ ] Add Firestore rules draft.
- [ ] Add tests for auth state handling where possible.

## Phase 2: Pregnancy onboarding and tracker

- [ ] Create models:
  - [ ] `PregnancyProfile`
  - [ ] `WeightLog`
  - [ ] `UserHealthProfile`
- [ ] Create onboarding screens:
  - [ ] Pregnancy dates
  - [ ] Weight and height
  - [ ] Allergies and restrictions
  - [ ] Conditions and notes
- [ ] Implement validation:
  - [ ] Current weight required
  - [ ] Weight unit required
  - [ ] At least one dating source recommended: LMP, estimated due date, or doctor-confirmed week
- [ ] Implement `PregnancyCalculator`.
- [ ] Implement trimester calculation.
- [ ] Save pregnancy profile to Firestore.
- [ ] Save initial weight log.
- [ ] Build dashboard showing:
  - [ ] Week
  - [ ] Day
  - [ ] Trimester
  - [ ] Estimated due date
  - [ ] Current weight
- [ ] Add unit tests for pregnancy calculations.

## Phase 3: Symptoms and safety

- [ ] Create `SymptomLog` model.
- [ ] Create symptom catalog.
- [ ] Build add symptom screen.
- [ ] Add fields:
  - [ ] Symptom name
  - [ ] Severity 1-10
  - [ ] Duration
  - [ ] Notes
- [ ] Implement `RedFlagDetector`.
- [ ] Add urgent warning UI.
- [ ] Save symptom logs.
- [ ] Show symptom history by date.
- [ ] Add tests for red-flag detection.

## Phase 4: Supplements

- [ ] Create `Supplement` model.
- [ ] Create `SupplementLog` model.
- [ ] Build supplement list screen.
- [ ] Build add/edit supplement screen.
- [ ] Add fields:
  - [ ] Name
  - [ ] Dose
  - [ ] Frequency
  - [ ] Time of day
  - [ ] Prescribed by
  - [ ] Instructions
  - [ ] Start date
  - [ ] End date
  - [ ] Active flag
- [ ] Add mark-as-taken flow.
- [ ] Add today’s supplement checklist to dashboard.
- [ ] Optional: local reminders.

## Phase 5: Meal logging

- [ ] Create `MealLog` model.
- [ ] Create `FoodItem` model.
- [ ] Build add meal screen.
- [ ] Support meal types:
  - [ ] Breakfast
  - [ ] Lunch
  - [ ] Dinner
  - [ ] Snack
  - [ ] Drink
- [ ] Add food entry fields:
  - [ ] Food name
  - [ ] Quantity
  - [ ] Unit
  - [ ] Weight grams
- [ ] Save meal logs.
- [ ] Show daily meals.
- [ ] Add edit/delete meal.

## Phase 6: Nutrition engine

- [ ] Create `NutrientProfile` model.
- [ ] Create `FoodNutritionEntry` model.
- [ ] Add initial curated food database.
- [ ] Implement nutrient calculation per grams.
- [ ] Create `NutritionTargetProfile` model.
- [ ] Implement target calculation based on:
  - [ ] Pregnancy week
  - [ ] Trimester
  - [ ] Current weight
  - [ ] Pre-pregnancy weight
  - [ ] Height/BMI if available
  - [ ] Pregnancy type
- [ ] Generate daily nutrition summary.
- [ ] Generate weekly nutrition summary.
- [ ] Build nutrition dashboard.
- [ ] Add tests for nutrient totals and target logic.

## Phase 7: AI integration

- [ ] Create backend proxy function.
- [ ] Create AI request DTO.
- [ ] Create AI response DTO.
- [ ] Implement Pollinations.ai backend call.
- [ ] Add AI safety system prompt.
- [ ] Validate JSON response.
- [ ] Store AI summaries in Firestore.
- [ ] Add daily AI summary screen/card.
- [ ] Add fallback if AI call fails.
- [ ] Ensure red-flag warning works without AI.

## Phase 8: Weekly intelligence

- [ ] Implement repeated nutrient gap detection.
- [ ] Implement weekly symptom trend detection.
- [ ] Implement weight trend by pregnancy week.
- [ ] Build weekly report screen.
- [ ] Add AI weekly summary.
- [ ] Add food suggestions based on nutrient gaps.

## Phase 9: UX polish

- [ ] Add loading states.
- [ ] Add empty states.
- [ ] Add error messages.
- [ ] Add form validation messages.
- [ ] Improve dashboard layout.
- [ ] Add accessibility labels.
- [ ] Add non-shaming copy for weight and nutrition.
- [ ] Improve offline behavior where possible.

## Phase 10: Privacy, export, and account controls

- [ ] Add privacy policy screen.
- [ ] Add medical disclaimer screen.
- [ ] Add AI consent screen.
- [ ] Add data export summary.
- [ ] Add delete account flow.
- [ ] Add delete all user data backend function.
- [ ] Add gynecologist report export.

## Phase 11: Advanced features

- [ ] Natural language meal entry.
- [ ] Barcode scanning.
- [ ] Food image recognition.
- [ ] Region-specific food database.
- [ ] Multi-language support.
- [ ] Doctor appointment tracker.
- [ ] Hydration tracking.
- [ ] Blood pressure tracking.
- [ ] Fetal movement tracking.
- [ ] Gestational diabetes mode.

