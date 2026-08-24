package com.example.secondshelf.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.secondshelf.ui.model.Condition

private val conditionColors = mapOf(
    Condition.LIKE_NEW to Pair(Color(0xFF007AFF).copy(alpha = 0.15f), Color(0xFF007AFF)),
    Condition.GOOD to Pair(Color(0xFF34C759).copy(alpha = 0.15f), Color(0xFF34C759)),
    Condition.FAIR to Pair(Color(0xFFFF9500).copy(alpha = 0.15f), Color(0xFFFF9500))
)

@Composable
fun ConditionBadge(condition: Condition, modifier: Modifier = Modifier) {
    val (bg, fg) = conditionColors[condition] ?: conditionColors[Condition.GOOD]!!
    Text(
        text = condition.label,
        style = MaterialTheme.typography.labelSmall,
        fontSize = 10.sp,
        color = fg,
        modifier = modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}
