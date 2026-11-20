# Downloading JBrowser 2.1.0

The GitHub Actions workflow creates four separate APK artifacts and, on a `v*` tag, attaches them to a GitHub Release.

Choose only one APK matching the TV's CPU ABI:

- **ARM64:** `JBrowser-2.1.0-Android-TV-arm64-v8a.apk` — most modern ARM64 Android TVs.
- **ARMv7:** `JBrowser-2.1.0-Android-TV-armeabi-v7a.apk` — older 32-bit ARM Android TVs.
- **x86:** `JBrowser-2.1.0-Android-TV-x86.apk` — 32-bit Intel/AMD Android devices.
- **x86_64:** `JBrowser-2.1.0-Android-TV-x86_64.apk` — 64-bit Intel/AMD devices and emulators.

Check the device's ABI with AIDA64 or another hardware-information app. Do not assume that CPU model alone implies the OS ABI.

After the first build, check the file sizes in the GitHub Actions build log. No exact size is guaranteed before the workflow produces the APKs.

The APKs are release variants with R8 and resource shrinking enabled, signed with a temporary GitHub runner debug key. Uninstall a differently signed older build if Android rejects an update. For repeatable upgrades, configure a persistent signing keystore as repository secrets; never commit keystore files or passwords to a public repository.
