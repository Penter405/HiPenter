/**
 * HiPenter — Google Apps Script Backend
 *
 * Deploy as: Web App → Execute as "Me", Access "Anyone"
 *
 * Central Google Sheet Tabs:
 *   1. "Buffer"        — Recent posts (last 7 days)
 *   2. "Archive"       — Older posts moved by time-trigger
 *   3. "UserDirectory" — userId → sheetUrl → displayName mapping
 *
 * Buffer columns: PostID | Content | Author | AuthorPhotoUrl | UserSheetUrl | Timestamp | UserId
 * Archive columns: (same as Buffer)
 * UserDirectory columns: UserId | SheetUrl | DisplayName | RegisteredAt
 */

// ─── Configuration ──────────────────────────────────────────────────
const SPREADSHEET_ID = PropertiesService.getScriptProperties().getProperty('SPREADSHEET_ID');
const BUFFER_SHEET   = 'Buffer';
const ARCHIVE_SHEET  = 'Archive';
const DIRECTORY_SHEET = 'UserDirectory';
const PAGE_SIZE      = 50;
const BUFFER_MAX_AGE_DAYS = 7;

// ─── HTTP Handlers ──────────────────────────────────────────────────

function doGet(e) {
  const action = (e.parameter && e.parameter.action) || 'feed';

  if (action === 'feed') {
    return handleGetFeed(e);
  }

  return jsonResponse({ error: 'Unknown action' }, 400);
}

function doPost(e) {
  try {
    const body = JSON.parse(e.postData.contents);
    const action = body.action;

    switch (action) {
      case 'post':
        return handlePost(body);
      case 'delete':
        return handleDelete(body);
      case 'register':
        return handleRegister(body);
      default:
        return jsonResponse({ error: 'Unknown action: ' + action }, 400);
    }
  } catch (err) {
    return jsonResponse({ error: 'Invalid request: ' + err.message }, 400);
  }
}

// ─── Feed ───────────────────────────────────────────────────────────

function handleGetFeed(e) {
  const page = parseInt(e.parameter.page || '0', 10);
  const sheet = getSheet(BUFFER_SHEET);
  const data = sheet.getDataRange().getValues();

  if (data.length <= 1) {
    // Only header row or empty
    return jsonResponse({ posts: [], hasMore: false });
  }

  // Skip header, reverse to show newest first
  const rows = data.slice(1).reverse();
  const start = page * PAGE_SIZE;
  const pageRows = rows.slice(start, start + PAGE_SIZE);

  const posts = pageRows.map(function(row) {
    return {
      id: row[0],
      content: row[1],
      author: row[2],
      authorPhotoUrl: row[3] || null,
      userSheetUrl: row[4] || null,
      timestamp: row[5],
      userId: row[6],
    };
  });

  return jsonResponse({
    posts: posts,
    hasMore: start + PAGE_SIZE < rows.length,
  });
}

// ─── Post ───────────────────────────────────────────────────────────

function handlePost(body) {
  if (!body.content || !body.author || !body.userId) {
    return jsonResponse({ error: 'Missing required fields: content, author, userId' }, 400);
  }

  // Validate 15-word limit
  var wordCount = body.content.trim().split(/\s+/).length;
  if (wordCount > 15) {
    return jsonResponse({ error: 'Post exceeds 15-word limit' }, 400);
  }

  var postId = Utilities.getUuid();
  var timestamp = new Date().getTime();

  var sheet = getSheet(BUFFER_SHEET);
  sheet.appendRow([
    postId,
    body.content,
    body.author,
    body.authorPhotoUrl || '',
    body.userSheetUrl || '',
    timestamp,
    body.userId,
  ]);

  // Auto-register user if not already in directory
  ensureUserRegistered(body.userId, body.userSheetUrl, body.author);

  return jsonResponse({
    success: true,
    postId: postId,
    timestamp: timestamp,
  });
}

// ─── Delete ─────────────────────────────────────────────────────────

function handleDelete(body) {
  if (!body.postId || !body.userId) {
    return jsonResponse({ error: 'Missing postId or userId' }, 400);
  }

  var sheet = getSheet(BUFFER_SHEET);
  var data = sheet.getDataRange().getValues();

  for (var i = 1; i < data.length; i++) {
    // Match postId AND userId (only the author can delete)
    if (data[i][0] === body.postId && data[i][6] === body.userId) {
      sheet.deleteRow(i + 1); // Sheets rows are 1-indexed
      return jsonResponse({ success: true, deleted: body.postId });
    }
  }

  return jsonResponse({ error: 'Post not found or not owned by user' }, 404);
}

// ─── Register ───────────────────────────────────────────────────────

function handleRegister(body) {
  if (!body.userId || !body.sheetUrl || !body.displayName) {
    return jsonResponse({ error: 'Missing userId, sheetUrl, or displayName' }, 400);
  }

  var sheet = getSheet(DIRECTORY_SHEET);
  var data = sheet.getDataRange().getValues();

  // Check if already registered
  for (var i = 1; i < data.length; i++) {
    if (data[i][0] === body.userId) {
      // Update existing entry
      sheet.getRange(i + 1, 2).setValue(body.sheetUrl);
      sheet.getRange(i + 1, 3).setValue(body.displayName);
      return jsonResponse({ success: true, action: 'updated' });
    }
  }

  // New registration
  sheet.appendRow([
    body.userId,
    body.sheetUrl,
    body.displayName,
    new Date().getTime(),
  ]);

  return jsonResponse({ success: true, action: 'registered' });
}

// ─── Helper: Auto-Register ─────────────────────────────────────────

function ensureUserRegistered(userId, sheetUrl, displayName) {
  if (!userId || !sheetUrl) return;

  var sheet = getSheet(DIRECTORY_SHEET);
  var data = sheet.getDataRange().getValues();

  for (var i = 1; i < data.length; i++) {
    if (data[i][0] === userId) return; // Already registered
  }

  sheet.appendRow([userId, sheetUrl, displayName, new Date().getTime()]);
}

// ─── Time-Based Triggers ────────────────────────────────────────────

/**
 * Run hourly: Moves posts older than 7 days from Buffer → Archive.
 * Set up via: Triggers → Add Trigger → archiveOldPosts → Time-driven → Hour timer
 */
function archiveOldPosts() {
  var buffer = getSheet(BUFFER_SHEET);
  var archive = getSheet(ARCHIVE_SHEET);
  var data = buffer.getDataRange().getValues();
  var cutoff = new Date().getTime() - (BUFFER_MAX_AGE_DAYS * 24 * 60 * 60 * 1000);
  var rowsToDelete = [];

  for (var i = 1; i < data.length; i++) {
    var timestamp = data[i][5];
    if (typeof timestamp === 'number' && timestamp < cutoff) {
      archive.appendRow(data[i]);
      rowsToDelete.push(i + 1); // 1-indexed sheet row
    }
  }

  // Delete in reverse to preserve row indices
  for (var j = rowsToDelete.length - 1; j >= 0; j--) {
    buffer.deleteRow(rowsToDelete[j]);
  }

  Logger.log('Archived ' + rowsToDelete.length + ' posts.');
}

/**
 * Run daily: Removes orphaned directory entries where the Sheet no longer exists.
 * Set up via: Triggers → Add Trigger → cleanupOrphanedEntries → Time-driven → Day timer
 */
function cleanupOrphanedEntries() {
  // This is a lightweight check — just logs for now.
  // Full implementation would attempt to fetch each sheetUrl and remove dead ones.
  var sheet = getSheet(DIRECTORY_SHEET);
  var data = sheet.getDataRange().getValues();
  Logger.log('UserDirectory has ' + (data.length - 1) + ' entries.');
}

// ─── Utility ────────────────────────────────────────────────────────

function getSheet(name) {
  var ss = SpreadsheetApp.openById(SPREADSHEET_ID);
  var sheet = ss.getSheetByName(name);
  if (!sheet) {
    // Auto-create the tab with headers
    sheet = ss.insertSheet(name);
    if (name === BUFFER_SHEET || name === ARCHIVE_SHEET) {
      sheet.appendRow(['PostID', 'Content', 'Author', 'AuthorPhotoUrl', 'UserSheetUrl', 'Timestamp', 'UserId']);
    } else if (name === DIRECTORY_SHEET) {
      sheet.appendRow(['UserId', 'SheetUrl', 'DisplayName', 'RegisteredAt']);
    }
  }
  return sheet;
}

function jsonResponse(data, statusCode) {
  // Apps Script doGet/doPost always return 200; status is in the JSON body
  var output = ContentService
    .createTextOutput(JSON.stringify(data))
    .setMimeType(ContentService.MimeType.JSON);
  return output;
}

// ─── Setup Helper ───────────────────────────────────────────────────

/**
 * Run once manually to initialize the spreadsheet structure.
 * This creates all three tabs with proper headers.
 */
function setupSpreadsheet() {
  getSheet(BUFFER_SHEET);
  getSheet(ARCHIVE_SHEET);
  getSheet(DIRECTORY_SHEET);
  Logger.log('✅ All tabs created successfully!');
}
