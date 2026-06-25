# Phase 3: Home Dashboard

## Goal
Create the main dashboard after onboarding.

## Scope
1. Build Home screen after onboarding.
2. Display:
   - current pregnancy week and day
   - trimester
   - estimated due date
   - countdown to due date
   - current weight
   - quick actions: Add Meal, Add Symptom, Add Supplement, View Nutrition
3. Load active pregnancy profile from Firestore.
4. Use a ViewModel and repository pattern.
5. Add loading, empty, and error states.
6. Add placeholder cards for:
   - today’s symptoms
   - today’s meals
   - today’s supplements
   - nutrition status
7. Keep UI simple and accessible.

## Out of scope
- Do not implement actual logging yet unless needed for navigation stubs.

## Acceptance criteria
- Home shows pregnancy and weight details from Firestore.
- Quick actions navigate to current or placeholder destinations.
- Loading/error/empty states exist.

## Required commit message
`Phase 3: Add pregnancy home dashboard`
