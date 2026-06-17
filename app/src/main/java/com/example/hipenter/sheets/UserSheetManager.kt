package com.example.hipenter.sheets

import android.util.Log
import com.example.hipenter.network.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class UserSheetManager(
    private val client: OkHttpClient
) {
    private val TAG = "UserSheetManager"
    
    // We are using raw REST calls to the Sheets/Drive APIs here for simplicity and to leverage our OkHttp AuthInterceptor

    /**
     * Initializes the user's personal Google Sheet.
     * 1. Creates a new spreadsheet named "HiPenter — [userName]'s Posts"
     * 2. Sets permissions to "Anyone with the link can view"
     * 3. Sets up the header row
     * Returns the Spreadsheet ID and public URL.
     */
    suspend fun initializePersonalSheet(userName: String): Pair<String, String> = withContext(Dispatchers.IO) {
        // 1. Create Spreadsheet
        val createBody = """
            {
                "properties": {
                    "title": "HiPenter — $userName's Posts"
                }
            }
        """.trimIndent()

        val createRequest = Request.Builder()
            .url("https://sheets.googleapis.com/v4/spreadsheets")
            .post(createBody.toRequestBody("application/json".toMediaType()))
            .build()

        var spreadsheetId = ""
        var spreadsheetUrl = ""

        client.newCall(createRequest).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to create spreadsheet: ${response.code}")
            val responseBody = response.body?.string() ?: "{}"
            val json = JSONObject(responseBody)
            spreadsheetId = json.getString("spreadsheetId")
            spreadsheetUrl = json.getString("spreadsheetUrl")
        }

        // 2. Make it public (Drive API)
        val permBody = """
            {
                "role": "reader",
                "type": "anyone"
            }
        """.trimIndent()

        val permRequest = Request.Builder()
            .url("https://www.googleapis.com/drive/v3/files/$spreadsheetId/permissions")
            .post(permBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(permRequest).execute().use { response ->
            if (!response.isSuccessful) {
                Log.w(TAG, "Failed to set permissions, but sheet was created. Status: ${response.code}")
            }
        }

        // 3. Set headers (Sheets API)
        val headerBody = """
            {
                "values": [
                    ["PostID", "Content", "Timestamp", "Status"]
                ]
            }
        """.trimIndent()

        val headerRequest = Request.Builder()
            .url("https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/A1:D1?valueInputOption=USER_ENTERED")
            .put(headerBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(headerRequest).execute().use { response ->
            if (!response.isSuccessful) Log.w(TAG, "Failed to set headers. Status: ${response.code}")
        }

        return@withContext Pair(spreadsheetId, spreadsheetUrl)
    }

    /**
     * Appends a new post to the user's personal sheet.
     */
    suspend fun appendPostToSheet(spreadsheetId: String, post: Post) = withContext(Dispatchers.IO) {
        val body = """
            {
                "values": [
                    ["${post.id}", "${post.content.replace("\"", "\\\"")}", "${post.timestamp}", "ACTIVE"]
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/Sheet1:append?valueInputOption=USER_ENTERED")
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to append to sheet: ${response.code}")
        }
    }
}
