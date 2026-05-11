---
name: update-dependencies
description: Updates the project dependencies
---

# 1. Research new updates
1. Use https://gradle.org to search for Gradle updates, including Gradle plugins
2. Use https://mvnrepository.com to search for dependency updates
3. Analyze `libs.versions.toml` and `gradle-wrapper.properties`
4. Read the comment in `libs.versions.toml` and `gradle-wrapper.properties` and honor the notes taken to
   decide if a dependency should be updated to it's latest version or remain on a certain version for compatibility reasons

# 2. Update dependencies
- Update all dependencies in `libs.versions.toml` and `gradle-wrapper.properties`
- Always prefer stable version of dependencies, if possible
   - Prioritize `rc` over `beta` and `alpha`
- If Gradle fails to resolve newly released versions that are confirmed to exist, use the `--refresh-dependencies` flag (e.g., `./gradlew assembleDebug --refresh-dependencies`)
- Add comments to `libs.versions.toml` and `gradle-wrapper.properties` on the versions you decide not to update with an explanation why (usually due to compatibility issues)

# 3. Verify updates
1. Run `./gradlew assembleDebug` and fix any compilation errors
2. Run all tests with `./gradlew test`. Fix any issues caused by failing tests. Never change the tests
   themself.
3. Verify app start(s)
    1. Start the Pixel_9_Pro_API_36 Android emulator
    2. Build the app with ./gradlew assembleDebug
    3. Install the build APK with adb install
    4. Verify the app is running and doesn't crash
    5. Start the Wear_OS_Small_Round_API_36 Android emulator
    6. Build the app with ./gradlew assembleDebug
    7. Install the build APK with adb install
    8. Verify the app is running and doesn't crash

# 4. Verify that all potential version updates have been addressed
Verify that all versions in `libs.versions.toml` and `gradle-wrapper.properties` that have a newer version available
either are at their latest version or has a comment explaining why it wasn't updated

# 5. Final step
- Summarize all changes in `libs.versions.toml` and `gradle-wrapper.properties` and let the user approve them
- When approved create a commit for the changes with title "Update dependencies"
- Push the changes
