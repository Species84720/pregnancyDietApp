# Implement Next Phase — Reusable Agent Prompt

Use this same prompt every time with GitHub Copilot Agent / GPT-5.5:

```text
You are working on the Pregnancy Diet Tracker Android app.

Your task is to implement the next uncompleted phase only.

First, read these files in order:
1. README.md
2. .github/copilot-instructions.md
3. agentic/MASTER_PLAN.md
4. agentic/PHASE_INDEX.md
5. agentic/PHASE_STATE.md
6. agentic/ACCEPTANCE_CRITERIA.md
7. docs/REQUIREMENTS.md
8. docs/FIREBASE_SCHEMA.md
9. docs/AI_CONTRACT.md
10. docs/NUTRITION_ENGINE.md
11. docs/SAFETY_RULES.md

Then:
1. Determine the next phase whose status is not DONE in agentic/PHASE_STATE.md.
2. Open the matching file under agentic/phases/.
3. Implement only that phase.
4. Do not skip ahead.
5. Do not modify unrelated future-phase functionality unless required to keep the app compiling.
6. Keep code clean, modular, testable, and consistent with existing project structure.
7. Use Kotlin, Jetpack Compose, Firebase, repository/ViewModel patterns, and Android best practices.
8. Keep all health and pregnancy data user-scoped and private.
9. Never place AI provider secrets in Android app code.
10. Add or update tests where practical.
11. Update documentation if implementation changes behavior.
12. Run available formatting, build, and tests.
13. Update agentic/PHASE_STATE.md by marking the completed phase as DONE, including date, summary, tests run, build result, commit hash if available, and any known issues.
14. Commit all relevant changes with the commit message specified in the phase file.
15. Push to the current remote branch.
16. Report the result with:
    - phase implemented
    - files changed
    - tests run
    - build result
    - commit hash
    - push result
    - next phase to implement

If a phase cannot be completed fully, still commit the working partial implementation only if it builds, then mark the phase as PARTIAL in agentic/PHASE_STATE.md with the reason and remaining tasks.
```

## Short version

After the first run, you can usually tell the coding agent:

```text
Implement the next uncompleted phase using agentic/IMPLEMENT_NEXT_PHASE.md. Commit and push when done.
```
