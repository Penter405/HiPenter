package com.example.hipenter.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserSession(
    val userId: String,        // Google account ID
    val email: String,
    val displayName: String,
    val profilePhotoUrl: String?,
    val accessToken: String,   // Short-lived, used for API requests
    val refreshToken: String?, // Long-lived, used to get new access tokens
    val personalSheetId: String? = null // Set once their Google Sheet is created
)
