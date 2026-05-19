package com.example.nexusbank.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.profile.ui.components.*

@Composable
@Preview
fun SecurityScreen(
    onBackClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onChangePinClick: () -> Unit = {},
    onTrustedDevicesClick: () -> Unit = {},
    onLoginHistoryClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {}
) {
    var biometricEnabled by remember { mutableStateOf(true) }
    var twoFactorEnabled by remember { mutableStateOf(true) }
    var appLockEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileTopBar(title = "Security", onBackClick = onBackClick)
        SectionStrip("Security Settings")

        GroupLabel("Authentication")
        GroupCard {
            SettingsRow(
                icon = Icons.Default.Password,
                title = "Change Password",
                subtitle = "Last changed 3 months ago",
                onClick = onChangePasswordClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Change Transaction PIN",
                subtitle = "4-digit PIN for transactions",
                onClick = onChangePinClick
            )
        }

        GroupLabel("Quick Sign-in")
        GroupCard {
            ToggleRow(
                icon = Icons.Default.Fingerprint,
                title = "Biometric Login",
                subtitle = "Fingerprint or Face ID",
                checked = biometricEnabled,
                onCheckedChange = { biometricEnabled = it }
            )
            RowDivider()
            ToggleRow(
                icon = Icons.Default.VerifiedUser,
                title = "Two-Factor Authentication",
                subtitle = "OTP for sensitive actions",
                checked = twoFactorEnabled,
                onCheckedChange = { twoFactorEnabled = it }
            )
            RowDivider()
            ToggleRow(
                icon = Icons.Default.Lock,
                title = "App Lock",
                subtitle = "Require auth on app open",
                checked = appLockEnabled,
                onCheckedChange = { appLockEnabled = it }
            )
        }

        GroupLabel("Activity")
        GroupCard {
            SettingsRow(
                icon = Icons.Default.Devices,
                title = "Trusted Devices",
                subtitle = "2 devices signed in",
                onClick = onTrustedDevicesClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.History,
                title = "Login History",
                subtitle = "View recent activity",
                onClick = onLoginHistoryClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Settings",
                subtitle = "Data sharing & permissions",
                onClick = onPrivacyClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
