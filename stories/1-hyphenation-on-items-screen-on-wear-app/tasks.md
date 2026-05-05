# Tasks

- [ ] **Research Horologist Chip flexibility**
  Check if `com.google.android.horologist.compose.material.Chip` supports a `label` as a Composable or if it only accepts a `String`. If it only accepts a `String`, plan to switch to `androidx.wear.compose.material3.Chip`.

- [ ] **Update Item composable to support hyphenation**
  Modify the `Item` composable in `ItemsScreen.kt`. Use a `Text` composable for the label and apply a `TextStyle` that includes `hyphens = Hyphens.Auto` and `lineBreak = LineBreak.Paragraph`.
  ```kotlin
  Text(
      text = item.name,
      style = MaterialTheme.typography.labelMedium.copy(
          hyphens = Hyphens.Auto,
          lineBreak = LineBreak.Paragraph
      )
  )
  ```

- [ ] **Add visual regression previews**
  Add a new `@Preview` in `ItemsScreen.kt` specifically containing the problematic Swedish words from the user story ("Matlagningsgrädde", "Hushållspapper", "Diskmaskinstabletter", "Kolsyrepatroner") to verify the hyphenation rules visually in the IDE.

- [ ] **Verify on Wearable Emulator**
  Run the app on a Wearable emulator.
  - Set the system language to Swedish.
  - Navigate to the Items screen.
  - Add the specific items mentioned in the story and verify they break correctly with hyphens.
