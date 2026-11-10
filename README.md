# CleanBot AI — Single-Robot Office Cleaning Management System

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84.svg?style=flat&logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-M3-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Repository](https://img.shields.io/badge/GitHub-Derartu%2Fcleaning__robot__controller-181717.svg?style=flat&logo=github)](https://github.com/Derartu/cleaning_robot_controller)

**CleanBot AI** is an autonomous single-robot office cleaning management and simulation system built with modern native Android (Kotlin, Jetpack Compose, Material 3). It provides real-time SLAM navigation visualization, automated task priority queuing, simulated optical spectrometry dirt detection, an 11-step full container recovery workflow, battery charging cycles, and a modular Hardware Abstraction Layer (HAL).

---

## 🚀 Key Features

* **Single-Robot Architecture:** Strictly manages 1 autonomous robot unit (`CleanBot Prime CB-X100`) and 1 service station (`Dock Bay STN-01`). Singleton constraint prevents duplicate robot registration.
* **Interactive 2D Office Map:** Real-time Jetpack Compose Canvas rendering rooms, corridors, workstations, meeting tables, obstacles, and restricted hazard zones. Features an active $360^\circ$ rotating LiDAR sweep beam and breadcrumb cleaned trails.
* **Autonomous Cleaning Scopes:** Supports *Floor Only*, *Table Only*, *Desk Only*, *Combined*, and *Complete Room* cleaning cycles with priority queue ordering (*Urgent*, *High*, *Medium*, *Low*).
* **11-Step Full Container Recovery Workflow:** Safely halts motors on full bin $\rightarrow$ saves room progress $\rightarrow$ autonomous dock return $\rightarrow$ sets `Waiting for Operator` status $\rightarrow$ verifies operator servicing $\rightarrow$ **auto-resumes unfinished task from saved checkpoint**.
* **Battery & Power Management:** Dynamic discharge tracking, low-battery auto-return ($\le 20\%$), fast charging at $120\text{W}$, and auto-resumption when recharged.
* **Simulated Dirt & Spill Detection:** Identifies debris (*Dust, Sand, Paper, Food Crumbs, Liquid Spills*) with classifications (`DRY`, `WET`, `MIXED`, `UNKNOWN`), confidence ratings, and recommended cleaning protocols.
* **Office & Zone Management (Full CRUD):** Create, edit, and delete office rooms, furniture, and virtual wall hazard zones.
* **Role-Based Access Control (RBAC):** One-tap role switching between **Administrator** (configuration, virtual walls, settings) and **Operator** (telemetry, dispatching, servicing).
* **Hardware Abstraction Layer (HAL):** Modular `RobotDriver` interface powering `SimulatedRobotDriver` (live on-device) and `PhysicalRobotDriver` (MQTT broker stub for physical hardware).
* **Simulation Engine & Diagnostic Triggers:** $1\times, 2\times, 5\times$ speed control with diagnostic buttons (*Force 100% Full*, *Force Low Bat*, *+ Obstacle*, *+ Spill*).
* **Audit Reports & Analytics:** Dynamic completion rate metrics, surface area cleaned ($\text{m}^2$), and exportable CSV audit logs.

---

## 🛠️ Technology Stack

* **Language:** Kotlin
* **UI Framework:** Jetpack Compose with Material Design 3 (M3)
* **Architecture:** MVVM (Model-View-ViewModel) with StateFlow and Repository Pattern
* **Concurrency:** Kotlin Coroutines & Asynchronous Flows
* **Build System:** Gradle (Kotlin DSL - `.gradle.kts`) with Version Catalog (`libs.versions.toml`)
* **Testing:** JUnit4, Robolectric for JVM integration testing

---

## 📂 Project Structure

```
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── cleanbot/
│   │   │   │   │   ├── data/
│   │   │   │   │   │   └── CleanBotRepository.kt     # Single source of truth & simulation engine
│   │   │   │   │   ├── hal/
│   │   │   │   │   │   ├── RobotDriver.kt            # Hardware Abstraction Interface
│   │   │   │   │   │   ├── SimulatedRobotDriver.kt   # Kinematics & sensor simulation
│   │   │   │   │   │   └── PhysicalRobotDriver.kt    # MQTT/WebSocket hardware stub
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── RobotModels.kt            # RobotRecord, Tasks, Telemetry
│   │   │   │   │   │   ├── OfficeModels.kt           # Rooms, Furniture, Zones, Dirt
│   │   │   │   │   │   ├── StationModels.kt          # Service Station & Docking Events
│   │   │   │   │   │   ├── AlertModels.kt            # Notifications & Severity
│   │   │   │   │   │   └── UserRole.kt               # RBAC Permissions
│   │   │   │   │   └── ui/
│   │   │   │   │       ├── MainAppScaffold.kt        # Drawer, TopBar, BottomNav routing
│   │   │   │   │       ├── components/
│   │   │   │   │       │   ├── OfficeMapCanvas.kt    # Custom 2D interactive canvas
│   │   │   │   │       │   ├── SimulationBar.kt      # Play/Pause, speed, force buttons
│   │   │   │   │       │   └── CommonUi.kt           # Badges, meters, stat cards
│   │   │   │   │       └── screens/                  # 10 dedicated management screens
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Color.kt                      # Industrial IoT Dark Navy Palette
│   │   │   │       └── Theme.kt
│   │   │   └── res/                                  # Adaptive icons, mipmaps, strings
│   │   └── test/                                     # Automated workflow unit tests
│   └── build.gradle.kts
└── settings.gradle.kts
```

---

## 📦 Building and Running

### 1. Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Install on Connected Android Device (via ADB)
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 3. Run Unit & Robolectric Tests
```bash
./gradlew testDebugUnitTest
```

---

## 👤 Author & Repository
* **Repository:** [Derartu/cleaning_robot_controller](https://github.com/Derartu/cleaning_robot_controller)
* **Maintainer:** Derartu (`derartukebeba70@gmail.com`)
