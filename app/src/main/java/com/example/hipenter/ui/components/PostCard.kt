package com.example.hipenter.ui.components

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hipenter.network.models.Post
import com.example.hipenter.theme.*

@Composable
fun PostCard(
    post: Post,
    onSheetClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar placeholder (Would use Coil in full implementation)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Teal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.author.firstOrNull()?.toString()?.uppercase() ?: "?",
                        color = TextOnTeal,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    
                    val timeAgo = DateUtils.getRelativeTimeSpanString(
                        post.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    )
                    
                    Text(
                        text = timeAgo.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                // Link to user sheet
                if (!post.userSheetUrl.isNullOrEmpty()) {
                    IconButton(onClick = { onSheetClick(post.userSheetUrl) }) {
                        Text("📊", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
        }
    }
}
