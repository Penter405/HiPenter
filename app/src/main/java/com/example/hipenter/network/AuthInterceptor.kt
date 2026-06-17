package com.example.hipenter.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor that attaches the current Google OAuth access token to requests.
 * If the token is expired, it will attempt to refresh it.
 */
class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val onAuthError: () -> Unit
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Only attach token to Google APIs
        if (!originalRequest.url.host.contains("googleapis.com")) {
            return chain.proceed(originalRequest)
        }

        val token = tokenProvider()
        if (token == null) {
            onAuthError()
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        val response = chain.proceed(authenticatedRequest)

        // If unauthorized, it might mean the token expired. 
        // We trigger the error callback so the auth manager can refresh the token.
        if (response.code == 401) {
            onAuthError()
        }

        return response
    }
}
