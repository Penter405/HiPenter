# Next Steps: Deployment & Setup Guide

Now that the codebase is complete, you need to manually set up the backend infrastructure and connect everything. Here is the step-by-step order:

## 1. Google Cloud Console Setup (OAuth Credentials)
The Android app needs permission to access Google Drive/Sheets to save user posts.
- [ ] Go to [Google Cloud Console](https://console.cloud.google.com).
- [ ] Create a new project.
- [ ] Enable the **Google Sheets API** and **Google Drive API**.
- [ ] Configure the OAuth Consent Screen (External, name it "HiPenter"). Add the scopes `.../auth/spreadsheets` and `.../auth/drive.file`.
- [ ] Create **OAuth 2.0 Client IDs**:
  - **Android Client ID**: You'll need your app's SHA-1 fingerprint.
  - **Web Client ID**: Needed for Credential Manager (Google Sign-In).

## 2. Central Google Sheet & Apps Script Backend
This is where the global feed data is aggregated.
- [ ] Create a new Google Sheet (e.g., "HiPenter Central Feed") at [sheets.google.com](https://sheets.google.com). Copy its **Spreadsheet ID** from the URL.
- [ ] Go to **Extensions → Apps Script**.
- [ ] Replace the default code with the contents of `backend/appsscript/Code.gs`.
- [ ] Go to Project Settings (⚙️ gear icon) → **Script Properties**. Add a property named `SPREADSHEET_ID` and paste your Spreadsheet ID.
- [ ] Select the `setupSpreadsheet` function from the top toolbar and click **Run** to initialize the tabs (Buffer, Archive, UserDirectory).
- [ ] Click **Deploy → New deployment**. Choose **Web app**, execute as "Me", and allow access to "Anyone". Click Deploy.
- [ ] **Copy the Web App URL** (you will need this for Vercel).
- [ ] Go to Triggers (⏰ icon) and add two time-driven triggers:
  - `archiveOldPosts` (Hour timer, Every hour)
  - `cleanupOrphanedEntries` (Day timer, 2am - 3am)

## 3. Vercel Proxy Deployment
Vercel protects your Apps Script from being overwhelmed by rate-limiting and caching requests.
- [ ] Log in to [Vercel](https://vercel.com/) and add a new project.
- [ ] Point it to your GitHub repository and set the **Root Directory** to `backend/vercel`.
- [ ] Before deploying, go to Environment Variables and add `APPS_SCRIPT_URL`, pasting the Web App URL you copied in Step 2.
- [ ] Deploy the project.
- [ ] **Copy the Vercel Domain** (e.g., `https://hipenter-proxy.vercel.app`).

## 4. Connect the Android App
Tell the Android app where to find your Vercel proxy.
- [ ] Open `gradle.properties` (or your local environment) and ensure the `VERCEL_BASE_URL` property is pointing to your new Vercel domain. If using local properties or command line, build the app using:
  ```bash
  ./gradlew assembleDebug -PVERCEL_BASE_URL="https://your-vercel-domain.vercel.app"
  ```
- [ ] Alternatively, just verify that the default fallback in `app/build.gradle.kts` matches your URL.

## 5. GitHub Actions & GitHub Pages
Get your CI/CD and landing page live.
- [ ] Go to your repository settings on GitHub → **Secrets and variables → Actions**.
- [ ] Add the following secrets for your release signing:
  - `SIGNING_KEYSTORE_BASE64` (base64 encoded `.jks` file)
  - `SIGNING_STORE_PASSWORD`
  - `SIGNING_KEY_ALIAS`
  - `SIGNING_KEY_PASSWORD`
- [ ] Push your code to the `main` branch.
- [ ] Create a new tag (e.g., `v1.0.0`) and push it to trigger the Release workflow:
  ```bash
  git tag v1.0.0
  git push origin v1.0.0
  ```
- [ ] Go to repository settings → **Pages**. Set the source to deploy from the `main` branch and the `/docs` folder. This will host your `index.html` landing page.
