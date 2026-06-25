# Acceptance Criteria

## MVP acceptance criteria

### Authentication

- Given a new user, when they sign in with Google, then a Firestore user document is created.
- Given a signed-in user, when they sign out, then protected screens are no longer accessible.

### Onboarding

- Given a signed-in user, when they enter pregnancy dating information and weight data, then a pregnancy profile is saved.
- Given current weight input, when onboarding completes, then a weight log is created.
- Given an estimated due date, then app uses it as the primary due date.
- Given LMP but no due date, then app estimates due date.

### Tracker

- Given a pregnancy profile, when the dashboard loads, then current week, day, trimester, and due date are displayed.

### Symptoms

- Given a user logs nausea severity 5, then symptom is saved without urgent warning.
- Given a user logs vaginal bleeding, then urgent warning is shown.
- Given a user logs severe headache with vision changes, then urgent warning is shown.

### Supplements

- Given a user adds folic acid, then it appears in supplement list.
- Given a user marks supplement as taken today, then dashboard shows taken status.

### Meals

- Given a user logs 120g banana for breakfast, then meal is saved and appears in daily meal history.
- Given a user edits meal weight, then nutrition summary recalculates.

### Nutrition

- Given meals are logged, then daily nutrients are totaled.
- Given pregnancy week changes, then target profile updates.
- Given current weight changes, then weight-aware targets update where applicable.
- Given nutrient intake is below target, then nutrition dashboard shows gap.

### AI

- Given daily logs exist, when user requests AI summary, then backend returns structured educational summary.
- Given red flag exists, AI output must include urgent warning.
- Given AI service fails, app shows fallback message and logs remain saved.

### Privacy

- Given user A is signed in, they cannot read or write user B's data.

