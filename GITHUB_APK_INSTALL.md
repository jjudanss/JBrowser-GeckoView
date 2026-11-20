# Build and install JBrowser 2.0.0 from GitHub

## Repository contents

Make sure the repository root contains:

- `.github/workflows/build-apk.yml`
- `app/`
- `build.gradle`
- `settings.gradle`
- `gradle.properties`

## Build

1. Open the repository's **Actions** tab.
2. Select **Build JBrowser GeckoView Android TV APK**.
3. Open the latest successful workflow run.
4. Under **Artifacts**, download `JBrowser-2.0.0-Android-TV`.
5. Extract the artifact ZIP. It contains `JBrowser-2.0.0-Android-TV.apk`.

## Release

Create a Git tag named `v2.0.0` and push it. The workflow builds the APK and attaches it to a prerelease on the repository's Releases page.

## Install

1. Transfer the APK to the Android TV or download it directly on the TV.
2. Open it with a file manager.
3. Allow installation from that file manager if Android asks.
4. Select Install.

Minimum supported OS: Android 8.0 (API 26). The workflow produces a debug-signed testing APK. Configure a persistent release keystore before distributing production updates.
