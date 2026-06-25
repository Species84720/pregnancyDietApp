# Phase 9: AI-Generated Summaries

## Goal
Generate safe AI summaries from user logs and nutrition data.

## Scope
1. Connect symptom logs, meal logs, supplement logs, pregnancy profile, and nutrition summary into AI request payloads.
2. Add "Generate Daily Insight" action.
3. Add "Generate Symptom Guidance" action.
4. Add "Generate Weekly Summary" action.
5. Store AI output in Firestore:
   - `dailyNutritionSummaries/{date}.aiSummary`
   - `symptomLogs/{logId}.aiSummary`
   - `weeklySummaries/{weekId}`
6. Validate AI response before displaying.
7. If urgentWarning is true, show prominent medical attention warning.
8. If AI output contains unsafe medication advice, suppress it and show safe fallback.
9. Add user-facing disclaimer:
   "This is educational guidance and does not replace medical advice."
10. Add loading and error states.

## Acceptance criteria
- Daily, symptom, and weekly AI summary flows exist.
- Unsafe AI medication advice is not displayed.
- Urgent warnings are prominent.
- AI failures do not block logging.

## Required commit message
`Phase 9: Add AI summaries`
