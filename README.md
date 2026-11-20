# JBrowser for Android TV

**Current source version: 2.1.0 (GeckoView edition, per-ABI optimized)**

JBrowser is an Android TV browser powered by Mozilla GeckoView. It supports compatible Firefox WebExtensions, a remote-controlled on-screen pointer, desktop browsing by default, and a TV-oriented homepage. The built-in ad blocker was removed in 2.0.0.

## 2.1.0 size and packaging changes

- Generates separate APKs for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.
- No universal APK is generated; each APK contains exactly one CPU architecture.
- Release builds enable R8 code shrinking and Android resource shrinking.
- Native GeckoView libraries are compressed in the downloaded APK to prioritize download size. Android extracts those libraries during installation, so installed storage usage may be higher than the APK download size.
- The GitHub workflow verifies that each APK contains exactly one ABI and uploads four separate artifacts.

**Important:** The individual APK sizes have not yet been measured. The 2.1.0 build must run in GitHub Actions before actual download sizes can be reported. GeckoView's native engine remains the largest part of the app; R8 cannot meaningfully shrink those native binaries.

## Choose the correct APK

| APK | Device architecture |
|---|---|
| `JBrowser-2.1.0-Android-TV-arm64-v8a.apk` | Most modern 64-bit ARM Android TVs and TV boxes |
| `JBrowser-2.1.0-Android-TV-armeabi-v7a.apk` | Older 32-bit ARM Android TVs and TV boxes |
| `JBrowser-2.1.0-Android-TV-x86.apk` | 32-bit Intel/AMD Android devices |
| `JBrowser-2.1.0-Android-TV-x86_64.apk` | 64-bit Intel/AMD Android devices and emulators |

Install only the APK matching the device's supported ABI. For a device check, use AIDA64 or a similar system-information app and look at Android/System ABI information. Sony X80J models are expected to use ARM64, but confirm the device's reported ABI before choosing.

## Features

- Mozilla GeckoView browser engine.
- Compatible, Mozilla-signed Firefox WebExtensions; not every Firefox add-on works and Chrome Web Store extensions are not supported.
- Extension manager: install from Firefox Add-ons, local signed XPI, or direct HTTPS XPI URL; manage installed extensions.
- Visible remote pointer controlled by D-pad and OK/Enter.
- Desktop user-agent and desktop viewport by default, with mobile-mode toggle.
- Clean TV homepage with large shortcuts.
- Tabs, bookmarks, private tabs and video playback, subject to device/provider capabilities.
- No built-in ad blocker or bundled filter lists.

## Compatibility

- Minimum Android: 8.0 / API 26.
- Compile SDK: Android API 37.0 preview channel used by the current build configuration.
- Target SDK: 36.
- Java: 17.
- GeckoView: `160.0.20261116130455`.
- GeckoView-supported ABIs: ARMv7, ARM64, x86 and x86_64.
- Video codecs, DRM, HDR and maximum resolution depend on the TV hardware, Android release, GeckoView and streaming provider.

## Build on GitHub

1. Upload the complete project to the repository root, including `.github/workflows/build-apk.yml`.
2. A push to `main` or `master`, or a manual workflow dispatch, builds four optimized release APKs.
3. Download the matching ABI artifact from the Actions run. Each artifact downloads as a ZIP containing one APK.
4. To publish all four APKs on a GitHub Release, create and push a tag named `v2.1.0`.

No Android Studio is required on your computer when using GitHub Actions.

## Signing and upgrades

The current workflow signs release-test APKs with the runner's debug key for easy side-loading. A persistent signing key is required for seamless in-place updates. Never commit a private keystore or passwords to a public repository; configure them as GitHub Actions secrets before production distribution. Because each hosted runner has its own temporary debug key, uninstalling an older build may be required before installing this build.

## Controls

- D-pad arrows: move the on-screen pointer while pointer mode is enabled.
- OK / Enter: click at the pointer.
- Page Up / Page Down or Channel Up / Channel Down: scroll when exposed by the remote.
- Back: navigate back or return to homepage.
- Physical keyboard: Ctrl+T new tab; Ctrl+W close tab; Ctrl+L address bar; Ctrl+R reload.

## Size testing

The workflow prints the compressed size of each produced APK in its log. Compare the actual output of 2.1.0 against the previous 500 MB universal APK. The size reduction is expected to come mainly from ABI splitting and native-library compression, not R8.
