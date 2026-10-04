package com.vraidev.acousticdriver.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vraidev.acousticdriver.ui.theme.*

@Composable
fun CompatibilityBadge(
    percentage: Int,
    isActive: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    if (isActive) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(CyberCyan.copy(alpha = 0.2f))
                .border(1.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(CyberCyan)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "ACTIVE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan
            )
        }
    } else {
        val color = when {
            percentage >= 85 -> CyberCyan
            percentage >= 70 -> NeonMint
            percentage >= 50 -> AmberWarning
            else -> AccentRed
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f))
                .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                .clickable(enabled = onClick != null) { onClick?.invoke() }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$percentage% DSP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
