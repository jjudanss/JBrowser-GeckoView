# JBrowser for Android TV

**Source version: 2.2.0 — GeckoView edition**

JBrowser is a desktop-first Android TV browser built with Mozilla GeckoView. It is designed for TV remotes, an on-screen pointer, and compatible Firefox WebExtensions.

## What's new in 2.2.0

- Removed the built-in ad-filter engine; JBrowser does not ship its own filter lists.
- Added a one-tap option to install or open the official Mozilla Add-ons version of **uBlock Origin**. The extension is downloaded after the user requests it; it is not bundled in the APK.
- Extension permission prompts are displayed on Android's main UI thread.
- Improved remote Select/OK click handling: both key-down and key-up are consumed, mouse down/up use the same GeckoSession and down-time, and more common TV Select key codes are recognized.
- Smoother pointer movement: pointer animation is drawn locally and Gecko receives a hover update when movement settles instead of on every animation frame.
- Reduced retained tabs from four to three to lower memory use on TV devices.
- Lower-priority inactive GeckoSessions are retained with media suspended.

## uBlock Origin

Use **Extensions → Install / open uBlock Origin** (or the homepage shortcut). JBrowser downloads the current XPI from Mozilla Add-ons and asks GeckoView to validate the Mozilla signature and request the extension's permissions. The user must approve the permissions before installation.

uBlock Origin is optional and is not embedded in the base APK. This keeps the extension's download separate from the browser installation. uBlock Origin's current Mozilla Add-ons package is approximately 6 MB; its size/version may change as the publisher updates it.

Important limitations:

- GeckoView supports a subset of Firefox WebExtensions APIs. Not every Firefox add-on is compatible with GeckoView.
- Chrome Web Store extensions are not supported.
- The uBlock Origin installation path must be tested on the target device and GeckoView version. Some extension features may depend on APIs that GeckoView does not expose.
- No browser extension can guarantee removal of every YouTube ad or bypass every site's anti-adblock measures.

## Browser and remote controls

- Desktop user-agent and desktop viewport are enabled by default.
- D-pad arrows move the visible pointer while pointer mode is on.
- OK, Enter, Select, A and Space act as pointer clicks.
- Page Up/Down and supported Channel Up/Down keys scroll the webpage.
- Physical keyboard: Ctrl+T new tab, Ctrl+W close tab, Ctrl+L address bar, Ctrl+R reload.
- Three-tab ceiling is used to limit the number of active browser sessions held by the app.

## Compatibility

- Minimum Android version: Android 8.0 / API 26.
- Supported GeckoView 160 ABIs in this project: `armeabi-v7a`, `arm64-v8a`, and `x86_64`.
- There is no 32-bit x86 APK because this GeckoView package does not provide that native engine ABI.
- This aims to support Android TV devices running Android 8.0 or newer and using one of those ABIs. It is not compatible with every Android TV device ever made.
- DRM playback, HDR, maximum resolution, codecs and performance depend on the TV hardware, Android version, GeckoView and streaming provider.

## APK editions

GitHub Actions produces three separate release APKs, each containing one ABI:

- `JBrowser-2.2.0-Android-TV-armeabi-v7a.apk`
- `JBrowser-2.2.0-Android-TV-arm64-v8a.apk`
- `JBrowser-2.2.0-Android-TV-x86_64.apk`

Use only the APK matching the TV's Android ABI. The edition known to install on the maintainer's Sony TV is ARMv7; for other TVs, check the system ABI first rather than guessing from model branding.

## APK size versus installed storage

The 2.1.0 ARMv7 APK download was about 85 MB, but the user reported an installed application footprint around 280 MB. This is expected to be higher with GeckoView because its native engine libraries are extracted when the compressed APK is installed. ABI splitting reduces the download by excluding unused CPU architectures; it does not make the native engine itself small.

2.2.0 adds app-level memory optimizations (three-tab limit, inactive session priority and reduced pointer-event churn). These do not materially shrink GeckoView's native engine. The actual 2.2.0 APK and installed sizes must be measured after the GitHub build and device installation. A 40 MB installed footprint is not a realistic target while bundling GeckoView and full extension support.

## Build with GitHub Actions

1. Upload the project contents to the repository root, including `.github/workflows/build-apk.yml`.
2. A push to `main` or `master`, or a manual dispatch, builds the three ABI-specific release APKs.
3. Download the desired artifact from the Actions run. Each artifact is a ZIP containing one APK.
4. To publish the APKs as a GitHub Release, create and push a tag beginning with `v`, such as `v2.2.0`.

No Android Studio is required on the maintainer's computer when using GitHub Actions.

## Signing

The workflow signs release-test APKs with GitHub Actions' debug signing key. GitHub-hosted runners use temporary debug keys, so a later build may not install as an in-place update over an APK signed by an earlier run. Uninstalling the old build may be required and can erase local browser data. A persistent private keystore is needed for production-quality upgrades; keep its contents and passwords in GitHub Actions secrets, never in a public repository.
