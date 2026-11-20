# Changelog

## 2.0.0 — GeckoView edition

- Replaced Android System WebView with Mozilla GeckoView stable 160.0.20261116130455.
- Removed the built-in AdBlockEngine, bundled filter domains, and filter-list updater.
- Added WebExtension installation and management using GeckoView's WebExtensionController APIs.
- Added user permission prompts for extension installs, optional permissions and updates.
- Added installation from local Mozilla-signed `.xpi` files and direct HTTPS `.xpi` URLs.
- Added management controls for installed extensions: enable, disable, options and uninstall.
- Added a visible on-screen pointer controlled by Android TV D-pad arrows and OK/Enter clicks.
- Replaced the previous homepage with a cleaner TV layout and large website shortcut tiles.
- Made desktop user-agent and desktop viewport the default, with a desktop/mobile toggle.
- Increased minimum Android version from API 23 to API 26 to match GeckoView requirements.
- Retained tabs, bookmarks, private tabs and a physical-keyboard shortcut set.

### Compatibility notes

- GeckoView does not run Chrome Web Store extensions.
- Firefox WebExtensions must be compatible with GeckoView; not every add-on is supported.
- Remote XPI packages must be Mozilla-signed.
- Video formats, DRM and maximum resolution remain device/provider dependent.
- The universal APK is substantially larger because GeckoView bundles a full browser engine.

## 1.5.0 — Previous WebView release

- Added web file-upload support, user-initiated new tabs, media-autoplay preference and improved filter matching.
- This version used Android System WebView and a partial built-in filter engine. That engine was removed in 2.0.0.

## 1.4.0 and earlier

See the git history for earlier TV toolbar, tab, homepage, download, history, bookmark and filter-engine work. Older validation notes are historical and do not apply to the GeckoView 2.0.0 build.
