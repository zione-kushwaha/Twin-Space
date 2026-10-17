# TwinSpace Cloner (Dual Apps & Standalone App Engine)

<p align="center">
  <img src="app/src/main/res/drawable/ic_twinspace_brand.xml" width="96" height="96" alt="TwinSpace Logo" />
</p>

<p align="center">
  <b>Multi-Account Parallel Space & Standalone Android Application Cloner</b><br>
  <i>Run dual WhatsApp, Telegram, TikTok, Instagram & gaming accounts simultaneously with 100% on-device isolation.</i>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+-3DDC84?style=flat&logo=android" alt="Platform" />
  <img src="https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?style=flat&logo=kotlin" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4?style=flat&logo=jetpackcompose" alt="Compose" />
  <img src="https://img.shields.io/badge/Architecture-Clean_Architecture_(MVI)-FF6F00?style=flat" alt="Architecture" />
  <img src="https://img.shields.io/badge/Tests-14_Suites_Passing-brightgreen?style=flat" alt="Tests" />
  <img src="https://img.shields.io/badge/Privacy-100%25_On--Device-success?style=flat" alt="Privacy" />
</p>

---

## 🌟 Key Features

### 1. Dual Space Sandboxing
- **100% Account Isolation**: Run second instances of social & messaging apps (WhatsApp, Telegram, Instagram, Messenger, TikTok) with completely separate SQLite databases, preferences, and session tokens.
- **Multi-Instance Badging**: Clear dynamic badges (`#1`, `#2`, `#3`) for multi-account identification.
- **Direct Launching**: Instant parallel execution with hardware-accelerated rendering.

### 2. Standalone System APK Generator
- **Binary AXML Manifest Rewriter**: Rewrites binary `AndroidManifest.xml` chunk pools on-the-fly, renaming package IDs and updating `ContentProvider` authorities to eliminate package collisions.
- **Dual v1 + v2 APK Signing**: Generates RSA-2048 keys and self-signed X.509 certificates to re-sign cloned APKs with SHA-256 digests.
- **Launcher Integration**: Standalone clones install directly into the device's main system app drawer and work independently like any regular app.

### 3. One-Tap Home Screen Shortcuts
- Pin direct shortcuts of any cloned app onto the Android home screen via `ShortcutManagerCompat` with badged app icons. Launch clones directly without opening TwinSpace first.

### 4. Privacy Vault & Calculator Disguise
- **App Lock**: Protect sensitive clone sessions with a master 4-digit PIN.
- **Stealth Calculator Disguise**: Masks TwinSpace as a fully functional math calculator. Entering the secret PIN followed by `=` unlocks the private clone vault.
- **Stealth Notifications**: Mask sender names and notification content for private accounts.

### 5. Storage & Cache Optimizer
- **Disk Usage Breakdown**: Monitor storage occupied by standalone APKs and volatile cache files.
- **1-Tap Cache Cleaner**: Free temporary extraction data and junk files safely in the background.

### 6. Zero Data Collection Certified
- **100% Offline Processing**: No remote telemetry, no external servers, and zero personal data tracking.

---

## 🏗️ Clean Architecture

The codebase strictly separates concerns into **Domain**, **Data**, and **Presentation** layers:

```
com.twinspace.cloner/
├── domain/                                      # Domain Layer (Zero Android framework coupling)
│   ├── model/
│   │   ├── ClonedApk.kt                         # Standalone APK entity & formatters
│   │   ├── CloneTaskStatus.kt                   # Sealed pipeline status states
│   │   ├── StorageUsageSummary.kt               # Storage footprint calculations
│   │   └── SecuritySettings.kt                  # Security lock & PIN domain model
│   ├── repository/
│   │   ├── StandaloneCloneRepository.kt         # APK modding & build contract
│   │   ├── VirtualCloneRepository.kt            # Sandbox / Work profile contract
│   │   ├── ShortcutRepository.kt                # Home screen shortcut pinning contract
│   │   ├── SecurityRepository.kt                # Security preferences contract
│   │   └── StorageOptimizerRepository.kt        # Disk analyzer & cache cleaner contract
│   └── usecase/                                 # Single-responsibility business use cases
│       ├── BuildStandaloneApkUseCase.kt
│       ├── GetSavedClonedApksUseCase.kt
│       ├── InstallApkUseCase.kt
│       ├── ShareApkUseCase.kt
│       ├── DeleteApkUseCase.kt
│       ├── PinShortcutUseCase.kt
│       ├── SecurityUseCases.kt
│       └── StorageOptimizerUseCases.kt
│
├── data/                                        # Data Layer (Engines & Repositories)
│   ├── repository/                              # Concrete repository implementations
│   └── core/
│       ├── StandaloneApkCloner.kt               # APK extraction, AXML modding & signing
│       ├── AxmlEditor.kt                        # Chunk-based binary AndroidManifest parser
│       └── DualSpaceManager.kt                  # Virtual dual-space / sandbox controller
│
└── ui/                                          # Presentation Layer (Jetpack Compose & MVI)
    ├── viewmodel/
    │   └── HomeViewModel.kt                     # MVI ViewModel (HomeUiState & HomeUiEvent)
    ├── components/
    │   ├── PopularAppsBanner.kt                 # 1-tap quick clone carousel
    │   ├── StandaloneApksDashboard.kt           # Glassmorphic standalone APK manager
    │   ├── BuildPipelineModal.kt                # 4-stage build progress dialog
    │   ├── StorageOptimizerModal.kt             # Storage breakdown & cleaner dialog
    │   └── PrivacyTrustCenterDialog.kt          # Offline certification modal
    └── screens/
        ├── HomeScreen.kt
        ├── CloneOptionsDialog.kt
        ├── AppLockScreen.kt
        ├── CalculatorDisguiseScreen.kt
        ├── WebTwinScreen.kt
        ├── SettingsScreen.kt
        └── OnboardingScreen.kt
```

---

## 🧪 Automated Test Suites (14 Suites, 100% Passing)

Run the full automated test suite:
```bash
./gradlew testDebugUnitTest
```

| Test Suite | Coverage |
| :--- | :--- |
| `ApkSigningTest` | RSA-2048 key generation, X.509 cert, ZIP signature blocks |
| `AxmlEditorTest` | String chunk encoding, UTF-8 / UTF-16, authority replacement |
| `AxmlAdvancedEdgeCasesTest` | Multiple providers, length mismatches, pool relocation |
| `ManifestAuthorityReplacementTest` | FileProvider collision prevention |
| `ClonedApkDomainModelTest` | Filename parsing, clone numbering, size & date formatters |
| `StandaloneCloneUseCaseTest` | Domain use cases: Fetch, Build, Install, Share, Delete |
| `ShortcutUseCaseTest` | Shortcut pinning validation and device support |
| `SecurityDomainModelTest` | PIN security verification and lockout logic |
| `StorageOptimizerDomainModelTest` | Storage calculations and byte formatting |
| `StorageOptimizerUseCaseTest` | Cache freeing and real-time usage monitoring |
| `HomeViewModelTest` | MVI state machine, event handling, tab filters, clone flow |
| `StandaloneApkClonerFileLogicTest` | Next available clone numbering (#1, #2, #3) |
| `CloneStateLogicTest` | Multi-instance state synchronization |
| `CloneRecordLogicTest` | Persistence and record integrity |

---

## 🚀 Building & Running

### Prerequisites
- JDK 21
- Android Studio Ladybug or newer
- Android SDK Platform 34

### Build Debug APK
```bash
./gradlew assembleDebug
```

### Build Production Release APK (R8 Minified & Signed)
```bash
./gradlew assembleRelease
```
Output artifact: `app/build/outputs/apk/release/app-release.apk`

---

## 🔒 Security & Privacy

TwinSpace is built with privacy as its foundation:
- **Zero Cloud Servers**: All processing occurs locally on the device.
- **Data Isolation**: Cloned apps operate in independent sandboxes with separated file systems.
- **Open Standards**: Standard Android `FileProvider` and `ShortcutManager` integrations.

---

## 📄 License
This project is licensed under the Apache 2.0 License.
