package com.example.hipenter.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.hipenter.theme.ErrorRed
import com.example.hipenter.theme.Teal
import com.example.hipenter.theme.TextSecondary
import com.example.hipenter.theme.WarningAmber

@Composable
fun WordCounter(
    currentWords: Int,
    maxWords: Int = 15,
    modifier: Modifier = Modifier
) {
    val progress = (currentWords.toFloat() / maxWords.toFloat()).coerceIn(0f, 1f)
    val remaining = maxWords - currentWords
    
    val animatedProgress by animateFloatAsState(targetValue = progress)
    
    val targetColor = when {
        remaining < 0 -> ErrorRed
        remaining <= 3 -> WarningAmber
        else -> Teal
    }
    
    val animatedColor by animateColorAsState(targetValue = targetColor)

    Box(
        modifier = modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            color = TextSecondary.copy(alpha = 0.2f),
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
        
        CircularProgressIndicator(
            progress = { animatedProgress },
            color = animatedColor,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )
        
        Text(
            text = remaining.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (remaining < 0) ErrorRed else TextSecondary
        )
    }
}

// Extension to count words properly
fun String.countWords(): Int {
    return this.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
}
