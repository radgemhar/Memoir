# Memoir 📝

Memoir is a private journaling, goal tracking, and milestone organization application for Android. It enables users to record memories, organize folders, track major milestones, and quickly compose new entries from anywhere on their device using a swipeable edge handle overlay.

---

## Key Features

- **Chronicle Entries**: Write and edit diary/journal entries chronologically, preserving your thoughts and memory timelines.
- **Milestones Tracking**: Set goals, log accomplishments, monitor task progress with a dynamic progress bar, and link important milestones directly to specific journal entries.
- **Quick Action Overlay**: Swipe a high-visibility edge handle from the upper-right corner of your screen—even while inside other apps—to open Memoir directly to the entry composer.
- **Smart Toggle Lifecycle**: Turn on the quick entry shortcut in Settings, and the app will automatically prompt you for system permission and manage the overlay visibility dynamically (strictly hiding it when you're in the app, and showing it when you leave).
- **Personalized Customization**: Fully responsive Dark/Light themes, scalable font sizes, and folder categorization.
- **Archive & Deleted Recovery**: Safely archive older logs or recover items deleted within 30 days from the trash bin.

---

## APK Installation Instructions

Follow these simple steps to install **`Memoir.apk`** on any Android device:

### Step 1: Transfer the APK to your Device
1. Locate the **`Memoir.apk`** file in the root of the project folder.
2. Copy the APK file to your phone using one of these methods:
   - Connect your phone to your computer via USB and transfer the file.
   - Upload the APK to Google Drive, then download it on your phone.
   - Send the file to yourself via an email attachment or messaging app (e.g., Discord, Telegram).

### Step 2: Enable Unknown Sources (If prompted)
Modern Android systems protect you from unofficial apps. When you open the APK for the first time, you may see a prompt.
1. Tap the **`Memoir.apk`** file on your phone's File Manager or Downloads folder.
2. If a popup says *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings** on the popup.
   - Toggle **Allow from this source** ON.
   - Press the Back button to return to the installer.

### Step 3: Install the App
1. Tap **Install** on the installation screen.
2. **Google Play Protect Warning**: Because this is a debug build of a school project, Google Play Protect might show a warning stating *"Unsafe App Blocked"*.
   - Tap **More details** (or **Details**).
   - Tap **Install anyway** to complete the installation.
3. Once completed, tap **Open**.

### Step 4: Configure the Quick Action Overlay (Optional)
To use the swiping shortcut overlay:
1. Open Memoir, tap the **Settings** gear icon.
2. Toggle **Quick Action Overlay** ON.
3. The app will automatically redirect you to the Android System Settings page for **Display over other apps**.
4. Scroll down, select **Memoir**, and toggle **Allow display over other apps** ON.
5. Press Back to return to Memoir. You will see the toggle is now ON, and a notification will keep the service active.
6. Press your home button. A thin gray handle will appear on the upper right edge of your screen. Swipe it leftward to test the quick-entry shortcut!

---

## Development Setup

To build and compile the project manually:

### Prerequisites
- JDK 17 or higher
- Android SDK (API 34 or higher)

### Build Commands (Windows PowerShell)
- **Compile project source files**:
  ```powershell
  .\gradlew.bat compileDebugSources
  ```
- **Build the Debug APK**:
  ```powershell
  .\gradlew.bat assembleDebug
  ```
- **Install directly onto a connected device/emulator**:
  ```powershell
  .\gradlew.bat installDebug
  ```
