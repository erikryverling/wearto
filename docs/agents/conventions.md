# Development Conventions

Technology stack and implementation conventions for WearTo.

## Storage Strategy

- **Item Lists:** Room database for local caching in `mobile:data:items` and `wear:data:items`.
- **Auth Token:** AndroidX DataStore (`TokenDataStore`) storing a JSON-serialized nullable token string encrypted with AES-CBC (`CryptoManager`).
- **Project Settings:** AndroidX DataStore (`ProjectDataStore`) serialized via Protobuf (`project.proto`).

## Implementation Stack

- **Dependency Injection:** Hilt (`@HiltViewModel`, `@Inject`).
- **Networking:** Ktor client with ContentNegotiation and Kotlinx Serialization on mobile (`mobile:common:network`). Wear communicates with mobile over Google Play Services Wearable `DataClient`.
- **UI:** Jetpack Compose (Material 3 for mobile; Wear Compose and Horologist for Wear OS).
- **State Management:** ViewModels with observable UI state expose `StateFlow`. Command-only ViewModels execute suspend functions directly without holding state.
- **Logging:** Timber (`Timber.d(...)`, `Timber.e(...)`).
