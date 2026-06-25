# Phase 5: Supplements and Pill Tracking

## Goal
Allow users to track prescribed pills, vitamins, and supplements.

## Scope
1. Build supplement management screen.
2. Allow user to add:
   - name
   - dose
   - frequency
   - timeOfDay
   - prescribedBy
   - instructions
   - startDate
   - endDate optional
   - active true/false
3. Store supplements at `users/{uid}/supplements/{supplementId}`.
4. Allow user to mark supplement as taken for a date.
5. Store supplement logs at `users/{uid}/supplementLogs/{logId}`.
6. Show today’s supplements on Home dashboard.
7. Add edit and deactivate supplement support.
8. Do not allow the app or AI to recommend changing prescribed medication.
9. Add user-facing safety copy:
   "Do not stop or change prescribed supplements without consulting your gynecologist."
10. Add local notification placeholder or architecture for reminders, but do not overbuild if not ready.

## Acceptance criteria
- User can create, edit, deactivate, and mark supplements as taken.
- Logs are date-based and user-scoped.
- Safety copy is visible.

## Required commit message
`Phase 5: Add supplement tracking`
