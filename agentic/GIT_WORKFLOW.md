# Git Workflow for Agentic Build

## Preferred branch strategy

Use one implementation branch for the full MVP build:

```bash
git checkout -b mvp-pregnancy-diet-tracker
```

Each phase should commit to the same branch and push after completion.

## Required end-of-phase commands

The agent should run equivalent commands after each phase:

```bash
git status
# run project-specific build/test commands, for example:
./gradlew build
./gradlew test

git add .
git commit -m "<phase commit message>"
git push
```

If build/test commands differ, use the commands available in the repo.

## Commit rules

- One phase = one main commit, unless a fix commit is needed.
- Do not mix future-phase features into the current phase commit.
- Do not commit local secrets, API keys, generated private config, or personal files.
- The Android app must not contain Pollinations.ai secrets.

## If the phase partially fails

If implementation is incomplete but the project builds:

1. Commit the safe working partial implementation.
2. Mark the phase as `PARTIAL` in `agentic/PHASE_STATE.md`.
3. Add remaining tasks in the Notes column.
4. Push.

If the project does not build:

1. Fix until it builds, or revert unsafe changes.
2. Do not mark the phase as DONE.
3. Do not push broken code unless explicitly instructed by the owner.
