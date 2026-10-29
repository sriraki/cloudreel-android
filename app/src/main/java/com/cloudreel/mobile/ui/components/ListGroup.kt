package com.cloudreel.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.theme.CRViolet
import com.cloudreel.mobile.theme.SuccessGreen
import com.cloudreel.mobile.theme.forTheme

/** Rounded block of list rows, like Android Settings. Put ListDivider() between rows. */
@Composable
fun ListGroup(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        content = content
    )
}

/** Small heading above a ListGroup. */
@Composable
fun ListSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

/** Divider inset past the row icon, so the icons read as one column. */
@Composable
fun ListDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    )
}

/**
 * One row in a ListGroup. Tappable rows show a chevron unless they pass their own trailing
 * content (a value or a switch).
 */
@Composable
fun ListRow(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    supporting: String? = null,
    tag: String? = null,
    accentColor: Color = CRViolet,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val accent = accentColor.forTheme()
    ListItem(
        modifier = if (onClick != null) modifier.clickable(role = Role.Button, onClick = onClick) else modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        overlineContent = tag?.let { { Text(it, color = accent) } },
        headlineContent = { Text(title, style = MaterialTheme.typography.titleMedium) },
        supportingContent = supporting?.let { { Text(it) } },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
            }
        },
        trailingContent = trailing ?: if (onClick != null) {
            {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else null
    )
}

/** Value shown at the end of a ListRow. */
@Composable
fun ListValue(text: String) {
    Text(text, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
}

/** Top-of-screen status for Relay and Network contribution: state and number on one line, then what the number means. */
@Composable
fun StatusHeader(
    isActive: Boolean,
    activeLabel: String,
    pausedLabel: String,
    value: String,
    caption: String
) {
    val stateColor = if (isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier = Modifier.padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(stateColor))
            Text(
                text = if (isActive) activeLabel else pausedLabel,
                style = MaterialTheme.typography.titleSmall,
                color = stateColor,
                modifier = Modifier.weight(1f)
            )
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        }
        Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
