# SSH Pad - Android Tablet SSH Client

[![Build Status](https://github.com/ssh-pad/ssh-pad/actions/workflows/build.yml/badge.svg)](https://github.com/ssh-pad/ssh-pad/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-34-green.svg)](https://developer.android.com)

**Android Tablet SSH Client with Material 3 Design**

A modern SSH client built specifically for Android tablets, featuring excellent touch experience and keyboard support.

## ✨ Features

### MVP (Week 5-10)
- 🔐 **SSH Connection Management** - Password & Key-based authentication
- 💻 **Terminal Emulator** - Full ANSI escape sequence support (256 colors)
- ⌨️ **Interactive Shell Sessions** - Pseudo-terminal bidirectional streaming
- 📱 **Tablet-Optimized UI** - Material 3, dual-pane layout for tablets
- 🔑 **Key Management** - Android Keystore with biometric authentication
- 🔌 **Background Service** - TCP Keep-Alive, auto-reconnection

### Coming Soon (Week 11+)
- 📁 SFTP File Transfer
- 🔄 Port Forwarding (Local/Remote/Dynamic)
- 📱 Tablet-specific features (Split-screen, Multi-window)
- ☁️ Cloud Sync (Encrypted)
- 🤖 AI Command Completion

## 📱 Screenshots

_Screenshots will be added after Week 6 UI development_

## 🛠️ Tech Stack

- **Language**: Kotlin 1.9.20
- **UI Framework**: Jetpack Compose
- **SSH Library**: Apache MINA sshd 2.11.0
- **Architecture**: Clean Architecture + MVVM
- **Min SDK**: Android 12 (API 31)
- **Target SDK**: Android 14 (API 34)

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK 34

### Build from Source

```bash
# Clone the repository
git clone https://github.com/ssh-pad/ssh-pad.git
cd ssh-pad

# Build debug APK
./gradlew assembleDebug

# Run tests
./gradlew test

# Install on device
./gradlew installDebug
```

### Download

_Alpha release coming Week 10 (2026-05-31)_

## 📖 Documentation

- [Technical Specification](./TECHNICAL_SPEC.md)
- [Project Plan](./PROJECT_PLAN.md)
- [Product Requirements](./docs/PRD-v1.0.md)
- [Contributing Guide](./docs/CONTRIBUTING.md) (Coming soon)
- [Architecture](./docs/ARCHITECTURE.md) (Coming soon)

## 🏛️ Project Structure

```
ssh-pad/
├── app/                          # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sshpad/app/
│   │   │   │   ├── data/        # Data layer (repositories, models)
│   │   │   │   ├── domain/      # Domain layer (use cases)
│   │   │   │   ├── presentation/ # UI layer (ViewModels, Composables)
│   │   │   │   ├── ssh/         # SSH client implementation
│   │   │   │   ├── terminal/    # Terminal emulator
│   │   │   │   └── di/          # Dependency injection
│   │   │   ├── res/             # Android resources
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                # Unit tests
│   │   └── androidTest/         # Instrumented tests
│   └── build.gradle.kts
├── docs/                        # Documentation
├── design/                      # UI/UX designs
├── assets/                      # App assets
├── test/                        # Test scripts
├── build.gradle.kts             # Root build config
├── settings.gradle.kts          # Project settings
└── README.md
```

## 📅 Development Timeline

| Phase | Weeks | Dates | Status |
|-------|-------|-------|--------|
| Requirements | W1-2 | 2026-03-23 ~ 2026-04-05 | ✅ Completed |
| Design | W3-4 | 2026-04-06 ~ 2026-04-19 | 🔄 In Progress |
| **MVP Development** | **W5-10** | **2026-04-20 ~ 2026-05-31** | **⏳ Starting** |
| Feature Enhancement | W11-14 | 2026-06-01 ~ 2026-06-28 | ⏳ Pending |
| Testing | W15-17 | 2026-06-29 ~ 2026-07-19 | ⏳ Pending |
| Release | W18 | 2026-07-20 ~ 2026-07-28 | ⏳ Pending |

## 📊 Current Status

**Week 5 Kickoff** (2026-04-20)

This week we're starting MVP development:
- ✅ GitHub repository setup
- ⏳ Android project skeleton
- ⏳ SSH connection management implementation
- ⏳ Connection UI implementation

## 👥 Team

- **Product Manager**: 陈经理
- **Technical Lead**: 张工
- **UI/UX Designer**: 王设计师
- **Android Developers**: 张工，李工
- **Security Advisor**: External consultant
- **Project Manager**: 司礼监

## 📄 License

```
Copyright 2026 SSH Pad Team

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## 🙏 Acknowledgments

- [Apache MINA sshd](https://mina.apache.org/sshd-project/) - SSH library
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system

---

**Built with ❤️ for Android tablet users**

*Last updated: 2026-04-20 (Week 5 Kickoff)*
