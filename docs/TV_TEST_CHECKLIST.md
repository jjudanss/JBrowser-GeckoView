# JBrowser 2.0.0 Android TV test checklist

## First launch
- [ ] Install on an Android TV / TV box running Android 8.0 or newer.
- [ ] Confirm the homepage opens without a crash.
- [ ] Confirm large shortcut tiles respond to D-pad focus and OK.
- [ ] Confirm the address bar opens the TV keyboard and navigates to HTTPS sites.

## Browser engine and desktop mode
- [ ] Open a desktop website (e.g. Wikipedia or a news site).
- [ ] Confirm desktop layout is requested by default.
- [ ] Use the toolbar to switch to mobile mode and back to desktop mode.
- [ ] Check back, forward, reload and homepage controls.

## Remote pointer
- [ ] Confirm the pointer is visible over websites by default.
- [ ] Move it with D-pad arrows.
- [ ] Click links and buttons with OK / Enter.
- [ ] Toggle pointer mode off and confirm normal browser navigation is available.
- [ ] Test with a physical USB/Bluetooth mouse if available.

## Tabs and bookmarks
- [ ] Open multiple tabs and switch between them.
- [ ] Open a link with target=_blank and confirm a new tab is created.
- [ ] Close a tab and verify the remaining tabs still work.
- [ ] Bookmark a page and reopen it.
- [ ] Open a private tab and confirm it uses GeckoView private mode.

## Extensions
- [ ] Open the Extensions screen.
- [ ] Browse the Firefox Add-ons directory.
- [ ] Install a Mozilla-signed, GeckoView-compatible XPI file.
- [ ] Confirm the permission prompt identifies requested permissions and host access.
- [ ] Verify an installed extension appears in the manager.
- [ ] Enable, disable and uninstall an extension.
- [ ] Confirm an extension requiring unsupported APIs is rejected or reported gracefully.
- [ ] Do not expect Chrome Web Store extension support.

## Media and site compatibility
- [ ] Test YouTube playback and fullscreen.
- [ ] Test audio/video on another streaming site.
- [ ] Verify playback and maximum quality are limited by the TV hardware, DRM support and provider.
- [ ] Confirm JBrowser itself does not block ads; no built-in ad blocker is included.

## Stability
- [ ] Test on a 2 GB RAM TV box and observe memory usage.
- [ ] Test switching between six tabs.
- [ ] Test returning to JBrowser after backgrounding the app.
- [ ] Test app rotation / TV display mode changes if the device supports them.
