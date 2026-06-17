package com.example.hipenter.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hipenter.theme.*
import com.example.hipenter.ui.components.WordCounter
import com.example.hipenter.ui.components.countWords

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComposePostScreen(
    onPost: (String) -> Unit,
    onCancel: () -> Unit,
    isPosting: Boolean = false,
    errorMessage: String? = null
) {
    var content by remember { mutableStateOf("") }
    val wordCount = content.countWords()
    val isOverLimit = wordCount > 15
    val canPost = content.isNotBlank() && !isOverLimit && !isPosting

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavySurface)
            .padding(top = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = TextSecondary)
                }
                
                Button(
                    onClick = { onPost(content) },
                    enabled = canPost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Teal,
                        disabledContainerColor = TealDark.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    if (isPosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Post", color = TextOnTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error Message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Input Field
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text(
                        "What's on your mind? (15 words max)", 
                        color = TextSecondary.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.headlineSmall
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Teal,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                textStyle = MaterialTheme.typography.headlineSmall,
                enabled = !isPosting
            )

            // Bottom bar with word counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WordCounter(currentWords = wordCount, maxWords = 15)
            }
        }
    }
}
