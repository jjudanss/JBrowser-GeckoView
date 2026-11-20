# Build and install JBrowser 2.2.0

## GitHub Actions

1. Upload the complete project contents to the repository root, including the `.github` folder and `app` folder.
2. Commit the files to `main`.
3. Open the repository's Actions tab and wait for **Build JBrowser GeckoView Android TV APKs** to finish successfully.
4. The run produces three artifacts: ARMv7, ARM64 and x86_64. Each downloaded artifact is a ZIP containing one APK.

## Which APK should I use?

The Sony TV tested by the project maintainer installed the ARMv7 variant. For that same device, download only **JBrowser-2.2.0-ARMv7** from the Actions artifacts and extract the APK.

For other Android TV devices, use the ABI reported by a system-information app. Do not choose an APK based only on the TV brand or resolution. 32-bit x86 is not built because the selected GeckoView package does not include it.

## Install

1. Copy the extracted APK to a USB drive or download it directly to the TV.
2. Open the APK using a file manager.
3. Allow that file manager to install unknown apps if Android asks.
4. Install JBrowser.

The current test workflow uses a temporary debug signing key. If Android reports a signature conflict with an older JBrowser build, uninstall the previous build before installing. This can erase its local browser data. Configure a persistent private signing key through GitHub Actions secrets before distributing production updates.
