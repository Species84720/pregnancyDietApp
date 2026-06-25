# Phase 4: Symptom Logging and Red-Flag Safety Rules

## Goal
Allow daily symptom logging and detect urgent red-flag symptoms locally before AI.

## Scope
1. Build symptom logging screen.
2. Allow user to enter:
   - date
   - symptom name
   - severity 1-10
   - duration
   - notes
3. Provide common symptom options:
   - nausea
   - vomiting
   - fatigue
   - headache
   - back pain
   - cramps
   - bleeding
   - dizziness
   - heartburn
   - constipation
   - swelling
   - mood changes
   - food aversions
   - cravings
   - reduced fetal movement
4. Store logs at `users/{uid}/symptomLogs/{logId}`.
5. Include pregnancyWeek and trimester in each symptom log.
6. Implement local red-flag rules before AI:
   - bleeding
   - severe abdominal pain
   - severe headache
   - vision changes
   - high fever
   - fainting
   - chest pain
   - severe vomiting / unable to keep fluids
   - reduced fetal movement later in pregnancy
   - sudden swelling of face or hands
7. If red flag is detected, show urgent message:
   "This may require medical attention. Contact your gynecologist, midwife, or emergency services."
8. Do not diagnose.
9. Add symptom history list.
10. Add tests for red-flag detection.

## Acceptance criteria
- Symptoms save to Firestore under the user.
- Red flags trigger local urgent warning without needing AI.
- Tests cover major red-flag cases.

## Required commit message
`Phase 4: Add symptom logging and safety flags`
