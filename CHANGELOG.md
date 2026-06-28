# Changelog

## 0.1.6 - 2026-06-28

- Merged the AI fix branches back into `main`, preserved saved Firebase AI summaries when local nutrition summaries are regenerated, and reused saved AI nutrition totals for repeat daily, weekly, and report nutrition views.

## 0.1.5 - 2026-06-28

- Published an installable signed GitHub APK release using the debug signing configuration for sideload testing.

## 0.1.4 - 2026-06-28

- Added Firebase persistence for AI summaries and nutrition processing metadata.

## 0.1.3 - 2026-06-28

- Fixed daily AI summary generation by routing free hourly Pollinations requests through the tested anonymous legacy POST endpoint with the accepted low reasoning setting, repairing array-style AI nutrition estimates, and preventing stale setup-required state from blocking configured free mode.

## 0.1.2 - 2026-06-28

- Fixed Pollinations summary generation by using the available low-cost `nova-fast` model on the new account API, keeping free requests on the anonymous `openai-fast` legacy path, and mapping Pollen budget errors to a clear quota fallback.

## 0.1.1 - 2026-06-28

- Fixed AI nutrition summary parsing fallback and added AI-assisted nutrition estimates with local fallback support.
