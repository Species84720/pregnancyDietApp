# Changelog

## 0.1.2 - 2026-06-28

- Fixed Pollinations summary generation by using the available low-cost `nova-fast` model on the new account API, keeping free requests on the anonymous `openai-fast` legacy path, and mapping Pollen budget errors to a clear quota fallback.

## 0.1.1 - 2026-06-28

- Fixed AI nutrition summary parsing fallback and added AI-assisted nutrition estimates with local fallback support.
