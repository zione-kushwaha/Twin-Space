# Privacy Policy for TwinSpace

**Last updated:** October 16, 2026

**TwinSpace** ("we", "our", or "us") is dedicated to protecting your privacy. This Privacy Policy outlines how your information is handled when you use the TwinSpace application ("App").

---

### 1. Zero Personal Data Collection
TwinSpace operates entirely locally on your device. 
- We do **not** collect, store, sell, or transmit any personally identifiable information (PII).
- We do **not** track your usage across apps or websites.
- We do **not** transmit analytics, device identifiers, or tracking tokens to any remote servers.

---

### 2. Device Permissions and How They Are Used
TwinSpace only requests permissions essential for core sandboxing and multi-instance app management:

- **Query All Packages (`android.permission.QUERY_ALL_PACKAGES`):**
  - **Purpose:** Used strictly to scan installed applications on your device so you can select which apps to clone into your secondary isolated space.
  - **Data Handling:** Application metadata (package name, app label, and icon) is read directly from your device's Package Manager in-memory and is never uploaded or shared externally.

- **Post Notifications (`android.permission.POST_NOTIFICATIONS`):**
  - **Purpose:** Used to deliver status alerts (such as clone creation progress or active sandbox alerts).

- **Install Shortcuts (`com.android.launcher.permission.INSTALL_SHORTCUT`):**
  - **Purpose:** Allows you to place direct 1-tap launcher shortcuts for cloned apps onto your device's home screen.

---

### 3. Data Isolation and Storage
- All cloned apps, their configurations, credentials, and cache files reside either in your system's hardware sandbox / work profile or private app storage.
- TwinSpace does not inspect the contents of your messages, files, or accounts inside cloned apps.
- Clearing or removing an app clone immediately removes its associated local preferences and configuration.

---

### 4. Third-Party Services
TwinSpace does not include third-party advertising SDKs, tracking libraries, or profiling trackers.

---

### 5. Children's Privacy
TwinSpace is not directed towards children under the age of 13. We do not knowingly collect personal information from children.

---

### 6. Changes to This Privacy Policy
We may periodically update this policy. Any updates will be reflected with a revised "Last updated" date in this document.

---

### 7. Contact Us
If you have any questions or inquiries regarding this Privacy Policy, please contact the TwinSpace support team:
- **Email:** support@twinspace.app
