# Architecture & Modules

Feature-based multi-module architecture separating mobile, wear, and shared infrastructure.

## Module Layout & Seams

- `mobile/app` & `wear/app`: Application entry points and lifecycle listeners.
- `mobile:data:item`: Remote Todoist task execution via Ktor (`TasksEndpoint`).
- `mobile:data:items`: Local Room database for mobile item list caching.
- `wear:data:items`: Local Room database for Wear OS item list; communicates with mobile via Wearable `DataClient`.
- `mobile:common:design-system` & `wear:common:design-system`: Platform-specific Compose styling and themes.
- `common:ui`: Cross-platform shared UI components (e.g., `LoadingScreen`).
- `gradle/build-logic`: Custom convention plugins for consistent module build configuration.

## Guidelines

- Add new features under `mobile/feature/<name>` or `wear/feature/<name>`.
- Place data sources and repositories in the appropriate `mobile/data/<name>` or `wear/data/<name>` module.
- Reusable UI shared across form factors belongs in `common:ui`; platform-specific UI foundations belong in `mobile:common:design-system` or `wear:common:design-system`.
- Apply convention plugins from `gradle/build-logic` in `build.gradle.kts` (e.g., `alias(libs.plugins.convention.android.library)`).
- Manage dependencies and versions in `gradle/libs.versions.toml`.
- Register all modules in `settings.gradle.kts`.
