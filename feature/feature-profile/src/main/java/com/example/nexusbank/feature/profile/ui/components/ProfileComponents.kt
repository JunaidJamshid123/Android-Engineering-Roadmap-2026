package com.example.nexusbank.feature.profile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbank.core.ui.theme.*

/**
 * Standard Nexus Bank top bar (dark green status bar area + back + centered logo/title).
 */
@Composable
fun ProfileTopBar(
    title: String,
    onBackClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NexusGreenDark)
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.nexus_app_icon),
                contentDescription = "Nexus Bank",
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        if (trailing != null) trailing() else Spacer(modifier = Modifier.size(40.dp))
    }
}

/**
 * Green section header strip (matches "More Options" / "Welcome" strip).
 */
@Composable
fun SectionStrip(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NexusGreen)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

/**
 * Small uppercase group label shown above a card group.
 */
@Composable
fun GroupLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextLight,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
    )
}

/**
 * Container card used to group related rows.
 */
@Composable
fun GroupCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

/**
 * Clickable settings row — icon | label (subtitle) | chevron.
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    showChevron: Boolean = true,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NexusGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NexusGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextLight
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                fontSize = 13.sp,
                color = TextMedium,
                modifier = Modifier.padding(end = 6.dp)
            )
        }
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Row with a trailing Switch toggle.
 */
@Composable
fun ToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NexusGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NexusGreen,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = TextLight)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NexusGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = DividerDark
            )
        )
    }
}

/**
 * Read-only label + value row used in info screens.
 */
@Composable
fun InfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = label, fontSize = 12.sp, color = TextLight)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Thin divider used between rows inside a GroupCard.
 */
@Composable
fun RowDivider() {
    HorizontalDivider(
        color = DividerColor,
        thickness = 0.6.dp,
        modifier = Modifier.padding(start = 66.dp)
    )
}
