# Protect the authentication token with AEAD

The mobile app will replace its custom AES-CBC token encryption with AndroidX DataStore's `AeadSerializer`, backed by a Tink keyset protected by a new unauthenticated Android Keystore AES-GCM master key. The token must remain available to background network requests, and authenticated encryption provides integrity protection that the current format lacks.

Existing sessions will be preserved through an indefinitely supported, read-only legacy path. A narrowly scoped serializer wrapper will recognize only the existing binary framing and decrypt it with the existing `WearToSecretKey`; a DataStore migration will then atomically rewrite the token through `AeadSerializer` before exposing it and delete the legacy key after the rewrite commits. All new writes use AEAD, and downgraded app builds are not expected to read the new format.

Unreadable token data, including corruption, tampering, unavailable keys, and unrestorable encrypted backups, will resolve to an absent token so the app can return to sign-in. The token DataStore file and Tink keyset preferences will be excluded from Android backup while unrelated settings and Room data remain eligible. This change accepts the prerelease DataStore `1.3.0-alpha10` dependency because `datastore-tink-android` is not available in the current stable DataStore release.
