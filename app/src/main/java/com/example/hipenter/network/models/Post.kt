package com.example.hipenter.network.models

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val content: String,
    val author: String,
    val authorPhotoUrl: String? = null,
    val userSheetUrl: String? = null,  // Link to user's public sheet
    val timestamp: Long,
)
