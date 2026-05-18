# Implementation Plan - Mood Selector for Chronicle Entries

Add a mood selector feature to the Chronicle entry screen, allowing users to select from default moods or create a custom one with an emoji and text.

## User Review Required

> [!NOTE]
> I will add a horizontal list of mood options above the tag picker in the Chronicle entry screen. The last option will be a "+" button to create a custom mood.

## Proposed Changes

### Data Layer

#### [Memoir.kt](file:///C:/Users/rad/AndroidStudioProjects/Memoir/app/src/main/java/com/example/memoir/data/Memoir.kt)

- Update `Memoir` entity to include `moodEmoji: String?` and `moodLabel: String?`.

---

### UI Layer

#### [DeskViewModel.kt](file:///C:/Users/rad/AndroidStudioProjects/Memoir/app/src/main/java/com/example/memoir/ui/editor/DeskViewModel.kt)

- Add `moodEmoji` and `moodLabel` states.
- Update `load` and `save` functions to handle mood data.

#### [DeskScreen.kt](file:///C:/Users/rad/AndroidStudioProjects/Memoir/app/src/main/java/com/example/memoir/ui/editor/DeskScreen.kt)

- Implement `MoodSelector` component.
- Implement `CustomMoodDialog` for choosing custom emoji and text.
- Integrate `MoodSelector` into the `DeskScreen` layout (only for Chronicle mode).

#### [ChronicleScreen.kt](file:///C:/Users/rad/AndroidStudioProjects/Memoir/app/src/main/java/com/example/memoir/ui/home/ChronicleScreen.kt)

- Update `MemoirCard` to display the mood emoji if present.

---

## Verification Plan

### Automated Tests
- Verify code compilation using `analyze_file`.

### Manual Verification
1.  **Selection**: Open a new Chronicle entry, select a default mood (e.g., 😊 Happy), and verify it's highlighted.
2.  **Custom Mood**: Click the "+" button, enter an emoji and a label, save, and verify it appears in the list and is selected.
3.  **Persistence**: Save the entry, then reopen it or check the main list to ensure the mood emoji is displayed.
4.  **Editing**: Change the mood of an existing entry and verify the change is saved.
