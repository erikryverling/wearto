# 1 Update Item Models (Mobile & Wear)

## Status
DONE

## Description
Add `interactionCount: Long` to the `Item` domain model and Room entity in both the mobile and wear data modules.

Files to modify:
- `mobile/data/items/src/main/kotlin/se/yverling/wearto/mobile/data/items/model/Item.kt`
- `mobile/data/items/src/main/kotlin/se/yverling/wearto/mobile/data/items/db/Item.kt`
- `wear/data/items/src/main/kotlin/se/yverling/wearto/wear/data/items/model/Item.kt`
- `wear/data/items/src/main/kotlin/se/yverling/wearto/wear/data/items/db/Item.kt` (if exists, or similar)

Ensure that `toEntity()` and `toModel()` mapping functions are updated to include this new field. Default value should be `0`.

---

# 2 Update Mobile Database Schema & Migration

## Status
DONE

Files to modify:
- `mobile/data/items/src/main/kotlin/se/yverling/wearto/mobile/data/items/db/AppDatabase.kt`

---

# 3 Update Wearable Data Layer Sync Logic

## Status
Blocked by 1

## Description
Ensure that when items are synced between Wear and Mobile, the `interactionCount` is preserved and transmitted. 
Check `DataLayerListenerService` and any data serialization logic (e.g., using `DataMap` or JSON) to include the new field.

Files to check:
- `mobile/app/src/main/kotlin/se/yverling/wearto/mobile/data/DataLayerListenerService.kt`
- `wear/app/src/main/kotlin/se/yverling/wearto/wear/data/DataLayerListenerService.kt`
- Any shared mapper/serialization classes.

---

# 4 Implement Interaction Tracking on Wear OS

## Status
Blocked by 1

## Description
Update the Wear OS `ItemsScreen` or its ViewModel to increment the `interactionCount` of an item whenever it is clicked or toggled. 
The updated item should then be saved to the local Wear database.

Files to modify:
- `wear/feature/items/src/main/kotlin/se/yverling/wearto/wear/feature/items/ui/ItemsViewModel.kt` (or similar)

---

# 5 Sync Interaction Updates from Wear to Mobile

## Status
Blocked by 4

## Description
When an item's `interactionCount` is incremented on Wear, trigger a sync to the Mobile app. 
This could be done by sending a `Message` or updating a `DataItem` via the `Wearable.getDataClient()`.

Files to modify:
- `wear/data/items/src/main/kotlin/se/yverling/wearto/wear/data/items/ItemsRepositoryImpl.kt` (or wherever sync is triggered)

---

# 6 Handle Interaction Updates on Mobile

## Status
Blocked by 5

## Description
Update the Mobile `DataLayerListenerService` to handle incoming interaction count updates from the Wear device. 
When an update is received, it should update the corresponding item in the Mobile local database.

Files to modify:
- `mobile/app/src/main/kotlin/se/yverling/wearto/mobile/data/DataLayerListenerService.kt`

---

# 7 Add getItemsByInteractionCount to Mobile Repository

## Status
Blocked by 2

## Description
Add a new method to `ItemsRepository` in the mobile module to retrieve items sorted by their `interactionCount` in descending order.

Files to modify:
- `mobile/data/items/src/main/kotlin/se/yverling/wearto/mobile/data/items/ItemsRepository.kt`
- `mobile/data/items/src/main/kotlin/se/yverling/wearto/mobile/data/items/ItemsRepositoryImpl.kt`
- `mobile/data/items/src/main/kotlin/se/yverling/wearto/mobile/data/items/db/ItemsDao.kt`

---

# 8 Create Usage Statistics ViewModel (Mobile)

## Status
Blocked by 7

## Description
Create `UsageStatisticsViewModel` in the mobile app. This ViewModel should:
1. Fetch items from `ItemsRepository` using the new sorted method.
2. Expose a UI state containing the list of items for the leaderboard.

Create the file in: `mobile/feature/items/src/main/kotlin/se/yverling/wearto/mobile/feature/items/ui/UsageStatisticsViewModel.kt` (or a new feature package if preferred).

---

# 9 Implement Usage Statistics Screen UI (Leaderboard)

## Status
Blocked by 8

## Description
Implement the `UsageStatisticsScreen` using Jetpack Compose.
- Use a `LazyColumn` to display the leaderboard.
- Display rank, item name, and interaction count.
- Highlight the top 3 items (e.g., gold/silver/bronze icons or badges).
- Use Material 3 components.

Create the file in: `mobile/feature/items/src/main/kotlin/se/yverling/wearto/mobile/feature/items/ui/UsageStatisticsScreen.kt`.

---

# 10 Integrate Statistics Screen into Navigation

## Status
Blocked by 9

## Description
Add the `UsageStatisticsScreen` to the mobile app's navigation graph.
1. Define a new `UsageStatisticsRoute`.
2. Add a `composable` entry in `MainActivity`'s `NavHost`.
3. Add a navigation entry (icon/label) in the `BottomNavigation` or as an action in the `ItemsScreen`.

Files to modify:
- `mobile/app/src/main/kotlin/se/yverling/wearto/mobile/app/ui/MainActivity.kt`
- `mobile/app/src/main/kotlin/se/yverling/wearto/mobile/app/ui/NavigationItem.kt`

---

# 11 Add Unit Tests for Statistics Logic

## Status
Blocked by 8

## Description
Add unit tests to ensure:
1. The `interactionCount` increments correctly on Wear.
2. The Repository returns items in the correct order for the leaderboard.
3. The ViewModel correctly maps the data to UI state.

Files to create/modify:
- `wear/data/items/src/test/kotlin/...`
- `mobile/data/items/src/test/kotlin/...`
- `mobile/feature/items/src/test/kotlin/...`
