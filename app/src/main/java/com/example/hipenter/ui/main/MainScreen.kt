package com.example.hipenter.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.hipenter.theme.*
import com.example.hipenter.ui.components.PostCard
import com.example.hipenter.Login
import com.example.hipenter.ComposePost
import com.example.hipenter.Profile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Automatically redirect to login if not authenticated
    if (uiState.requiresLogin) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            onItemClick(Login)
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = NavyDark,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Feed", color = TealLight, fontWeight = FontWeight.ExtraBold) 
                },
                actions = {
                    IconButton(onClick = { onItemClick(Profile) }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NavyDark,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onItemClick(ComposePost) },
                containerColor = Coral,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Post")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Teal
                )
            } else if (uiState.error != null && uiState.posts.isEmpty()) {
                Text(
                    text = uiState.error!!,
                    color = ErrorRed,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(uiState.posts, key = { it.id }) { post ->
                        PostCard(
                            post = post,
                            onSheetClick = { url -> /* TODO: Open intent to browser */ }
                        )
                    }
                    
                    if (uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = Teal, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
