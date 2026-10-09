# Google Play Store Publication Guide — TwinSpace

This document provides all details, configurations, and responses required to publish **TwinSpace** on the Google Play Console.

---

## 1. Production Artifacts

| Artifact | File Location | Size | Status |
| :--- | :--- | :--- | :--- |
| **Android App Bundle (.aab)** *(Upload to Play Console)* | `app/build/outputs/bundle/release/app-release.aab` | ~4.9 MB | Signed, Minified (R8), Resource Shrunk |
| **Standalone Release APK** *(For direct testing/sideloading)* | `app/build/outputs/apk/release/app-release.apk` | ~2.5 MB | Signed with Release Keystore |

---

## 2. Release Signing Keystore Information

> [!IMPORTANT]
> Keep `twinspace-release.jks` backed up in a secure offline location. Google Play requires this key (or Google Play App Signing) for all subsequent app updates.

- **Keystore File:** `e:\app_cloner\twinspace-release.jks`
- **Keystore Password:** `twinspace2026`
- **Key Alias:** `twinspace`
- **Key Password:** `twinspace2026`
- **Validity:** 10,000 days (until year 2054)

---

## 3. Google Play Console Policy Declarations

### A. Core Permission: `QUERY_ALL_PACKAGES`
When prompted in Google Play Console regarding high-risk permissions:
- **Category:** App Management / Cloner / Multi-Account Utility
- **Compliance Question:** *Why does your app need the `QUERY_ALL_PACKAGES` permission?*
- **Recommended Justification Text:**
  > "TwinSpace is an app cloning and dual space utility. The core user-facing functionality requires displaying the list of installed applications on the user's device so that the user can select and create isolated dual/multiple parallel instances of their applications (e.g., social messaging, productivity). Without `QUERY_ALL_PACKAGES`, the app cannot enumerate installed applications to allow the user to select apps to clone."

### B. High-Risk Permissions Audit
- `REQUEST_INSTALL_PACKAGES` has been **removed** from `AndroidManifest.xml` to avoid Play Store rejection.
- The app uses native Android User Profiles / Hardware Sandboxing APIs, compliant with Google Play policies.

### C. Data Safety Questionnaire
- **Does your app collect or share any user data?** `No`
- **Does your app collect device IDs, location, or personal data?** `No`
- **Is all data processed locally on the device?** `Yes`
- **Is data encrypted in transit?** `N/A` (no network data transmission)

### D. Target Audience and Content
- **Target Age:** 13 and older (or 18+)
- **Ads:** Select `No, my app does not contain ads` (unless third-party ad networks are integrated later)

---

## 4. Privacy Policy Requirement
Google Play requires a public URL to your Privacy Policy:
1. Upload [PRIVACY_POLICY.md](file:///e:/app_cloner/PRIVACY_POLICY.md) to GitHub Pages, Notion, or your custom domain (e.g. `https://yourdomain.com/privacy`).
2. Paste the URL into **Google Play Console > Policy > Privacy Policy**.

---

## 5. Live Physical Device Test Results (Vivo V2407)
- **Multi-Instance Cloning:** Verified with 4 WhatsApp instances (1 default + 3 clones) displaying distinct numbered badges (`1`, `2`, `3`, `4`) and labels (`DEFAULT`, `CLONE #1`, `CLONE #2`, `CLONE #3`).
- **Profile Activity Deletion:** Verified that removing a clone removes its card and doesn't resurrect deleted instances.
- **Auto-Refresh Lifecycle:** Verified that resuming the app refreshes state without requiring app restart.
- **R8 ProGuard & Shrinking:** Release build successfully compiled and ran cleanly on Vivo hardware with zero obfuscation crashes.
