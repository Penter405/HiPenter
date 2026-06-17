package com.example.hipenter.network

import com.example.hipenter.network.models.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class HiPenterApi(
    private val client: OkHttpClient,
    private val vercelBaseUrl: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Gets the global feed from the Vercel proxy.
     */
    suspend fun getFeed(page: Int = 0): List<Post> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("$vercelBaseUrl/api/feed?page=$page")
            .get()
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to load feed: ${response.code}")
            
            val body = response.body?.string() ?: "[]"
            return@withContext json.decodeFromString<List<Post>>(body)
        }
    }

    /**
     * Submits a post to the central feed via Vercel proxy.
     * Note: Writing to the user's personal sheet is handled separately by UserSheetManager.
     */
    suspend fun submitToCentralFeed(post: Post) = withContext(Dispatchers.IO) {
        val jsonBody = """
            {
                "action": "post",
                "id": "${post.id}",
                "content": "${post.content.replace("\"", "\\\"")}",
                "author": "${post.author}",
                "authorPhotoUrl": "${post.authorPhotoUrl ?: ""}",
                "userSheetUrl": "${post.userSheetUrl ?: ""}",
                "timestamp": ${post.timestamp}
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$vercelBaseUrl/api/post")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to submit to central feed: ${response.code}")
        }
    }

    /**
     * Deletes a post from the central feed.
     */
    suspend fun deleteFromCentralFeed(postId: String, userId: String) = withContext(Dispatchers.IO) {
        val jsonBody = """
            {
                "action": "delete",
                "postId": "$postId",
                "userId": "$userId"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("$vercelBaseUrl/api/delete")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to delete post: ${response.code}")
        }
    }
}
