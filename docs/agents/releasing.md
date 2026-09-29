# Releasing

Procedures and publishing scripts for WearTo applications.

## Release Commands

- **Mobile:** `./bin/release-mobile <version>`
- **Wear OS:** `./bin/release-wear <version>`

Both scripts accept a semantic version argument (e.g. `2.1.0`).

## Side Effects & Approval

Executing either release script performs destructive remote and publishing operations:
1. Increments `versionCode` and updates `versionName` in module `build.gradle.kts`.
2. Commits the version bump and pushes directly to `origin`.
3. Creates and pushes a git tag (`m<version>` or `w<version>`).
4. Updates and commits `Gemfile.lock`.
5. Invokes Fastlane to build the release App Bundle (`.aab`) and upload directly to Google Play Store tracks.

**Hard rule:** Always obtain explicit user approval before executing `./bin/release-mobile` or `./bin/release-wear`.
