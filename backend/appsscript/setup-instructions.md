# Google Apps Script — Setup Instructions

Follow these steps to deploy the HiPenter backend.

---

## 1. Create the Central Google Sheet

1. Go to [Google Sheets](https://sheets.google.com) and create a new spreadsheet
2. Name it: **HiPenter Central Feed**
3. Copy the **Spreadsheet ID** from the URL:
   ```
   https://docs.google.com/spreadsheets/d/{THIS_IS_THE_ID}/edit
   ```

## 2. Create the Apps Script Project

1. In your Google Sheet, go to **Extensions → Apps Script**
2. Delete any code in the default `Code.gs` file
3. Paste the entire contents of [`Code.gs`](./Code.gs) into the editor
4. Click **Save** (Ctrl+S)

## 3. Set the Script Property

1. In the Apps Script editor, go to **Project Settings** (⚙️ gear icon on the left)
2. Scroll down to **Script Properties**
3. Click **Add script property**
4. Set:
   - Property: `SPREADSHEET_ID`
   - Value: *(the Spreadsheet ID you copied in Step 1)*
5. Click **Save**

## 4. Initialize the Spreadsheet

1. In the Apps Script editor, select the function `setupSpreadsheet` from the dropdown at the top
2. Click **Run** (▶)
3. Grant permissions when prompted (it will ask to access Google Sheets)
4. Check the spreadsheet — you should see three tabs: **Buffer**, **Archive**, **UserDirectory**

## 5. Deploy as Web App

1. Click **Deploy → New deployment**
2. Click the ⚙️ gear icon → select **Web app**
3. Set:
   - Description: `HiPenter Backend v1`
   - Execute as: **Me** (your Google account)
   - Who has access: **Anyone**
4. Click **Deploy**
5. Copy the **Web app URL** — it looks like:
   ```
   https://script.google.com/macros/s/AKfyc.../exec
   ```

> **⚠️ IMPORTANT**: Keep this URL secret! It goes into the Vercel environment variable, never directly into the Android app.

## 6. Set Up Time-Based Triggers

1. In the Apps Script editor, click the ⏰ **Triggers** icon (left sidebar)
2. Click **+ Add Trigger** and set:
   - Function: `archiveOldPosts`
   - Event source: Time-driven
   - Type: Hour timer
   - Interval: Every hour
3. Click **Save**
4. Add another trigger:
   - Function: `cleanupOrphanedEntries`
   - Event source: Time-driven
   - Type: Day timer
   - Time of day: 2am - 3am
5. Click **Save**

## 7. Configure Vercel Environment Variable

1. Go to your [Vercel dashboard](https://vercel.com/dashboard)
2. Select the `hipenter-proxy` project
3. Go to **Settings → Environment Variables**
4. Add:
   - Name: `APPS_SCRIPT_URL`
   - Value: *(the Web app URL from Step 5)*
5. **Redeploy** the Vercel project for the change to take effect

---

## Testing

You can test the endpoints manually:

### Read Feed
```bash
curl "YOUR_APPS_SCRIPT_URL?action=feed&page=0"
```

### Submit Post
```bash
curl -X POST YOUR_APPS_SCRIPT_URL \
  -H "Content-Type: application/json" \
  -d '{"action":"post","content":"Hello world","author":"Test User","userId":"test123"}'
```

### Delete Post
```bash
curl -X POST YOUR_APPS_SCRIPT_URL \
  -H "Content-Type: application/json" \
  -d '{"action":"delete","postId":"POST_ID_HERE","userId":"test123"}'
```

### Register User
```bash
curl -X POST YOUR_APPS_SCRIPT_URL \
  -H "Content-Type: application/json" \
  -d '{"action":"register","userId":"test123","sheetUrl":"https://docs.google.com/spreadsheets/d/xxx","displayName":"Test User"}'
```
