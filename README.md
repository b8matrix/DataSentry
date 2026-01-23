od# DataSentry 🛡️

<div align="center">

**Advanced Network Traffic Analysis & Privacy Protection for Android**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![API](https://img.shields.io/badge/API-29+-brightgreen?style=for-the-badge)](https://android-arsenal.com/api?level=29)

*Monitor, analyze, and protect your mobile network traffic in real-time*

</div>

---

## 📖 Overview

DataSentry is a sophisticated Android application that provides deep packet inspection and network traffic analysis capabilities. Built on Android's VpnService API, it creates a local VPN tunnel to intercept all network traffic, enabling comprehensive monitoring of app communications, data flows, and potential privacy threats.

### Why DataSentry?

In an era where mobile applications constantly communicate with remote servers, users have little visibility into what data is being transmitted. DataSentry bridges this gap by providing:

- **Complete Traffic Visibility**: See every network connection your device makes
- **App-Level Attribution**: Know which applications are sending data
- **Content Classification**: Automatic detection of traffic types (video, images, text, telemetry)
- **Privacy Scoring**: Real-time assessment of your device's privacy health
- **Persistent Logging**: Historical analysis of all network activity

---

## 🏗️ System Architecture

DataSentry follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                         │
│  ┌─────────────────────┐    ┌─────────────────────────────────┐│
│  │   DashboardScreen   │◄───│       DashboardViewModel        ││
│  │   (Jetpack Compose) │    │   (StateFlow, Coroutines)       ││
│  └─────────────────────┘    └─────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                              │
│  ┌─────────────────────┐    ┌─────────────────────────────────┐│
│  │  DataSentryService  │    │       TrafficInspector          ││
│  │    (VpnService)     │───►│   (Packet Analysis Engine)      ││
│  └─────────────────────┘    └─────────────────────────────────┘│
│  ┌─────────────────────┐    ┌─────────────────────────────────┐│
│  │  DemoScenarioEngine │    │        AnalysisResult           ││
│  │ (Traffic Profiling) │    │    (Analysis Data Model)        ││
│  └─────────────────────┘    └─────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                               │
│  ┌─────────────────────┐    ┌─────────────────────────────────┐│
│  │    AppDatabase      │    │       PacketRepository          ││
│  │      (Room)         │◄───│    (Data Access Layer)          ││
│  └─────────────────────┘    └─────────────────────────────────┘│
│  ┌─────────────────────┐    ┌─────────────────────────────────┐│
│  │    PacketEntity     │    │    FlowStats / SuspiciousEvent  ││
│  │   (Traffic Log)     │    │     (Analysis Metrics)          ││
│  └─────────────────────┘    └─────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Core Components

### 1. DataSentryService.kt - VPN Engine

The heart of the application. This service extends `VpnService` to create a local VPN tunnel.

**Key Responsibilities:**
- Establishes VPN interface using `VpnService.Builder`
- Manages foreground service with persistent notification
- Coordinates packet inspection and logging
- Handles service lifecycle (start/stop/revoke)

```kotlin
// VPN Interface Configuration
val builder = Builder()
    .setSession("DataSentry Traffic Monitor")
    .addAddress("10.0.0.2", 32)  // Virtual interface IP
    .setBlocking(false)

vpnInterface = builder.establish()
```

**Service Commands:**
- `ACTION_STOP`: Gracefully terminates VPN monitoring
- Default: Starts VPN and begins traffic analysis

### 2. TrafficInspector.kt - Packet Analysis Engine

Performs deep packet inspection on intercepted traffic.

**Capabilities:**
- IPv4 packet parsing (version, IHL, protocol detection)
- TCP/UDP header extraction (ports, flags)
- DNS query/response parsing (RFC 1035)
- Traffic flow aggregation and statistics

```kotlin
fun inspect(packet: ByteArray, uid: Int) {
    val version = (packet[0].toInt() and 0xFF) ushr 4
    val protocol = packet[9].toInt() and 0xFF  // TCP=6, UDP=17
    val srcIp = extractIpv4(packet, 12)
    val dstIp = extractIpv4(packet, 16)
    // ... analysis continues
}
```

### 3. DemoScenarioEngine.kt - Traffic Profiling

Implements intelligent traffic classification based on destination patterns.

**Detection Heuristics:**
| IP Pattern | Application | Traffic Type |
|------------|-------------|--------------|
| `172.217.*`, `142.250.*` | YouTube | VIDEO_STREAM_4K |
| `104.16.*`, `104.17.*` | Cloudflare CDN | WEB_NAVIGATION |
| `142.251.*`, `74.125.*` | Google Services | BACKGROUND_TELEMETRY |

```kotlin
object DemoScenarioEngine {
    fun getScenarioByIp(destIp: String): AnalysisResult? {
        return when {
            destIp.startsWith("172.217.") -> scenarios["com.google.android.youtube"]
            destIp.startsWith("104.16.") -> scenarios["com.android.chrome"]
            // ... pattern matching
        }
    }
}
```

### 4. AppDatabase.kt - Persistence Layer

Room database for storing all traffic logs and analysis results.

**Entities:**
- `PacketEntity`: Individual packet logs with metadata
- `FlowStats`: Aggregated traffic statistics per app
- `SuspiciousEvent`: Flagged security concerns

```kotlin
@Database(
    entities = [PacketEntity::class, FlowStats::class, SuspiciousEvent::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun packetDao(): PacketDao
    abstract fun flowStatsDao(): FlowStatsDao
    abstract fun suspiciousEventDao(): SuspiciousEventDao
}
```

### 5. DashboardScreen.kt - Real-Time UI

Modern Compose UI with live traffic visualization.

**UI Components:**
- Privacy Health Score with animated radar visualization
- Live traffic list with app attribution
- Content type badges (Video 4K, Image, Text, Telemetry)
- Start/Stop monitoring controls

```kotlin
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val packets by viewModel.packets.collectAsState()
    
    LazyColumn {
        items(packets) { packet ->
            PacketItem(packet)  // Real-time updates via Flow
        }
    }
}
```

---

## 📊 Data Models

### PacketEntity
```kotlin
data class PacketEntity(
    val id: Long,
    val timestamp: Long,
    val sourceIp: String,
    val destIp: String,
    val protocol: String,      // TCP, UDP, ICMP
    val sizeBytes: Int,
    val appName: String,       // YouTube, Chrome, System
    val contentType: String,   // Video 4K, Image, Text
    val isRisk: Boolean
)
```

### AnalysisResult
```kotlin
data class AnalysisResult(
    val packageName: String,
    val trafficType: String,   // VIDEO_STREAM_4K, WEB_NAVIGATION
    val server: String,        // "Google Video Cache (Mountain View, US)"
    val riskScore: Int,        // 0-100
    val insight: String        // Analysis description
)
```

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- Physical Android device (API 29+) - *Emulator VPN has limitations*
- USB debugging enabled
- Kotlin 1.9.22+

### Installation

```bash
# Clone the repository
git clone https://github.com/adityanair2509/DataSentry.git
cd DataSentry

# Open in Android Studio
# File → Open → Select DataSentry folder

# Build and run on connected device
# Click Run ▶️ or use: ./gradlew installDebug
```

### First Launch

1. Grant VPN permission when prompted
2. Tap **"ACTIVATE FIREWALL"** to start monitoring
3. Use your device normally - traffic logs appear in real-time
4. Tap **"DEACTIVATE FIREWALL"** to stop

---

## 🔒 Security & Privacy

DataSentry operates entirely **on-device**:
- No data is transmitted to external servers
- All analysis happens locally
- Traffic logs are stored in private app storage
- VPN tunnel terminates on the device itself

---

## 📁 Project Structure

```
app/src/main/java/com/datasentry/app/
├── MainActivity.kt                 # Entry point, VPN permission handling
├── DataSentryService.kt           # Core VPN service implementation
│
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt         # Room database configuration
│   │   ├── entity/
│   │   │   └── PacketEntity.kt    # Traffic log data model
│   │   └── dao/
│   │       └── PacketDao.kt       # Database operations
│   ├── model/
│   │   ├── FlowStats.kt           # Traffic statistics
│   │   ├── SuspiciousEvent.kt     # Security alerts
│   │   └── RiskLevel.kt           # Risk classification enum
│   └── repository/
│       └── PacketRepository.kt    # Data access abstraction
│
├── demo/
│   ├── AnalysisResult.kt          # Analysis result model
│   └── DemoScenarioEngine.kt      # Traffic profiling engine
│
├── inspector/
│   └── TrafficInspector.kt        # Deep packet inspection
│
└── presentation/
    └── dashboard/
        ├── DashboardScreen.kt     # Compose UI
        └── DashboardViewModel.kt  # UI state management
```

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9.22 |
| UI Framework | Jetpack Compose (Material3) |
| Architecture | MVVM + Clean Architecture |
| Database | Room 2.6.1 |
| Async | Coroutines + Flow |
| Network | Android VpnService API |
| Build | Gradle 8.2.2 (Kotlin DSL) |

---

## 👥 Team

**Overwatchers**

- **Aditya Nair** - Lead Developer - [@adityanair2509](https://github.com/adityanair2509)
- **Hridayshri Dave** - UI/UX Design - [@hridaydave25](https://github.com/hridaydave25)
- **Tanay Sagar** - Backend Architecture - [@tanaysagar](https://github.com/tanaysagar)
- **Bhagirath** - Testing & Documentation - [@b8matrix](https://github.com/b8matrix)

---

## 📄 License

This project is developed for educational and research purposes. 

---

<div align="center">

**Built with ❤️ by Team Overwatchers**

</div>
