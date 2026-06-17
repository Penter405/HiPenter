package com.example.hipenter.data

import com.example.hipenter.auth.UserSession
import com.example.hipenter.network.HiPenterApi
import com.example.hipenter.network.models.Post
import com.example.hipenter.sheets.UserSheetManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

interface DataRepository {
    fun getFeed(): Flow<List<Post>>
    fun getMyPosts(userSession: UserSession): Flow<List<Post>>
    suspend fun submitPost(content: String, userSession: UserSession, sheetUrl: String, spreadsheetId: String): Post
    suspend fun deletePost(postId: String, userSession: UserSession)
}

class DefaultDataRepository(
    private val api: HiPenterApi,
    private val sheetManager: UserSheetManager,
    private val localCache: LocalCache
) : DataRepository {

    override fun getFeed(): Flow<List<Post>> = flow {
        // Emit cached data immediately for instant load
        val cached = localCache.getCachedFeed()
        if (cached.isNotEmpty()) {
            emit(cached)
        }

        // Fetch fresh data from network
        try {
            val freshData = api.getFeed(page = 0)
            localCache.saveFeed(freshData)
            emit(freshData)
        } catch (e: Exception) {
            if (cached.isEmpty()) {
                throw e
            }
            // If we have cache, silently fail network and stick with cache
        }
    }

    override fun getMyPosts(userSession: UserSession): Flow<List<Post>> = flow {
        // For now, we rely on the central feed or cache to filter user posts
        // In a complete implementation, this would read directly from their Google Sheet
        val cached = localCache.getCachedMyPosts()
        if (cached.isNotEmpty()) {
            emit(cached)
        }
        
        try {
            // Simulated: We pull from feed and filter
            val allPosts = api.getFeed(0)
            val myPosts = allPosts.filter { it.author == userSession.displayName }
            localCache.saveMyPosts(myPosts)
            emit(myPosts)
        } catch (e: Exception) {
            if (cached.isEmpty()) throw e
        }
    }

    override suspend fun submitPost(
        content: String,
        userSession: UserSession,
        sheetUrl: String,
        spreadsheetId: String
    ): Post {
        val newPost = Post(
            id = UUID.randomUUID().toString(),
            content = content,
            author = userSession.displayName,
            authorPhotoUrl = userSession.profilePhotoUrl,
            userSheetUrl = sheetUrl,
            timestamp = System.currentTimeMillis()
        )

        // 1. Write to user's personal Google Sheet (they own it)
        sheetManager.appendPostToSheet(spreadsheetId, newPost)

        // 2. Send to central Vercel feed (for discovery)
        api.submitToCentralFeed(newPost)

        return newPost
    }

    override suspend fun deletePost(postId: String, userSession: UserSession) {
        // 1. Delete from central feed
        api.deleteFromCentralFeed(postId, userSession.userId)
        
        // 2. Note: Deleting from the user's personal sheet requires reading the sheet, 
        // finding the row, and clearing it. For brevity, omitted from this MVP repository layer.
    }
}
