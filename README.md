# DataSentry 🛡️

**Privacy-First Network Monitoring for Android**

DataSentry is a hackathon project that visualizes mobile network traffic in real-time, providing users with insights into what data their apps are accessing and sharing.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-29%2B-brightgreen.svg)](https://android-arsenal.com/api?level=29)

---

## 🎯 Features

- **🔐 VPN-Based Monitoring**: Leverages Android's VpnService API to intercept network traffic
- **📊 Real-Time Dashboard**: Live traffic visualization with privacy score calculation
- **🏷️ Smart Categorization**: Heuristic engine classifies traffic (Social, Analytics, Video, etc.)
- **💾 Persistent Logging**: Room database stores all intercepted packets
- **🎨 Modern UI**: Material3 design with dark theme and smooth animations
- **🔄 Full Lifecycle Control**: Start/Stop VPN with proper service management

---

## 📱 Screenshots

*Coming soon after demo recording*

---

## 🏗️ Architecture

**Clean Architecture** with MVVM pattern:

```
📦 DataSentry
├── 🎨 Presentation Layer
│   ├── DashboardScreen (Compose UI)
│   └── DashboardViewModel (State management)
├── 💼 Domain Layer
│   ├── DataSentryService (VPN service)
│   ├── DnsPacket (DNS parser)
│   └── TrafficSimulator (Demo data generator)
└── 💾 Data Layer
    ├── AppDatabase (Room)
    ├── PacketEntity
    ├── PacketDao
    └── PacketRepository
```

**Tech Stack**:
- Kotlin
- Jetpack Compose (Material3)
- Room Database (2.6.1)
- Coroutines & Flow
- Android VpnService API

---

## 🚀 Getting Started

### Prerequisites

- Android Studio (Arctic Fox or later)
- Physical Android device (API 29+)
- USB debugging enabled
- VPN permission granted during first launch

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/adityanair2509/DataSentry.git
   cd DataSentry
   ```

2. **Open in Android Studio**
   - File → Open → Select `DataSentry` folder
   - Wait for Gradle sync (🐘 icon)

3. **Run on device**
   - Connect phone via USB
   - Click **Run** ▶️
   - Accept VPN permission when prompted

---

## 🎮 Usage

1. **Launch DataSentry** on your device
2. **Activate Firewall**: Tap "ACTIVATE FIREWALL" button
3. **Grant Permission**: Accept the VPN connection request
4. **Monitor Traffic**: Watch real-time logs appear in the dashboard
5. **Deactivate**: Tap "DEACTIVATE FIREWALL" to stop monitoring

---

## ⚙️ Configuration

### Enable Traffic Simulation (Recommended for Demo)

For hackathon demos, use simulated traffic to ensure stable presentation:

1. Open `DashboardViewModel.kt`
2. Uncomment `simulator.startSimulation()` in `startSimulation()`
3. Rebuild the app

This provides realistic traffic visualization without interfering with actual internet connectivity.

---

## 🛠️ Development

### Project Structure

```
app/src/main/java/com/datasentry/app/
├── MainActivity.kt                    # Entry point & VPN permission
├── DataSentryService.kt              # VPN service (ACTION_STOP lifecycle)
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt           # Room database
│   │   ├── entity/PacketEntity.kt   # Data model
│   │   └── dao/PacketDao.kt         # Database operations
│   ├── repository/PacketRepository.kt
│   └── remote/DnsPacket.kt          # DNS query parser
├── domain/model/TrafficSimulator.kt  # Fake traffic generator
└── presentation/dashboard/
    ├── DashboardScreen.kt           # Compose UI
    └── DashboardViewModel.kt        # ViewModel
```

### Key Implementation Details

**VPN Service Lifecycle**:
- Uses `ACTION_STOP` intent pattern for proper shutdown
- Service calls `stopSelf()` internally (Android VPN requirement)
- Foreground notification with persistent icon

**DNS Packet Parsing**:
- Implements RFC 1035 DNS query parsing
- Extracts hostname from UDP payloads
- *Note*: Response forwarding not implemented (hackathon scope)

---

## 🐛 Known Issues & Limitations

| Issue | Status | Workaround |
|-------|--------|------------|
| DNS responses not forwarded | ⚠️ Known | Use TrafficSimulator for demo |
| Internet blocked when VPN active | ⚠️ Expected | Disable real DNS routing |
| Private DNS (DoH/DoT) bypasses interception | ⚠️ By design | Disable Private DNS in phone settings |

---

## 🧪 Testing

**Manual Test Checklist**:
- [x] VPN starts on "ACTIVATE FIREWALL"
- [x] VPN key icon appears in status bar
- [x] VPN stops on "DEACTIVATE FIREWALL"
- [x] Key icon disappears after stop
- [x] Database persists logs across app restarts
- [x] Clear logs button works
- [x] Privacy score updates correctly

---

## 📝 Troubleshooting

### "VPN won't stop"
- Force stop app: Settings → Apps → DataSentry → Force Stop
- Uninstall and reinstall

### "Gradle sync failed" 
- Clean project: Build → Clean Project
- Invalidate caches: File → Invalidate Caches / Restart

### "Resource linking failed"
- Check `themes.xml` uses `Theme.MaterialComponents.DayNight.NoActionBar`
- Verify `build.gradle.kts` includes Material dependencies

---

## 🤝 Contributing

This is a hackathon project and not actively maintained. Feel free to fork and extend for your own purposes!

---

## 📄 License

This project is created for educational/hackathon purposes. Use at your own discretion.

---

## 👨‍💻 Author

**Aditya Nair**  
GitHub: [@adityanair2509](https://github.com/adityanair2509)
**Tanay Sagar**
**Hridayshri Dave**


---

## 🙏 Acknowledgments

- Android VpnService documentation
- Jetpack Compose Material3 guidelines
- Room persistence library
- Hackathon mentors and teammates

---

## 📊 Project Stats

- **Lines of Code**: ~2,500
- **Development Time**: Hackathon sprint (2 days)
- **Commits**: 15+
- **Files**: 25+

---

**Built with ❤️ From Overwatchers**

