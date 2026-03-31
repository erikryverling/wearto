---
name: upgrade-dependencies
description: Upgrades the project dependencies
---

- Use https://gradle.org to search for Gradle updates, including Gradle plugins
- Use https://mvnrepository.com to search for dependency updates
- Analyze libs.versions.toml and gradle-wrapper.properties
- Upgrade all dependencies in libs.versions.toml and gradle-wrapper.properties
- Always prefer stable version of dependencies, if possible
- Fix any compilation errors
- Verify the changes by doing the following:
  - Staring the Pixel_9_Pro_API_36 Android emulator
  - Building the app with ./gradlew assembleDebug
  - Installing the build APK with adb install 
  - Verify the app is running and doesn't crash
- Summerize all changes in libs.versions.toml and gradle-wrapper.properties and let me approve them
- When approved create a commit for the changes named "Bump dependencies"
- Push the changes
