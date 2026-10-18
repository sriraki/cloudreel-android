package com.cloudreel.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cloudreel.mobile.theme.BadgeBackgroundDark
import com.cloudreel.mobile.theme.BadgeBackgroundLight
import com.cloudreel.mobile.theme.BadgeBorderDark
import com.cloudreel.mobile.theme.BadgeBorderLight
import com.cloudreel.mobile.theme.BadgeTextDark
import com.cloudreel.mobile.theme.BadgeTextLight
import com.cloudreel.mobile.theme.LocalThemeController

@Composable
fun EnterprisePlatformBadge(
    modifier: Modifier = Modifier,
    text: String = "✦ ENTERPRISE BROADCAST PLATFORM"
) {
    val isDark = LocalThemeController.current.isDark

    val bgColor = if (isDark) BadgeBackgroundDark else BadgeBackgroundLight
    val borderColor = if (isDark) BadgeBorderDark else BadgeBorderLight
    val textColor = if (isDark) BadgeTextDark else BadgeTextLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}
