# Phase 8: AI Integration Contract and Backend Boundary

## Goal
Create the Android-side AI contract while keeping provider secrets outside the app.

## Scope
1. Create AI module interfaces in the Android app.
2. Do not call Pollinations.ai directly from Android with secrets.
3. Implement abstractions such as:
   - AiRepository
   - AiService
   - AiRequestBuilder
   - AiResponseParser
4. Assume the app calls a secure backend/Firebase Cloud Function.
5. Define request payloads for:
   - daily nutrition summary
   - symptom explanation
   - weekly summary
6. Define structured AI response models matching `docs/AI_CONTRACT.md`.
7. Add safe fallback if AI fails:
   - show local nutrition gaps
   - show generic safe guidance
   - do not block logging
8. Add prompt guardrails:
   - no diagnosis
   - no medication changes
   - escalate red flags
   - food-based suggestions preferred
   - explain uncertainty
9. If this repo includes Firebase Functions, create placeholder function structure.
10. Add tests for AI response parsing.

## Acceptance criteria
- AI models and service interfaces exist.
- No AI secrets are in Android code.
- Fallback behavior exists.
- Response parsing tests exist.

## Required commit message
`Phase 8: Add AI integration contract`
