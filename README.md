# ApexHub OTA Sample — end-to-end update verification

This repository is a **self-contained, CI-verified proof** that the
[ApexHub Android SDK](https://github.com/Mr-Perfect-252/apexhub-android-sdk) detects a
newly-published version on the live ApexHub backend and **prompts the user to update**.

## What it contains

| Module | Purpose |
| ------ | ------- |
| `:sdk` | A vendored copy of the ApexHub Android SDK source (so CI needs no GitHub Packages auth). |
| `:app` | A minimal sample app (`com.apexhub.ota.sample`, versionCode **1** / versionName **1.0.0**) that initializes the SDK and calls `checkAndPrompt()` on launch. |

## The scenario under test

1. The app **`com.apexhub.ota.sample`** was registered on the live ApexHub backend
   (`https://apex-hub-production.vercel.app`), which issued the public key baked into
   `app/build.gradle.kts`.
2. Version **1.0.0 (code 1)** was published — this is the app built by CI (the "installed" build).
3. Version **1.0.1 (code 2)** was then published to the backend.
4. When the v1 app launches, the SDK calls
   `GET /api/update/com.apexhub.ota.sample?channel=stable&installed=1`, the backend replies
   `updateAvailable: true, latestVersion: "1.0.1"`, and the SDK shows the **"Update available"** dialog.

Live backend response for an installed v1 client:

```json
{ "updateAvailable": true, "latestVersion": "1.0.1", "versionCode": 2,
  "mandatory": false, "channel": "stable", "rolloutPercent": 100 }
```

## How CI proves it (`.github/workflows/verify-ota.yml`)

**Job 1 — Build + deterministic tests** (`build-and-unit-test`)
- Builds the v1 APK and uploads it as the artifact `apexhub-ota-sample-v1-apk` (the "downloaded" baseline).
- Runs Robolectric tests (`app/src/test/...`) that assert:
  - `detectsUpdateFromMockedBackend` — SDK parses a v2 response into `UpdateAvailable(1.0.1)`.
  - `detectsUpdateFromLiveBackend` — the **real** production backend advertises 1.0.1 to an installed v1 client.
  - `showsUpdatePromptDialog` — the SDK actually shows a dialog titled **"Update available"** reading **"Version 1.0.1 is available."**

**Job 2 — Emulator screenshot** (`instrumented-emulator-test`)
- Installs the v1 APK on a real Android emulator, launches it, waits for the live-backend-driven
  **"Update available"** dialog via UiAutomator, asserts its text, and uploads a **screenshot**
  (`ota-update-prompt-screenshot`) as visual evidence that the app asked to update.

## Running locally

```bash
./gradlew :app:assembleDebug          # build the v1 APK
./gradlew :app:testDebugUnitTest      # deterministic SDK detection + prompt proof
./gradlew :app:connectedDebugAndroidTest   # emulator screenshot (requires a running emulator)
```
