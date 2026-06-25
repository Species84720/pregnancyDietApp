# Phase 13: Production Readiness and Polish

## Goal
Review the app end-to-end and prepare it for a production-quality MVP.

## Scope
1. Review the full app against all docs:
   - `docs/REQUIREMENTS.md`
   - `agentic/ACCEPTANCE_CRITERIA.md`
   - `docs/SAFETY_RULES.md`
2. Fix navigation bugs.
3. Improve loading, empty, and error states.
4. Improve accessibility:
   - readable text sizes
   - content descriptions
   - contrast
   - simple language
5. Improve form validation.
6. Improve offline behavior where practical.
7. Add crash-safe handling for missing Firestore fields.
8. Add analytics placeholders only if privacy-safe.
9. Run full build and tests.
10. Update README with final setup, Firebase setup, and release notes.

## Acceptance criteria
- App is stable across main user journeys.
- Forms have validation.
- Missing/empty Firestore fields do not crash the app.
- README is updated.

## Required commit message
`Phase 13: Polish app for production readiness`
