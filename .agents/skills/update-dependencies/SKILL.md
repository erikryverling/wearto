---
name: update-dependencies
description: Updates the project dependencies
---

# Research new updates
1. Use https://gradle.org to search for Gradle updates, including Gradle plugins
2. Use https://mvnrepository.com to search for dependency updates
3. Analyze libs.versions.toml and gradle-wrapper.properties
4. Read the comment in libs.versions.toml and gradle-wrapper.properties and honor the notes taken do
   decide if a dependency should be upgraded to the latest version or remain on a certain version due to compability reasons

# Update dependencies
- Update all dependencies in libs.versions.toml and gradle-wrapper.properties
- Always prefer stable version of dependencies, if possible
- If Gradle fails to resolve newly released versions that are confirmed to exist, use the `--refresh-dependencies` flag (e.g., `./gradlew assembleDebug --refresh-dependencies`)
- Add comments to libs.versions.toml on the versions you decide not to update with an explanation why

# Verify upgrades
1. Run ./gradlew assembleDebug and fix any compilation errors
2. Run all tests with ./gradlew test. Fix any issues caused by failing tests. Never change the tests
   themself.
3. Verify app start(s)
    2. Start the Pixel_9_Pro_API_36 Android emulator
    3. Build the app with ./gradlew assembleDebug
    4. Install the build APK with adb install
    5. Verify the app is running and doesn't crash
    6. Start the Wear_OS_Small_Round_API_36 Android emulator
    7. Build the app with ./gradlew assembleDebug
    8. Install the build APK with adb install
    9. Verify the app is running and doesn't crash

# Final step
- Summerize all changes in libs.versions.toml and gradle-wrapper.properties and let the user approve them
- When approved create a commit for the changes named "Update dependencies"
- Push the changes

