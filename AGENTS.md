# WearTo Project Instructions

WearTo is a multi-module Android application designed to let users add items to Todoist with minimal effort using their wearables (Wear OS).

## Project Overview

*   **Main Technologies:** Kotlin, Jetpack Compose (Material 3 for Mobile, Wear Compose for Wear OS), Hilt (DI), Ktor (Networking), Room & DataStore (Local Storage), Protobuf.
*   **Architecture:** Feature-based multi-module architecture.
    *   `mobile/app` & `wear/app`: Main application modules.
    *   `feature/*`: Feature-specific UI and ViewModels.
    *   `data/*`: Data layer (repositories, local/remote data sources).
    *   `common/*`: Shared UI components and infrastructure.
    *   `gradle/build-logic`: Custom convention plugins for consistent build configuration across modules.

## Building and Running

### Key Commands

*   **Build & Verify:** `./bin/build-and-verify` (Runs `assembleDebug`, `lint`, and `testDebugUnitTest` via Fastlane).
*   **Deploy Mobile:** `./bin/build-and-deploy-mobile` (Builds and uploads to Play Store).
*   **Deploy Wear:** `./bin/build-and-deploy-wear` (Builds and uploads to Play Store).
*   **Clean:** `./gradlew clean`
*   **Assemble Debug:** `./gradlew assembleDebug`
*   **Run Unit Tests:** `./gradlew test`
*   **Lint:** `./gradlew lint`

### Prerequisites

*   Java 17
*   Android Studio Otter | 2025.2.1 Patch 1 or higher

## Development Conventions

### Architecture & Style

*   **DI:** Always use Hilt for dependency injection.
*   **Networking:** Use Ktor for all network requests.
*   **UI:** Use Jetpack Compose. Follow the `common:design-system` patterns.
*   **State Management:** Use ViewModels with StateFlow/Flow.
*   **Concurrency:** Use Kotlin Coroutines and Flow.
*   **Logging:** Use Timber for logging.
*   **Storage:** Room for item lists; DataStore + Protobuf for token and settings

### Module Guidelines

*   When adding a new feature, create it under `mobile/feature/` or `wear/feature/`.
*   Keep data logic within `data/` modules.
*   Shared UI components should go into `common:ui` or specific `design-system` modules.
*   Use `gradle/libs.versions.toml` for dependency management.
*   Apply convention plugins from `gradle/build-logic` in `build.gradle.kts` files (e.g., `alias(libs.plugins.convention.android.library)`).

### Testing Practices

*   **Frameworks:** JUnit 5, MockK, Kotest (Assertions), Turbine (Flow testing).
*   Write unit tests for ViewModels and Repositories.
*   Place tests in the `src/test/` directory of the respective module.

## Key Files

*   `gradle/libs.versions.toml`: Centralized dependency and version management.
*   `settings.gradle.kts`: Project module configuration.
*   `fastlane/Fastfile`: Automation lanes for testing and deployment.
*   `gradle/build-logic/`: Custom Gradle convention plugins.
