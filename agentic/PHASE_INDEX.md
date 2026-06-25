# Phase Index

The coding agent must implement phases in this exact order.

| Order | Phase file | Commit message |
|---:|---|---|
| 0 | `agentic/phases/PHASE_00_PROJECT_FOUNDATION.md` | `Phase 0: Set up Android project foundation` |
| 1 | `agentic/phases/PHASE_01_AUTH_FIREBASE.md` | `Phase 1: Add Firebase Google authentication` |
| 2 | `agentic/phases/PHASE_02_ONBOARDING_WEIGHT.md` | `Phase 2: Add pregnancy onboarding and weight profile` |
| 3 | `agentic/phases/PHASE_03_HOME_DASHBOARD.md` | `Phase 3: Add pregnancy home dashboard` |
| 4 | `agentic/phases/PHASE_04_SYMPTOMS_SAFETY.md` | `Phase 4: Add symptom logging and safety flags` |
| 5 | `agentic/phases/PHASE_05_SUPPLEMENTS.md` | `Phase 5: Add supplement tracking` |
| 6 | `agentic/phases/PHASE_06_MEAL_LOGGING.md` | `Phase 6: Add meal logging` |
| 7 | `agentic/phases/PHASE_07_NUTRITION_ENGINE.md` | `Phase 7: Add pregnancy nutrition engine` |
| 8 | `agentic/phases/PHASE_08_AI_CONTRACT.md` | `Phase 8: Add AI integration contract` |
| 9 | `agentic/phases/PHASE_09_AI_SUMMARIES.md` | `Phase 9: Add AI summaries` |
| 10 | `agentic/phases/PHASE_10_REMINDERS.md` | `Phase 10: Add reminders and notifications` |
| 11 | `agentic/phases/PHASE_11_REPORTS_EXPORT.md` | `Phase 11: Add reports and export` |
| 12 | `agentic/phases/PHASE_12_SETTINGS_PRIVACY.md` | `Phase 12: Add settings privacy and account deletion` |
| 13 | `agentic/phases/PHASE_13_PRODUCTION_POLISH.md` | `Phase 13: Polish app for production readiness` |
| 14 | `agentic/phases/PHASE_14_FINAL_VERIFICATION.md` | `Final verification and build report` |

## Agent rule

The agent must read `agentic/PHASE_STATE.md`, find the first phase that is not marked `DONE`, and implement only that phase.
