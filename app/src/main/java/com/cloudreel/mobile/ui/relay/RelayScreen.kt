package com.cloudreel.mobile.ui.relay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.cloudreel.mobile.R
import com.cloudreel.mobile.ui.components.BottomActionBar
import com.cloudreel.mobile.ui.components.CloudReelTopBar
import com.cloudreel.mobile.ui.components.GradientButton
import com.cloudreel.mobile.ui.components.ListDivider
import com.cloudreel.mobile.ui.components.ListGroup
import com.cloudreel.mobile.ui.components.ListRow
import com.cloudreel.mobile.ui.components.ListSectionTitle
import com.cloudreel.mobile.ui.components.ListValue
import com.cloudreel.mobile.ui.components.SecondaryButton
import com.cloudreel.mobile.ui.components.StatusHeader

@Composable
fun RelayScreen(
    onNavigateBack: () -> Unit
) {
    var isRelaying by remember { mutableStateOf(true) }
    var batterySaverEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CloudReelTopBar(
            title = stringResource(R.string.relay_title),
            onBackClick = onNavigateBack
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            StatusHeader(
                isActive = isRelaying,
                activeLabel = "Relaying",
                pausedLabel = "Paused",
                value = if (isRelaying) "48.2 Mbps" else "0.0 Mbps",
                caption = "Bandwidth contributed · Bonded 5G Sub-6"
            )

            ListSectionTitle("Connection")
            ListGroup {
                ListRow(
                    title = "Primary SIM 1 (5G SA)",
                    supporting = "Signal -82 dBm · Excellent",
                    icon = Icons.Default.CellTower
                )
                ListDivider()
                ListRow(
                    title = "Data donated",
                    supporting = "This session",
                    icon = Icons.Default.DataUsage,
                    trailing = { ListValue("1.84 GB") }
                )
            }

            ListSectionTitle("Settings")
            ListGroup {
                ListRow(
                    title = "Thermal & battery throttling",
                    icon = Icons.Default.BatteryChargingFull,
                    onClick = { batterySaverEnabled = !batterySaverEnabled },
                    trailing = { Switch(checked = batterySaverEnabled, onCheckedChange = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        BottomActionBar {
            if (isRelaying) {
                SecondaryButton(
                    text = "Pause Connection Donation",
                    onClick = { isRelaying = false }
                )
            } else {
                GradientButton(
                    text = "Resume Cellular Donation",
                    onClick = { isRelaying = true }
                )
            }
        }
    }
}
