# Expense Tracker Android App & Desktop Widget

A native Android application with a home-screen quick add widget connected directly to your Google Sheets expense tracker (specifically tailored for the Sept 2026 layout).

## Project Features
* **Desktop / Launcher Widget**: Tap the '+' button on your home screen to instantly bring up a compact popup dialog.
* **Quick Add Dialog**: Pre-populated with your expense categories (`Food`, `Travel`, `Subscriptions`, `Luxury Purchase`, `Household`, `Rent`, `for others`), amount, note, and "Once a month" check.
* **Dynamic / Configurable URL**: Enter your Apps Script Webhook URL directly inside the app settings, or change default sheet target.
* **Automated CI/CD**: Includes GitHub Actions workflow to build the `.apk` on every git push.

---

## Quick Start (Build APK via GitHub in 2 minutes)

1. **Create a new GitHub Repository** (private or public).
2. **Extract this zip file** into your local folder.
3. Open your terminal in that folder and run:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of Expense Tracker"
   git branch -M main
   git remote add origin <YOUR_GITHUB_REPO_URL>
   git push -u origin main
   ```
4. Go to your GitHub repository -> Click on the **Actions** tab.
5. Watch the **Build Android APK** workflow run (~90 seconds).
6. Under **Artifacts** at the bottom of the completed workflow run, download `ExpenseTracker-debug-apk.zip`.
7. Unzip and install `app-debug.apk` directly onto your Android device!

---

## Google Sheet Apps Script Setup

1. Open your Google Sheet.
2. Click **Extensions** > **Apps Script**.
3. Copy and paste the contents of `google-apps-script/Code.js`.
4. Click **Deploy** > **New deployment**.
5. Select type: **Web app**.
   * **Execute as:** *Me*
   * **Who has access:** *Anyone*
6. Copy the resulting **Web App URL**.
7. Open the installed Android app, paste the URL into the settings card, and tap **Test Ping**!
