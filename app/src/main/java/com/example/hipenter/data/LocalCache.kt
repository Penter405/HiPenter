package com.example.hipenter.data

import android.content.Context
import android.content.SharedPreferences
import com.example.hipenter.network.models.Post
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hipenter_cache", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun saveFeed(posts: List<Post>) {
        val serialized = json.encodeToString(posts)
        prefs.edit().putString("cached_feed", serialized).apply()
    }

    fun getCachedFeed(): List<Post> {
        val serialized = prefs.getString("cached_feed", null) ?: return emptyList()
        return try {
            json.decodeFromString(serialized)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMyPosts(posts: List<Post>) {
        val serialized = json.encodeToString(posts)
        prefs.edit().putString("cached_my_posts", serialized).apply()
    }

    fun getCachedMyPosts(): List<Post> {
        val serialized = prefs.getString("cached_my_posts", null) ?: return emptyList()
        return try {
            json.decodeFromString(serialized)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
