# Built-in ad blocking removed

JBrowser 2.2.0 does not include a built-in ad-blocking engine or bundled filter lists. The previous WebView-era filter source notes no longer apply.

Users may optionally install the official Mozilla Add-ons version of uBlock Origin from within JBrowser using **Extensions → Install / open uBlock Origin**. The extension is downloaded only after the user requests it and is not bundled in the base APK. GeckoView checks the Mozilla signature and displays the extension's permission prompt.

Not every Firefox WebExtension API is available in GeckoView. uBlock Origin compatibility and YouTube behavior must be tested on the actual GeckoView build; no extension guarantees that all ads, including all YouTube ads, will be blocked.
