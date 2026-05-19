package com.example.nexusbank.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Vibration
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
fun AppPreferencesScreen(
    onBackClick: () -> Unit = {},
    onThemeClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {}
) {
    var darkMode by remember { mutableStateOf(false) }
    var pushNotifs by remember { mutableStateOf(true) }
    var emailNotifs by remember { mutableStateOf(true) }
    var smsNotifs by remember { mutableStateOf(false) }
    var vibration by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileTopBar(title = "App Preferences", onBackClick = onBackClick)
        SectionStrip("App Preferences")

        GroupLabel("Appearance")
        GroupCard {
            SettingsRow(
                icon = Icons.Default.Palette,
                title = "Theme",
                subtitle = "Choose app theme",
                trailingText = "System",
                onClick = onThemeClick
            )
            RowDivider()
            ToggleRow(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Override system theme",
                checked = darkMode,
                onCheckedChange = { darkMode = it }
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = "App display language",
                trailingText = "English",
                onClick = onLanguageClick
            )
        }

        GroupLabel("Notifications")
        GroupCard {
            ToggleRow(
                icon = Icons.Default.Notifications,
                title = "Push Notifications",
                subtitle = "Transaction & promo alerts",
                checked = pushNotifs,
                onCheckedChange = { pushNotifs = it }
            )
            RowDivider()
            ToggleRow(
                icon = Icons.Default.Email,
                title = "Email Notifications",
                subtitle = "Statements & receipts",
                checked = emailNotifs,
                onCheckedChange = { emailNotifs = it }
            )
            RowDivider()
            ToggleRow(
                icon = Icons.Default.Sms,
                title = "SMS Alerts",
                subtitle = "OTP & transaction SMS",
                checked = smsNotifs,
                onCheckedChange = { smsNotifs = it }
            )
            RowDivider()
            ToggleRow(
                icon = Icons.Default.Vibration,
                title = "Vibration",
                subtitle = "Vibrate on alerts",
                checked = vibration,
                onCheckedChange = { vibration = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
