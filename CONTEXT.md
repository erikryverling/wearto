# WearTo Domain Glossary

## Core Concepts

### Preset Item
A user-defined item template (name and optional database identifier) maintained on the mobile device and cached locally on Wear OS. Tapping a preset item on the watch initiates remote task creation.

### Task
A Todoist action item created in the user's selected Todoist project when a preset item is triggered on Wear OS.

### Item Interaction State
The transient, in-memory presentation status of a preset item chip on Wear OS during task creation:
- **Init**: Default idle state.
- **Loading**: Task creation request has been dispatched to the mobile device.
- **Successful**: Task creation confirmed by Todoist via mobile response; briefly highlighted before returning to Init.
- **Error**: Task creation failed (network, token, or project error); briefly highlighted before returning to Init.

### Project
A Todoist project target selected by the user in settings, where all created tasks are placed.
