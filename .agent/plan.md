# Project Plan

Completely rebrand and rename the app to "Memoir". This includes package names, directory structures, file names, UI strings, and all internal terminology to reflect a narrative storytelling focus. Rename package to com.example.memoir. Update the aesthetic to be more elegant.

## Project Brief

# Memoir Project Brief


Memoir is an elegant, narrative-focused application designed for capturing life reflections and documenting personal journeys. Rebranded from Notable
 and Braincrumbs, Memoir transforms traditional note-taking and task management into a sophisticated storytelling experience, utilizing an evolved "Midnight Blue" aesthetic
.

## Features

*   **The Chronicle (Unified Home):** An elegant central hub titled "The Chronicle"
 that displays a chronological list of Memoirs and Milestones. It features a persistent search bar for quick recollection and bottom navigation to
 toggle between story entries and life progress.
*   **The Desk (Narrative Editor):** A refined writing environment for "Mem
oirs" (narrative accounts) featuring storytelling-first tools: auto-save, Undo/Redo capabilities, a
 character counter, and prominent date/time stamps for every entry.
*   **Life Milestones:** A hierarchical progress
 tracker reimagined for documenting life chapters. It supports nested sub-milestones with intelligent checkbox synchronization—completing all children
 automatically marks the parent "Chapter" as complete.
*   **Preserved Reflections (Archive):** A streamlined archival system to
 keep the main Chronicle focused. Memoirs and Milestones can be archived or deleted using intuitive swipe gestures, accompanied by "Undo" snackbars
 to ensure no memory is accidentally lost.
*   **Adaptive Narrative Layout:** A vibrant yet elegant Material 3 interface
 that adapts seamlessly to any device. Utilizing a "List-Detail" strategy, it provides a tailored experience for phones, tablets,
 and foldables.

## High-Level Tech Stack

*   **Kotlin:** The primary language for all application and
 business logic.
*   **Jetpack Compose:** For building a modern, reactive, and elegant Material 3 user
 interface.
*   **Jetpack Navigation 3:** A state-driven approach for robust and predictable navigation across the Chronicle
, Desk, and Milestones.
*   **Compose Material Adaptive:** Used to implement responsive layouts that scale the storytelling
 experience for all screen sizes.
*   **Room Database:** Required for the local persistence of Memoirs, hierarchical Milestones, and
 the archive state.
*   **Kotlin Coroutines & Flow:** For handling background operations and providing reactive updates to the UI layer
.

## Implementation Steps

### Task_1_SetupFoundation: Set up the core data layer and the Midnight Blue theme. This includes the Room database (Crumb entity, DAO), Repository, and Hilt DI. Configure the Material 3 theme with the specified Light and Dark mode color palettes.
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully set up the foundation of the Braincrumbs app.
- **Acceptance Criteria:**
  - Room database and DAO are functional
  - Midnight Blue Material 3 theme is correctly implemented for light and dark modes
  - Hilt is set up and providing dependencies

### Task_2_TheTrailUI: Implement 'The Trail' (Home Screen) UI and Navigation 3. Create the list view for crumbs using LazyColumn and implement the filter chips (All/Active/Buried). Set up the top toolbar and FAB.
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully implemented 'The Trail' (Home Screen) and Navigation 3.
- **Acceptance Criteria:**
  - Home screen displays a list of crumbs with the specified card design
  - Filter chips correctly filter crumbs based on status
  - Navigation 3 is configured to move between Home and Editor screens

### Task_3_CrumbEditor: Build the 'Drop a Crumb' (Editor) screen. Implement fields for Title and Body, a color tag picker (Plain, Idea, Task, Memory, Urgent), and auto-save logic on back navigation.
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully built the 'Drop a Crumb' (Editor) screen.
- **Acceptance Criteria:**
  - Notes can be created and edited
  - Color tags are correctly applied and persisted
  - Auto-save works when navigating away from the editor

### Task_4_InteractionsSearch: Implement advanced interactions and search. This includes swipe actions (Swipe Left to Bury, Swipe Right to Sweep Away with Undo), search functionality (Retrace Your Steps), and pinned crumbs logic (max 5 pinned at top).
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully implemented advanced interactions and search.
- **Acceptance Criteria:**
  - Swipe actions for archiving and deleting are functional with Undo snackbar
  - Search filter correctly queries titles and bodies
  - Pinned crumbs are displayed at the top of the trail

### Task_5_FinalPolishVerify: Refine UI details, add app icon, and perform final verification. Implement the 'new crumb' indicator (blue dot), format timestamps (Dropped X ago), create an adaptive app icon, and ensure edge-to-edge display.
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully refined the UI:
- **Acceptance Criteria:**
  - Adaptive app icon matches the midnight blue theme
  - Timestamps use 'Dropped X ago' format
  - 'New' indicator dot appears on fresh crumbs
  - Application is stable, passes build, and meets all UI requirements

### Task_6_HierarchicalTasksData: Update the data layer to support hierarchical tasks. Modify the Room database to include a Task entity with parent-child relationships for nested sub-tasks. Implement the logic for intelligent checkbox synchronization (checking a parent checks all children, and parent status derived from children).
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully updated the data layer to support hierarchical tasks.
- **Acceptance Criteria:**
  - Room database supports nested tasks and sub-task relationships
  - Checkbox logic for parent-child synchronization is correctly implemented
  - Repository provides reactive flows for both Crumbs and Tasks

### Task_7_AdvancedUIAndNavigation: Implement the unified UI with Bottom Navigation, persistent search, and the advanced editor features. Integrate the persistent 'Retrace Your Steps' search bar, add Bottom Navigation (Notes/Tasks), enhance the Editor with Undo/Redo and character counters, and implement the List-Detail adaptive layout for different screen sizes. Perform a final Run and Verify.
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully implemented the unified UI with Bottom Navigation, persistent search, and advanced editor features.
- **Acceptance Criteria:**
  - Bottom navigation and persistent search are fully functional
  - Editor includes Undo/Redo, character counter, and explicit timestamps
  - App implements List-Detail adaptive strategy and three-dot card menus
  - App builds successfully, passes all tests, and critic_agent verifies stability and UI fidelity

### Task_8_RebrandToMemoir: Rebrand the application to 'Memoir'. Perform a comprehensive refactor of the package name to 'com.example.memoir', including directory structures and file updates. Replace all terminology with narrative-focused storytelling language: rename 'Home' to 'The Chronicle', 'Notes' to 'Memoirs', 'Editor' to 'The Desk', 'Tasks' to 'Milestones', and 'Archive' to 'Preserved Reflections'.
- **Status:** COMPLETED
- **Updates:** The coder_agent successfully rebranded the app to 'Memoir'.
- **Acceptance Criteria:**
  - Package name and directory structure updated to com.example.memoir
  - applicationId and namespace in build.gradle.kts updated
  - All UI strings and internal variables reflect 'Memoir' storytelling branding

### Task_9_MemoirPolishAndVerify: Refine 'Memoir' visual identity and perform Final Verification. Update the adaptive app icon for the new brand and refine the 'Midnight Blue' theme for a more elegant, narrative aesthetic. Perform a final 'Run and Verify' step: instruct critic_agent to verify application stability (no crashes), confirm consistent rebranding, and ensure alignment with the storytelling-focused project brief.
- **Status:** COMPLETED
- **Updates:** The critic_agent successfully verified the 'Memoir' application.
- Application is stable with no crashes.
- All core features (The Chronicle, The Desk, Milestones, Preserved Reflections) are functional.
- Rebranding is consistent across all strings and logic (no old terminology remains).
- The "Midnight & Gold" aesthetic and the new adaptive icon (Gold Quill) are correctly implemented and meet the storytelling-focused project brief.
- Edge-to-edge display and adaptive layouts are verified.
Final quality check passed.
- **Acceptance Criteria:**
  - Elegant 'Memoir' adaptive app icon implemented
  - Build pass, all existing tests pass, and app does not crash
  - Rebranding is consistent across all screens and logic
  - Narrative storytelling focus confirmed by critic_agent
- **Duration:** N/A

