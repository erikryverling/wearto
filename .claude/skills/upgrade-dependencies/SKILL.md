---
name: upgrade-dependencies
description: Upgrades the project dependencies
---

## Finding Latest Versions

### For Gradle and Gradle Plugins
- Use https://gradle.org/releases/ for Gradle version updates
- Use https://plugins.gradle.org for Gradle plugin versions
- Check plugin-specific GitHub releases pages for the most accurate version information

### For Maven/Android Dependencies
1. **Primary Method**: Use WebSearch with specific queries:
    - Format: `"group:artifact" "version" latest maven 2026`
    - Example: `"androidx.navigation:navigation-compose" "2.9.7" OR "2.9.8" latest 2026`
    - Example: `"io.kotest:kotest-assertions-core" "6.0" OR "6.1" latest version`

2. **Check for Patch Releases**: Don't miss intermediate versions!
    - Search for "latest" may skip patch releases (e.g., finding 4.34.0 but missing 4.33.5)
    - For each dependency, search for versions between current and "latest"
    - Patch releases (x.x.X) often contain important bug fixes
    - Example: If current is 1.5.5 and latest is 1.6.0-alpha, check if 1.5.6 exists

3. **Verify Version Exists in Maven Central**:
    - **CRITICAL**: Documentation may mention versions not yet published to Maven
    - Always verify the version resolves during build, not just in documentation
    - If build fails with "plugin not found" or "artifact not found", the version isn't actually available yet
    - Example: AGP 9.x is documented but not in Maven Central as of Feb 2026

4. **Check Official Sources**:
    - AndroidX libraries: https://developer.android.com/jetpack/androidx/releases/
    - Kotlin libraries: https://github.com/Kotlin/[library-name]/releases
    - Firebase: https://firebase.google.com/support/release-notes/android
    - Maven Central directly: https://central.sonatype.com/artifact/[group]/[artifact]
    - For each dependency group, search for their official release pages

4. **Version Compatibility**:
    - For Kotlin ecosystem libraries (KSP, kotlinx.serialization, etc.), check compatibility with the Kotlin version
    - Example: KSP version format is `kotlin-version-ksp-version` (e.g., `2.2.21-2.0.5`)
    - Some libraries like kotlinx-serialization have different versions for different Kotlin versions

5. **Special Cases**:
    - kotlin-dsl-plugin: Must match Gradle version requirements (check Gradle warnings)
    - Hilt: Check GitHub issues for Kotlin metadata compatibility
      - **WARNING**: Hilt 2.59+ requires AGP 9.0+, which may not be available yet
      - Version dependencies: Hilt 2.57.2 (Kotlin ≤2.2.x), Hilt 2.58 (Kotlin 2.3.x), Hilt 2.59+ (AGP 9.0+)
    - Android Gradle Plugin (AGP):
      - **WARNING**: AGP 9.x is documented but not published to Maven Central as of Feb 2026
      - Always verify with actual build, not just documentation
      - If unavailable, stay on latest 8.x (e.g., 8.13.1)
    - Coil 3.x: Uses different Maven coordinates (io.coil-kt.coil3 vs io.coil-kt)

## Upgrade Process

1. Analyze libs.versions.toml and gradle-wrapper.properties
2. For EACH dependency, search for the latest version using the methods above
   - Check for "latest" major/minor version
   - **Also check for patch releases** between current and latest (e.g., if current is 1.5.5, check if 1.5.6, 1.5.7, etc. exist)
3. Upgrade all dependencies in libs.versions.toml and gradle-wrapper.properties
4. Always prefer stable versions over alpha/beta when possible
5. **Document version constraints**: Add comments explaining why certain versions cannot be upgraded
   - Example: `hilt = "2.58" # 2.59+ requires AGP 9.0+, which is not yet available`
   - Example: `android-plugin = "8.13.1" # AGP 9.x documented but not published to Maven`
6. Fix any compilation errors that arise from upgrades
7. If a documented version fails to resolve, it's not actually available yet - stay on previous version and document why

## Verification Steps

Verify the changes by doing the following in order:
1. Run `./gradlew assembleDebug` to check compilation
2. Run `./gradlew test` to run tests (may have issues but compilation is key)
3. Start the Pixel_9_Pro_API_36 Android emulator
4. Install the app in the mobile:app module
5. Verify that it starts
6. Start the Wear_OS_Small_Round_API_36 Android emulator
7. Install the app in the wear:app module
8. Verify that it starts

## Final Steps

1. Summarize ALL changes in libs.versions.toml and gradle-wrapper.properties in a clear table format
2. Present to user for approval - do not proceed without approval
3. When approved, create a commit for the changes named "Bump dependencies"
4. Push the changes to remote

## Common Pitfalls & Lessons Learned

### Missing Patch Releases
**Problem**: Searching for "latest" versions (e.g., "protobuf latest 2026") often returns major/minor releases but skips intermediate patch releases.
- **Example**: Found protobuf 4.34.0-RC1 but missed 4.33.5
- **Example**: Found wear-compose 1.6.0-alpha09 but missed 1.5.6
- **Solution**: Explicitly search for patch versions between current and "latest" (e.g., if on 4.33.1, check 4.33.2, 4.33.3, etc.)

### Documentation vs. Availability
**Problem**: Official documentation may reference versions not yet published to Maven Central.
- **Example**: AGP 9.0.0 is documented on Android Developers site but builds fail with "plugin not found"
- **Example**: Hilt 2.59.1 exists in documentation but requires unpublished AGP 9.0+
- **Solution**: Always verify with actual build. If it fails to resolve, document the constraint and stay on previous version.

### Dependency Chains
**Problem**: Some dependencies require specific versions of other dependencies.
- **Example**: Hilt 2.59+ requires AGP 9.0+, creating a blocked upgrade path
- **Solution**: Document these chains in comments so future upgrades can address them together

### Version Compatibility Testing
**Problem**: Assuming compatibility between versions without testing.
- **Solution**: Always run full build and test suite, even for "minor" upgrades. Patch releases can introduce subtle breaking changes.

### Emulator Storage Issues
**Problem**: Android emulators can fill up storage, preventing app installation during verification.
- **Solution**: Use `emulator -wipe-data` flag to start with fresh storage, or run `adb shell pm trim-caches 1G` to free space
