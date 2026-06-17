package com.example.hipenter.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hipenter.auth.GoogleAuthManager
import com.example.hipenter.data.DataRepository
import com.example.hipenter.network.models.Post
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class FeedUiState(
    val posts: List<Post> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val requiresLogin: Boolean = false
)

class MainScreenViewModel(
    private val dataRepository: DataRepository,
    private val authManager: GoogleAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState(isLoading = true))
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state. If they log out, we flip requiresLogin
        viewModelScope.launch {
            authManager.userSession.collect { session ->
                if (session == null) {
                    _uiState.update { it.copy(requiresLogin = true) }
                } else {
                    _uiState.update { it.copy(requiresLogin = false) }
                    loadFeed()
                }
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            dataRepository.getFeed()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Failed to load feed") }
                }
                .collect { posts ->
                    // Reverse to show newest first
                    val sorted = posts.sortedByDescending { it.timestamp }
                    _uiState.update { it.copy(posts = sorted, isLoading = false, error = null) }
                }
        }
    }
}
