package com.cloudreel.mobile.ui.mesh

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
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Wifi
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
fun CellularBondingMeshScreen(
    onNavigateBack: () -> Unit
) {
    var isContributing by remember { mutableStateOf(true) }
    var backgroundAllowed by remember { mutableStateOf(true) }
    var batterySaverEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CloudReelTopBar(
            title = stringResource(R.string.mesh_title),
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
                isActive = isContributing,
                activeLabel = "Contributing",
                pausedLabel = "Paused",
                value = if (isContributing) "52.4 Mbps" else "0.0 Mbps",
                caption = "Contributed bandwidth · Bonded 5G + Wi-Fi"
            )

            ListSectionTitle("Supported sessions")
            ListGroup {
                ListRow(
                    title = if (isContributing) "2 active" else "0 active",
                    supporting = "CR-8492-HOST, Stage-1",
                    icon = Icons.Default.Hub
                )
            }

            ListSectionTitle("Bonded interfaces · 18 ms latency")
            ListGroup {
                ListRow(
                    title = "Cellular 5G SA",
                    supporting = "Primary SIM · Bonded",
                    icon = Icons.Default.CellTower,
                    trailing = { ListValue(if (isContributing) "24.1 Mbps" else "Standby") }
                )
                ListDivider()
                ListRow(
                    title = "Wi-Fi 6 uplink",
                    supporting = "5 GHz · Bonded",
                    icon = Icons.Default.Wifi,
                    trailing = { ListValue(if (isContributing) "18.4 Mbps" else "Standby") }
                )
                ListDivider()
                ListRow(
                    title = "Aggregated mesh channel",
                    supporting = "Optimized",
                    icon = Icons.Default.NetworkCheck,
                    trailing = { ListValue(if (isContributing) "142.5 Mbps" else "Idle") }
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
                ListDivider()
                ListRow(
                    title = "Background contribution",
                    icon = Icons.Default.DataUsage,
                    onClick = { backgroundAllowed = !backgroundAllowed },
                    trailing = { Switch(checked = backgroundAllowed, onCheckedChange = null) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        BottomActionBar {
            if (isContributing) {
                SecondaryButton(
                    text = "Pause Network Contribution",
                    onClick = { isContributing = false }
                )
            } else {
                GradientButton(
                    text = "Resume Network Contribution",
                    onClick = { isContributing = true }
                )
            }
        }
    }
}
