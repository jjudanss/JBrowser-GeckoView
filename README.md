# JBrowser for Android TV

**Current source version: 2.0.0 (GeckoView edition)**

JBrowser 2.0 is a major browser-engine change. It replaces Android System WebView with Mozilla GeckoView, removes JBrowser's built-in ad-blocking engine, adds a remote-controlled mouse pointer, and requests desktop websites by default.

## Version 2.0.0 highlights

- **Mozilla GeckoView engine:** bundles Mozilla's Gecko browser engine rather than relying on the TV's installed Android System WebView.
- **Firefox WebExtensions:** extension manager can open Firefox Add-ons, install a Mozilla-signed `.xpi` file, install from a direct HTTPS `.xpi` URL, list installed extensions, enable/disable them, open their options page, and uninstall them.
- **Permission prompts:** JBrowser asks before granting extension permissions and lists the requested website access.
- **TV pointer:** D-pad arrows move a visible on-screen mouse pointer; OK/Enter clicks at the pointer location. Toggle pointer mode from the toolbar.
- **Desktop-first browsing:** sessions default to GeckoView's desktop user-agent and desktop viewport. A toolbar control can switch to mobile mode.
- **Clean homepage:** large TV-friendly shortcuts, a simple hero area, and quick links to extensions and bookmarks.
- **Tabs and bookmarks:** multiple tabs, a tab switcher, bookmarks and private tabs remain available.
- **No built-in ad blocker:** the old JBrowser filter engine, filter lists and request interception have been removed. A user may install a compatible extension that provides content blocking, subject to extension compatibility.

## Important extension compatibility notes

JBrowser uses GeckoView's WebExtensions APIs. This is **not** a full Firefox-for-Android installation and does **not** support Chrome Web Store extensions. Only extensions compatible with GeckoView can run. Regular remote `.xpi` installs must be Mozilla-signed; unsigned packages are rejected by GeckoView. Some add-ons may depend on Firefox UI or APIs that an embedded GeckoView browser does not expose, so they may not work even if they are Firefox WebExtensions.

The extension manager offers:

1. Firefox Add-ons website.
2. Install from a local `.xpi` file.
3. Install from a direct HTTPS `.xpi` URL.
4. Manage installed extensions (enable, disable, open options, uninstall).

The Add-ons website's in-page installation flow depends on the site's GeckoView compatibility. Installing a downloaded Mozilla-signed `.xpi` from the extension manager is the fallback path.

## Android compatibility

- **Minimum Android version: 8.0 (API 26).** Mozilla's current GeckoView requires Android 8.0 or newer.
- Compile/target SDK: Android 16 / API 36.
- Java: 17.
- GeckoView stable: `160.0.20261116130455`.
- A single universal APK is built; GeckoView includes a substantial native browser engine, so the APK is much larger than the old WebView build.
- Video codecs, 4K/8K, HDR, DRM and hardware decoding still depend on the TV hardware, Android version, GeckoView support and streaming provider.

## Build a single APK on GitHub

The repository includes `.github/workflows/build-apk.yml`.

1. Upload the complete project contents to the repository root (including `app/`, `docs/`, Gradle files and `.github/workflows/build-apk.yml`).
2. Push to `main` or `master`, or run the workflow manually from the **Actions** tab.
3. After the workflow succeeds, download the `JBrowser-2.0.0-Android-TV` artifact. The downloaded artifact is a ZIP containing the APK.
4. To publish a GitHub Release, create and push a tag named `v2.0.0`. The workflow attaches `JBrowser-2.0.0-Android-TV.apk` to the release.

You do not need Android Studio on your own computer when using GitHub Actions.

### Signing note

The workflow creates a debug-signed testing APK. A persistent release keystore is required for repeatable in-place upgrades. Do not commit a keystore or its passwords to a public repository; configure them as GitHub Actions secrets before distributing production releases.

## Controls

- D-pad arrows: move the pointer while pointer mode is on.
- OK / Enter: click at the pointer.
- Page Up / Page Down or Channel Up / Channel Down: scroll the page when supported by the remote.
- Toggle pointer mode from the pointer toolbar button.
- Back: navigate back or return to the homepage.
- Ctrl+T: new tab; Ctrl+W: close current tab; Ctrl+L: address bar; Ctrl+R: reload (physical keyboard).
- Toolbar: Back, Forward, Reload, Home, Tabs, Pointer, Desktop/Mobile, Extensions, Bookmark, More.

## Project layout

- `app/src/main/java/com/jbrowser/tv/MainActivity.java` — GeckoView browser shell, tabs, TV pointer and extension manager.
- `app/src/main/java/com/jbrowser/tv/BookmarkStore.java` — bookmarks and encrypted private bookmarks.
- `app/src/main/AndroidManifest.xml` — Android TV launcher and browser declarations.
- `.github/workflows/build-apk.yml` — GitHub Actions APK build/release workflow.

## Build locally (optional)

Open the project in Android Studio with JDK 17 and Android SDK Platform 36 installed. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.
