# Changelog

## 2.2.0 — Lightweight TV interaction and optional uBlock Origin

- Kept the GeckoView 160 engine and desktop-first website rendering.
- Removed the app's bundled ad-filter engine; no custom filter lists ship in the APK.
- Added a homepage and Extensions-menu action to install/open the current official Mozilla-signed uBlock Origin XPI on demand.
- Added main-thread handling for WebExtension installation and permission dialogs.
- Improved remote click handling by consuming both Select/OK key-down and key-up events, supporting additional common Select key codes, and keeping each mouse down/up sequence on the same GeckoSession with a shared down-time.
- Reduced cursor input overhead: animate the pointer overlay locally and send Gecko hover events when movement settles instead of generating a Gecko motion event on every animation frame.
- Limited open tabs to three and apply low priority to inactive sessions with media suspended.
- Continued to build separate `armeabi-v7a`, `arm64-v8a`, and `x86_64` APKs; 32-bit x86 is not included because the selected GeckoView artifact does not provide that native ABI.
- Updated application version to 2.2.0 / version code 10.

### Size note

The 2.1.0 ARMv7 downloaded APK was approximately 85 MB while the reported installed footprint was around 280 MB. This difference is primarily GeckoView's native engine libraries being extracted at installation. ABI splits reduce the APK download by excluding other CPU architectures, but do not materially reduce the extracted native engine's installed footprint. 2.2.0 targets lower runtime memory and pointer overhead, not a guaranteed major reduction in installed storage.

## 2.1.0 — GeckoView per-ABI packaging

- Added separate GeckoView APKs for ARM64, ARMv7 and x86_64.
- Enabled R8 and resource shrinking for release builds.
- Compressed native libraries in APK downloads; Android extracts these on install.
- Removed the unsupported 32-bit x86 build after GeckoView did not produce that ABI.

## 2.0.0 — GeckoView edition

- Replaced Android System WebView with Mozilla GeckoView.
- Added Firefox WebExtension management, desktop-first browsing and a remote pointer.
- Removed the built-in ad blocker.
