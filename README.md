# WearTo

![GitHub header](https://github.com/user-attachments/assets/40d7f93a-3dbe-41c2-8f49-ef418c95c774)

_WearTo lets you add items to Todoist with minimal effort using your wearable. Just like it should be._

[![google-play-badge](https://user-images.githubusercontent.com/1917608/36116679-614bba4e-1037-11e8-891f-9bd7bca10e97.png)
](https://play.google.com/store/apps/details?id=se.yverling.wearto)

## Overview

WearTo is a companion app for Todoist that allows you to quickly add tasks from your Wear OS device. The project consists of both a mobile app (for authentication and management) and a Wear OS app (
for quick task creation), built with modern Android development practices using Kotlin, Jetpack Compose, and a modular architecture.

## Features

- **Wear OS Integration**: Add tasks to Todoist directly from your smartwatch
- **Mobile Companion**: Authenticate with Todoist and manage settings on your phone
- **Offline Support**: Create tasks offline and sync when connected
- **Data Sync**: Seamless data synchronization between mobile and wear devices
- **Import/Export**: Backup and restore your tasks via CSV files
- **Modern UI**: Built with Jetpack Compose for both mobile and Wear OS

## Prerequisites

Before you begin, ensure you have the following installed:

- **Android Studio**: Electric Eel (2022.1.1) or later
- **JDK**: 11 or higher
- **Android SDK**: API level 31 (minimum) to 36 (target)
- **Kotlin**: 2.2.10+
- **Git**: For version control

### Hardware Requirements

- **For Mobile Development**: Any Android device or emulator running API 31+
- **For Wear OS Development**: Wear OS device or Wear OS emulator (API 31+)

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/erikryverling/wearto.git
cd wearto
```

### 2. Project Setup

The project uses Gradle with version catalogs and custom convention plugins for consistent configuration across modules.

#### Initial Build

```bash
./gradlew build
```

This will:

- Download all dependencies
- Build both mobile and wear applications
- Run code quality checks
- Execute unit tests

### 3. Configuration

#### Local Properties

Create a `local.properties` file in the root directory:

```properties
sdk.dir=/path/to/your/Android/Sdk
```

#### Todoist API Integration

The app integrates with Todoist's API. For development:

1. Register your app with Todoist's developer portal
2. Configure the OAuth redirect URLs
3. Update the API endpoints in the network modules

### 4. Running the Apps

#### Mobile App

```bash
./gradlew :mobile:app:installDebug
```

#### Wear OS App

```bash
./gradlew :wear:app:installDebug
```

#### Running Both Apps Simultaneously

```bash
./gradlew installDebug
```

## Project Architecture

WearTo follows a modular architecture with clear separation of concerns:

### Module Structure

```
wearto/
├── buildSrc/                    # Build configuration and versions
├── gradle/build-logic/          # Convention plugins for consistent builds
├── common/                      # Shared components
│   └── ui/                     # Common UI components
├── mobile/                      # Mobile app modules
│   ├── app/                    # Main mobile application
│   ├── common/                 # Mobile-specific common code
│   │   ├── design-system/      # Mobile design system
│   │   └── network/            # Network layer
│   ├── data/                   # Data layer modules
│   │   ├── item/               # Single item data
│   │   ├── items/              # Items collection data
│   │   ├── settings/           # Settings data
│   │   └── token/              # Authentication tokens
│   └── feature/                # Feature modules
│       ├── items/              # Tasks/items management
│       ├── login/              # Authentication
│       └── settings/           # App settings
├── wear/                        # Wear OS app modules
│   ├── app/                    # Main wear application
│   ├── common/                 # Wear-specific common code
│   │   └── design-system/      # Wear design system
│   ├── data/                   # Wear data layer
│   │   └── items/              # Items data for wear
│   └── feature/                # Wear features
│       └── items/              # Task management on wear
└── test/                        # Test utilities
    └── utils/                  # Common test utilities
```

### Architecture Patterns

- **MVVM**: Model-View-ViewModel pattern with Compose
- **Clean Architecture**: Clear separation between data, domain, and presentation layers
- **Dependency Injection**: Hilt for dependency management
- **Repository Pattern**: Data layer abstraction
- **Single Source of Truth**: State management with StateFlow and Compose State

### Key Technologies

- **UI Framework**: Jetpack Compose (Mobile) + Wear Compose (Wear OS)
- **Navigation**: Navigation Compose with type-safe routing
- **Networking**: Ktor client with kotlinx.serialization
- **Database**: Room with Kotlin coroutines
- **Data Storage**: DataStore for preferences and Protocol Buffers
- **Dependency Injection**: Hilt
- **Build System**: Gradle with Kotlin DSL and convention plugins
- **Testing**: JUnit 5, MockK, Kotest, Turbine

## Development Workflow

### Code Style

The project follows Kotlin official code style. Configure Android Studio:

1. Go to `Settings` → `Editor` → `Code Style` → `Kotlin`
2. Click `Set from...` → `Predefined style` → `Kotlin style guide`

### Git Hooks

The project includes pre-push hooks to ensure code quality:

```bash
# Install git hooks
cp hooks/pre-push .git/hooks/
chmod +x .git/hooks/pre-push
```

### Testing

#### Running Unit Tests

```bash
# Run all tests
./gradlew test

# Run specific module tests
./gradlew :mobile:feature:items:test
```

#### Running UI Tests

```bash
./gradlew connectedAndroidTest
```

### Build Variants

The project supports different build configurations:

- **Debug**: Development builds with logging and debugging enabled
- **Release**: Production builds with optimization and obfuscation

### Continuous Integration

The project uses GitHub Actions for CI/CD:

- Automated testing on pull requests
- APK generation for releases
- Play Store deployment via Fastlane

## Deployment

### Play Store Deployment

The project uses Fastlane for automated deployment:

```bash
# Install dependencies
bundle install

# Deploy to Play Store (requires setup)
bundle exec fastlane deploy
```

### Manual Release Build

```bash
# Generate release APKs
./gradlew assembleRelease

# APKs will be generated in:
# mobile/app/build/outputs/apk/release/
# wear/app/build/outputs/apk/release/
```

## Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes following the project's coding standards
4. Add tests for new functionality
5. Ensure all tests pass: `./gradlew test`
6. Submit a pull request

### Code Review Process

- All changes require pull request review
- Automated checks must pass (build, tests, linting)
- Manual testing on both mobile and wear devices when applicable

## Troubleshooting

### Common Issues

#### Build Failures

```bash
# Clean and rebuild
./gradlew clean build
```

#### Dependency Issues

```bash
# Refresh dependencies
./gradlew --refresh-dependencies build
```

#### Wear OS Connection Issues

1. Ensure both devices are on the same WiFi network
2. Enable Developer options on Wear OS device
3. Check ADB connection: `adb devices`

### Getting Help

- Check existing [GitHub Issues](https://github.com/erikryverling/wearto/issues)
- Create a new issue with detailed reproduction steps
- Include logs from `./gradlew build --info` if relevant

## License and Acknowledgments

This project is gratefully using the following third-party libraries:

- [Kotlin](https://github.com/JetBrains/kotlin/tree/master/license)
- [Timber](https://github.com/JakeWharton/timber/blob/trunk/LICENSE.txt)
- [ktor](https://github.com/ktorio/ktor/blob/main/LICENSE)
- [JUnit](https://github.com/junit-team/junit5/blob/main/LICENSE.md)
- [mockk](https://github.com/mockk/mockk/blob/master/LICENSE)
- [kotest](https://github.com/kotest/kotest/blob/master/LICENSE)
- [turbine](https://github.com/cashapp/turbine/blob/trunk/LICENSE.txt)

## Privacy Policy

See our [Privacy Policy](https://www.github.com/erikryverling/wearto/blob/master/privacy-policy.md) for information about data handling and user privacy.

---
Made with ❤️ for the Android and Wear OS community
