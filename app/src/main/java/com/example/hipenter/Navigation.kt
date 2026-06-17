package com.example.hipenter

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.hipenter.auth.GoogleAuthManager
import com.example.hipenter.data.DefaultDataRepository
import com.example.hipenter.data.LocalCache
import com.example.hipenter.network.HiPenterApi
import com.example.hipenter.sheets.UserSheetManager
import com.example.hipenter.ui.auth.LoginScreen
import com.example.hipenter.ui.compose.ComposePostScreen
import com.example.hipenter.ui.main.MainScreen
import com.example.hipenter.ui.main.MainScreenViewModel
import com.example.hipenter.ui.profile.ProfileScreen
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Create shared dependencies
    val authManager = remember { GoogleAuthManager(context) }
    val okHttpClient = remember { OkHttpClient.Builder().build() }
    // TODO: Replace with your actual Vercel deployment URL
    val vercelBaseUrl = "https://hipenter-proxy.vercel.app"
    val api = remember { HiPenterApi(okHttpClient, vercelBaseUrl) }
    val sheetManager = remember { UserSheetManager() }
    val localCache = remember { LocalCache(context) }
    val repository = remember { DefaultDataRepository(api, sheetManager, localCache) }
    val viewModel = remember { MainScreenViewModel(repository, authManager) }

    // Auth state
    var isSigningIn by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Posting state
    var isPosting by remember { mutableStateOf(false) }
    var postError by remember { mutableStateOf<String?>(null) }

    // Determine start destination based on auth state
    val userSession by authManager.userSession.let {
        val state = remember { mutableStateOf(it.value) }
        // Collect session updates
        androidx.compose.runtime.LaunchedEffect(Unit) {
            it.collect { session -> state.value = session }
        }
        state
    }

    val startDest: Any = if (userSession != null) Main else Login
    val backStack = rememberNavBackStack(startDest)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
        entryProvider {
            entry<Login> {
                LoginScreen(
                    onSignInClick = {
                        scope.launch {
                            isSigningIn = true
                            loginError = null
                            val result = authManager.signIn()
                            isSigningIn = false
                            result.fold(
                                onSuccess = {
                                    // Clear backstack and navigate to Feed
                                    backStack.clear()
                                    backStack.add(Main)
                                },
                                onFailure = { e ->
                                    loginError = e.message ?: "Sign-in failed"
                                }
                            )
                        }
                    },
                    isLoading = isSigningIn,
                    errorMessage = loginError
                )
            }

            entry<Main> {
                MainScreen(
                    onItemClick = { navKey -> backStack.add(navKey) },
                    modifier = Modifier.safeDrawingPadding(),
                    viewModel = viewModel
                )
            }

            entry<ComposePost> {
                ComposePostScreen(
                    onPost = { content ->
                        scope.launch {
                            val session = userSession ?: return@launch
                            isPosting = true
                            postError = null
                            try {
                                repository.submitPost(
                                    content = content,
                                    userSession = session,
                                    sheetUrl = session.personalSheetId ?: "",
                                    spreadsheetId = session.personalSheetId ?: ""
                                )
                                isPosting = false
                                // Go back to feed and refresh
                                backStack.removeLastOrNull()
                                viewModel.loadFeed()
                            } catch (e: Exception) {
                                isPosting = false
                                postError = e.message ?: "Failed to post"
                            }
                        }
                    },
                    onCancel = { backStack.removeLastOrNull() },
                    isPosting = isPosting,
                    errorMessage = postError
                )
            }

            entry<Profile> {
                ProfileScreen(
                    userSession = userSession,
                    onBack = { backStack.removeLastOrNull() },
                    onLogout = {
                        authManager.signOut()
                        backStack.clear()
                        backStack.add(Login)
                    },
                    onViewDataClick = {
                        // TODO: Open browser intent to user's Google Sheet URL
                    }
                )
            }
        },
    )
}
