# 1 Usage Statistics Screen

## Description
A new screen in the WearTo **mobile app** that provides users with insights into their usage patterns. The screen will focus on "Item Interactions," specifically tracking and displaying the lifetime total of how many times each item has been interacted with (clicked or toggled) on the **Wear OS companion app**. The goal is to give users a sense of which items they use most frequently in a "Leaderboard Style" presentation on their phone.

## Design
- **Entry Point:** The screen will be accessible via navigation from the main screen in the mobile app.
- **Presentation:** A "Leaderboard Style" list showing items ranked by their interaction count.
- **Visuals:** 
    - The most used items will be highlighted at the top.
    - Each entry will show the item name and its total lifetime interaction count.
    - Simple visual indicators (like a small badge or distinctive styling for the top 3) will be used to emphasize ranking.
- **Platform:** Android mobile application using Jetpack Compose (Material 3).

## Definition of Done
- **Data Model:** Update the data models and database schemas to track `interactionCount` for each item.
- **Wear App Tracking:** Update the `ItemsScreen` and data layer in the Wear OS app to increment and transmit the interaction count whenever an item is toggled or clicked.
- **Data Sync:** Ensure the Wearable Data Layer correctly syncs these interaction events/counts from the Wear OS device to the Mobile app's local storage.
- **UI Implementation:** Create a new `StatisticsScreen` (and corresponding ViewModel) in the Mobile app that retrieves and displays the interaction data.
- **Navigation:** Add the `StatisticsScreen` to the mobile app's navigation graph.
- **Tests:** Add unit tests for the interaction count increment logic, data layer syncing, and the repository/viewmodel data flow.
- **Styling:** Ensure the leaderboard looks modern and fits the mobile app's theme.
