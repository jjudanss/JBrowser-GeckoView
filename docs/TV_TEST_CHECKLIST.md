# JBrowser 2.1.0 Android TV test checklist

## Install and ABI
- [ ] Confirm Android 8.0 / API 26 or newer.
- [ ] Identify the device ABI (arm64-v8a, armeabi-v7a, x86 or x86_64).
- [ ] Install only the matching APK.
- [ ] Record downloaded APK size and installed storage usage.

## First launch and TV UI
- [ ] Confirm homepage opens without a crash.
- [ ] Confirm shortcut tiles respond to D-pad focus and OK.
- [ ] Confirm address bar opens the TV keyboard.
- [ ] Confirm remote pointer is visible and moves with the D-pad.
- [ ] Confirm OK/Enter clicks at the pointer location.

## Browser engine and desktop mode
- [ ] Open a desktop website and confirm desktop layout by default.
- [ ] Switch to mobile mode and back to desktop mode.
- [ ] Test back, forward, reload, homepage and fullscreen video.

## Extensions
- [ ] Open the Extensions screen.
- [ ] Install a Mozilla-signed GeckoView-compatible XPI.
- [ ] Verify permission prompts.
- [ ] Enable, disable, open options and uninstall an extension.
- [ ] Confirm unsupported add-ons fail gracefully.
- [ ] Do not expect Chrome Web Store extension support.

## Tabs, bookmarks and media
- [ ] Open and switch among multiple tabs.
- [ ] Close a tab and verify remaining sessions.
- [ ] Add and reopen a bookmark.
- [ ] Test YouTube playback and a second video site.
- [ ] Test fullscreen and media keys.
- [ ] Confirm DRM, codecs and maximum resolution are device/provider dependent.
- [ ] Confirm JBrowser itself does not block ads.

## Performance and stability
- [ ] Compare cold-start time with 2.0.0.
- [ ] Observe memory usage with one, three and six tabs.
- [ ] Observe available storage after installation (compressed .so files are extracted).
- [ ] Background and resume the browser.
- [ ] Check that extension functionality remains intact in the optimized release.
