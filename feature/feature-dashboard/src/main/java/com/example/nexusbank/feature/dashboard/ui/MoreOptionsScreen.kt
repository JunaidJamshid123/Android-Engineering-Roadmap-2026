package com.example.nexusbank.feature.dashboard.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbank.core.ui.theme.*

private data class MoreOption(
    val label: String,
    @DrawableRes val icon: Int,
    val key: String
)

private val moreOptions = listOf(
    MoreOption("Profile", com.example.nexusbank.core.ui.R.drawable.profile, "profile"),
    MoreOption("Statements", com.example.nexusbank.core.ui.R.drawable.statement, "statements"),
    MoreOption("Transactions", com.example.nexusbank.core.ui.R.drawable.transactions, "transactions"),
    MoreOption("Beneficiaries", com.example.nexusbank.core.ui.R.drawable.beneficiaries, "beneficiaries"),
    MoreOption("KYC Verification", com.example.nexusbank.core.ui.R.drawable.verification, "verification"),
    MoreOption("Notifications", com.example.nexusbank.core.ui.R.drawable.notification, "notifications"),
    MoreOption("Security", com.example.nexusbank.core.ui.R.drawable.security, "security"),
    MoreOption("Help & Support", com.example.nexusbank.core.ui.R.drawable.help, "help"),
    MoreOption("About", com.example.nexusbank.core.ui.R.drawable.about, "about"),
)

@Composable
@Preview
fun MoreOptionsScreen(
    onBackClick: () -> Unit = {},
    onOptionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        // Top Bar
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
                    "Nexus Bank",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            // Spacer to balance the back button
            Spacer(modifier = Modifier.size(40.dp))
        }

        // Header strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexusGreen)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "More Options",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        // Grid of options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            moreOptions.chunked(3).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { option ->
                        MoreOptionCard(
                            icon = option.icon,
                            label = option.label,
                            onClick = { onOptionClick(option.key) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining space if row has fewer than 3 items
                    repeat(3 - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreOptionCard(
    @DrawableRes icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(BgWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(32.dp),
            tint = NexusGreen
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            maxLines = 2
        )
    }
}
