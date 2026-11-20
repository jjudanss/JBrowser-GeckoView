# Changelog

## 2.1.0 — GeckoView size-optimized release

- Added ABI-specific APK outputs for ARM64, ARMv7, x86 and x86_64.
- Disabled universal APK generation so each download contains only one native engine architecture.
- Enabled R8 code shrinking and Android resource shrinking for release builds.
- Enabled compressed native-library packaging to reduce APK download size; Android extracts native libraries during install, so installed storage may be larger.
- Changed GitHub Actions to build optimized signed release APKs, verify the ABI included in each APK, print file sizes, and publish all four APKs to GitHub Releases.
- Updated app version to 2.1.0 / version code 8.
- Preserved GeckoView, compatible Firefox WebExtensions, desktop mode, remote pointer and TV homepage.
- Continued to exclude the built-in ad blocker.

### Size caveat

Actual APK sizes are not known until GitHub Actions completes the first 2.1.0 build. No under-100-MB guarantee is made. GeckoView native binaries dominate the package and are not reduced by R8.

## 2.0.0 — GeckoView edition

- Replaced Android System WebView with Mozilla GeckoView stable 160.0.20261116130455.
- Removed built-in ad blocking.
- Added compatible Firefox WebExtension management, remote pointer and desktop-first browsing.
- Set minimum Android version to API 26.

## 1.5.0 — Previous WebView release

- Added file uploads, user-initiated new tabs, autoplay preference and enhanced filter matching.
- Used Android System WebView and a partial built-in filter engine; this engine was removed in 2.0.0.
