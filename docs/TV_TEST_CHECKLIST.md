# JBrowser 2.2.0 Android TV test checklist

## Installation and compatibility
- [ ] Confirm Android 8.0 / API 26 or newer.
- [ ] Confirm device ABI and install the matching APK.
- [ ] Confirm the TV launcher displays the JBrowser icon and banner.
- [ ] Record the APK download size and Android-reported installed app size.

## Remote pointer and clicks
- [ ] D-pad moves the visible pointer in all directions.
- [ ] Pointer movement remains responsive during repeated key presses.
- [ ] OK / D-pad Center clicks a visible website button.
- [ ] OK / Select activates search fields, search buttons, links and video controls.
- [ ] Enter, remote A/Select, and supported keyboard Enter also click.
- [ ] Pointer toggle returns to normal D-pad page navigation.
- [ ] Page Up/Down or supported Channel Up/Down scrolls the page.
- [ ] A Bluetooth/USB mouse (if attached) still works normally.

## Desktop browsing
- [ ] Desktop mode is enabled on first launch.
- [ ] Google and a few ordinary desktop sites display at a usable desktop scale.
- [ ] Mobile mode toggle works and can be switched back.
- [ ] Address bar, back, forward, reload, home and tabs work.
- [ ] New-tab links open within the tab limit.

## Firefox WebExtensions and uBlock Origin
- [ ] Extensions menu opens.
- [ ] Install / open uBlock Origin points to the official Mozilla Add-ons XPI.
- [ ] Mozilla signature validation succeeds.
- [ ] Permission dialog is displayed and can be approved or cancelled using the remote.
- [ ] uBlock Origin appears in the installed extension list.
- [ ] Extension enable, disable, options and uninstall actions work.
- [ ] Test a variety of sites; do not assume every uBlock feature or every YouTube ad is blocked by GeckoView.

## Memory and responsiveness
- [ ] Open three tabs and switch between them.
- [ ] Inactive tab playback suspends.
- [ ] The app remains responsive after switching tabs several times.
- [ ] Observe RAM use and any renderer crashes on low-memory TVs.

## Video
- [ ] Test standard HTML5 video.
- [ ] Test fullscreen entry and exit.
- [ ] Test sound, pause/play and seeking where supported.
- [ ] Note that DRM, 4K, HDR and codec support depend on the device and streaming provider.
