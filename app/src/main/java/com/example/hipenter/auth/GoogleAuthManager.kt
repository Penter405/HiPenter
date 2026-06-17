package com.example.hipenter.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthManager(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

    // TODO: Replace with your actual Web Client ID from Google Cloud Console
    private val webClientId = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"

    suspend fun signIn(): Result<UserSession> {
        return try {
            // Generate a nonce for secure sign-in
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(hashedNonce)
                // Requesting scopes for Google Sheets and Drive
                .associateLinkedAccounts("https://www.googleapis.com/auth/spreadsheets", null)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context, request)
            handleSignInResult(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleSignInResult(result: GetCredentialResponse): Result<UserSession> {
        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                
                // In a real app, you would exchange the ID token for an Access Token and Refresh Token here
                // via your backend or directly with Google's OAuth endpoints.
                // For now, we mock the session creation.
                val session = UserSession(
                    userId = googleIdTokenCredential.id,
                    email = googleIdTokenCredential.id, // Using ID as fallback
                    displayName = googleIdTokenCredential.displayName ?: "User",
                    profilePhotoUrl = googleIdTokenCredential.profilePictureUri?.toString(),
                    accessToken = "mock_access_token",
                    refreshToken = "mock_refresh_token"
                )
                
                _userSession.value = session
                return Result.success(session)
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }
        return Result.failure(Exception("Unexpected credential type"))
    }

    fun signOut() {
        _userSession.value = null
        // Note: You would also want to clear credential manager state here 
        // credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}
