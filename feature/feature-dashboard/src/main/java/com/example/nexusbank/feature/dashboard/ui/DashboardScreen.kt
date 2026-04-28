package com.example.nexusbank.feature.dashboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.common.util.CurrencyUtils
import com.example.nexusbank.core.common.util.MaskUtils
import com.example.nexusbank.core.ui.theme.*

@Composable
fun DashboardScreen(
    onMenuClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSendMoneyClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onCardsClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onAccountClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
            IconButton(onClick = onMenuClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.menu),
                    contentDescription = "Menu",
                    modifier = Modifier.size(18.dp),
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
                Text("Nexus Bank", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            IconButton(onClick = onLogoutClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.logout),
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }

        // Welcome Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexusGreen)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "WELCOME, ${uiState.user?.fullName ?: "User"}",
                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White
            )
        }

        // Quick Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.send_money, label = "Send\nMoney", onClick = onSendMoneyClick, modifier = Modifier.weight(1f))
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.payment, label = "Payment", onClick = onPaymentClick, modifier = Modifier.weight(1f))
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.cards, label = "Cards", onClick = onCardsClick, modifier = Modifier.weight(1f))
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.more, label = "More", onClick = onMoreClick, modifier = Modifier.weight(1f))
        }

        HorizontalDivider(color = DividerDark)

        // Accounts section
        uiState.accounts.forEach { account ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgGray)
                    .clickable { onAccountClick(account.id) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(account.type.name.replace("_", " "), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        }
                    }
                }
                Text(
                    MaskUtils.maskAccountNumber(account.accountNumber),
                    fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(start = 22.dp)
                )
                Text(
                    CurrencyUtils.formatAmountPlain(account.balance),
                    fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(start = 22.dp)
                )
                Text("Available Balance", fontSize = 11.sp, color = TextLight, modifier = Modifier.padding(start = 22.dp))
            }
            HorizontalDivider(color = DividerDark)
        }
    }
}

@Composable
private fun QuickActionButton(icon: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(60.dp)
            .border(1.dp, BorderLight, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = NexusGreen
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Medium, color = TextDark, textAlign = TextAlign.Center, lineHeight = 11.sp, maxLines = 2)
    }
}
