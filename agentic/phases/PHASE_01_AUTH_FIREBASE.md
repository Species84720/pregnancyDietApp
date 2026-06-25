# Phase 1: Authentication and Firebase User Setup

## Goal
Add Google login using Firebase Authentication and create a user document in Firestore.

## Scope
1. Add Google Sign-In using Firebase Authentication.
2. Create login screen with "Continue with Google".
3. Add sign-out support.
4. After successful login, create or update the user document in Firestore at `users/{uid}`.
5. Store:
   - uid
   - email
   - displayName
   - createdAt
   - lastLoginAt
6. Add an auth state observer so the app routes users correctly:
   - logged out -> Login screen
   - logged in but no pregnancy profile -> Onboarding
   - logged in with pregnancy profile -> Home
7. Add basic Firebase error handling.
8. Add or update tests where practical.
9. Update docs if setup steps are needed.

## Out of scope
- Do not implement pregnancy profile form yet, only route placeholder.

## Security rules
- Do not commit private keys or secrets.
- Keep user data scoped by UID.

## Acceptance criteria
- User can sign in with Google.
- User can sign out.
- Firestore user document is created or updated.
- App routes based on auth/profile state.

## Required commit message
`Phase 1: Add Firebase Google authentication`
