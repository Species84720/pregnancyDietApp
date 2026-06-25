# Phase 2: Pregnancy Onboarding With Weight

## Goal
Collect pregnancy profile, pregnancy dating information, and weight data.

## Scope
1. Build onboarding screens for pregnancy profile.
2. Collect:
   - dateFoundOut
   - lastMenstrualPeriod optional
   - estimatedDueDate optional
   - doctorConfirmedWeek optional
   - pregnancyType: singleton, twins, multiple, unknown
   - heightCm optional but recommended
   - prePregnancyWeightKg optional but recommended
   - currentWeightKg required
   - weightUnit: kg or lb
   - allergies
   - dietaryRestrictions
   - medicalConditions
3. Validate required fields.
4. Store the profile at `users/{uid}/pregnancyProfiles/{profileId}`.
5. Mark the active pregnancy profile.
6. Create first weight log at `users/{uid}/weightLogs/{weightLogId}`.
7. Add pregnancy calculation logic:
   - current pregnancy week
   - day within week
   - trimester
   - estimated due date if possible
8. Prefer doctor-provided estimatedDueDate over calculated values.
9. Show clear message if dateFoundOut alone is not enough for accurate pregnancy dating.
10. Add onboarding completion routing to Home screen.
11. Add tests for pregnancy week / trimester calculation.

## Acceptance criteria
- User can complete onboarding with current weight.
- Profile and initial weight log are stored under the user.
- Pregnancy week/day/trimester calculations are tested.
- Doctor-provided due date takes priority.

## Required commit message
`Phase 2: Add pregnancy onboarding and weight profile`
