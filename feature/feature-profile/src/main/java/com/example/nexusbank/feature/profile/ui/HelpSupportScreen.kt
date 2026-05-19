package com.example.nexusbank.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.profile.ui.components.*

@Composable
@Preview
fun HelpSupportScreen(
    onBackClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onLiveChatClick: () -> Unit = {},
    onCallUsClick: () -> Unit = {},
    onReportIssueClick: () -> Unit = {},
    onRateAppClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onAboutClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileTopBar(title = "Help & Support", onBackClick = onBackClick)
        SectionStrip("We're here to help")

        // Hero contact card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = NexusGreen)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "24/7 Customer Support",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Talk to our team anytime, anywhere.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HeroAction(
                        icon = Icons.Default.Phone,
                        label = "Call",
                        modifier = Modifier.weight(1f),
                        onClick = onCallUsClick
                    )
                    HeroAction(
                        icon = Icons.AutoMirrored.Filled.Chat,
                        label = "Chat",
                        modifier = Modifier.weight(1f),
                        onClick = onLiveChatClick
                    )
                }
            }
        }

        GroupLabel("Get Help")
        GroupCard {
            SettingsRow(
                icon = Icons.Default.QuestionAnswer,
                title = "FAQs",
                subtitle = "Find quick answers",
                onClick = onFaqClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "Live Chat",
                subtitle = "Chat with an agent",
                onClick = onLiveChatClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.BugReport,
                title = "Report an Issue",
                subtitle = "Let us know what's wrong",
                onClick = onReportIssueClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Star,
                title = "Rate the App",
                subtitle = "Share your experience",
                onClick = onRateAppClick
            )
        }

        GroupLabel("Legal")
        GroupCard {
            SettingsRow(
                icon = Icons.Default.Description,
                title = "Terms & Conditions",
                onClick = onTermsClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy Policy",
                onClick = onPrivacyClick
            )
            RowDivider()
            SettingsRow(
                icon = Icons.Default.Info,
                title = "About Nexus Bank",
                trailingText = "v1.0.0",
                onClick = onAboutClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HeroAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.18f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = NexusGreen, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
