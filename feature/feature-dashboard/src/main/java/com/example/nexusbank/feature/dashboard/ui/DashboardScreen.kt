package com.example.nexusbank.feature.dashboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.common.util.CurrencyUtils
import com.example.nexusbank.core.common.util.MaskUtils
import com.example.nexusbank.core.ui.components.LogoutConfirmationDialog
import com.example.nexusbank.core.ui.theme.*
import kotlinx.coroutines.launch

@Composable
@Preview
fun DashboardScreen(
    onMenuClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onSendMoneyClick: () -> Unit = {},
    onPaymentClick: () -> Unit = {},
    onCardsClick: () -> Unit = {},
    onMoreClick: () -> Unit = {},
    onAccountClick: (String) -> Unit = {},
    onDrawerProfileClick: () -> Unit = {},
    onDrawerStatementsClick: () -> Unit = {},
    onDrawerTransactionsClick: () -> Unit = {},
    onDrawerBeneficiariesClick: () -> Unit = {},
    onDrawerVerificationClick: () -> Unit = {},
    onDrawerNotificationsClick: () -> Unit = {},
    onDrawerSecurityClick: () -> Unit = {},
    onDrawerHelpClick: () -> Unit = {},
    onDrawerAboutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                userName = uiState.user?.fullName ?: "User",
                userEmail = uiState.user?.email ?: "",
                onSendMoneyClick = {
                    scope.launch { drawerState.close() }
                    onSendMoneyClick()
                },
                onPaymentClick = {
                    scope.launch { drawerState.close() }
                    onPaymentClick()
                },
                onCardsClick = {
                    scope.launch { drawerState.close() }
                    onCardsClick()
                },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    onDrawerProfileClick()
                },
                onStatementsClick = {
                    scope.launch { drawerState.close() }
                    onDrawerStatementsClick()
                },
                onTransactionsClick = {
                    scope.launch { drawerState.close() }
                    onDrawerTransactionsClick()
                },
                onBeneficiariesClick = {
                    scope.launch { drawerState.close() }
                    onDrawerBeneficiariesClick()
                },
                onVerificationClick = {
                    scope.launch { drawerState.close() }
                    onDrawerVerificationClick()
                },
                onNotificationsClick = {
                    scope.launch { drawerState.close() }
                    onDrawerNotificationsClick()
                },
                onSecurityClick = {
                    scope.launch { drawerState.close() }
                    onDrawerSecurityClick()
                },
                onHelpClick = {
                    scope.launch { drawerState.close() }
                    onDrawerHelpClick()
                },
                onAboutClick = {
                    scope.launch { drawerState.close() }
                    onDrawerAboutClick()
                },
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    showLogoutDialog = true
                }
            )
        }
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
            IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.size(40.dp)
            ) {
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
            IconButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.logout),
                    contentDescription = "Logout",
                    modifier = Modifier.size(20.dp),
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
                .background(BgGray)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.send_money, label = "Send\nMoney", onClick = onSendMoneyClick, modifier = Modifier.weight(1f))
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.payment, label = "Payment", onClick = onPaymentClick, modifier = Modifier.weight(1f))
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.cards, label = "Cards", onClick = onCardsClick, modifier = Modifier.weight(1f))
            QuickActionButton(icon = com.example.nexusbank.core.ui.R.drawable.more, label = "More", onClick = onMoreClick, modifier = Modifier.weight(1f))
        }

        // "What I Have" header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgGray)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "What I Have",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    modifier = Modifier.size(14.dp),
                    tint = TextLight
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PKR",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
        }

        HorizontalDivider(color = DividerDark)

        // Accounts section
        uiState.accounts.forEach { account ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgGray)
                    .clickable { onAccountClick(account.id) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Account details
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deposit Account", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.padding(start = 22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.user?.fullName ?: "User",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = MaskUtils.maskAccountNumber(account.accountNumber),
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        CurrencyUtils.formatAmountPlain(account.balance),
                        fontSize = 30.sp, fontWeight = FontWeight.Bold, color = NexusGreen,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Available Balance",
                        fontSize = 11.sp, color = NexusGreen,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                // QR Code icon
                Icon(
                    painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.qr_code),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .size(28.dp)
                        .padding(top = 4.dp),
                    tint = NexusGreen
                )
            }
            HorizontalDivider(color = DividerDark)
        }
    }
    } // ModalNavigationDrawer
}

@Composable
private fun DrawerContent(
    userName: String,
    userEmail: String,
    onSendMoneyClick: () -> Unit,
    onPaymentClick: () -> Unit,
    onCardsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onStatementsClick: () -> Unit,
    onTransactionsClick: () -> Unit,
    onBeneficiariesClick: () -> Unit,
    onVerificationClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onHelpClick: () -> Unit,
    onAboutClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp),
        drawerContainerColor = BgWhite
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexusGreenDark)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NexusGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userName.firstOrNull()?.uppercase() ?: "U",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            if (userEmail.isNotEmpty()) {
                Text(
                    text = userEmail,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.send_money,
            label = "Send Money",
            onClick = onSendMoneyClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.payment,
            label = "Payment",
            onClick = onPaymentClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.cards,
            label = "Cards",
            onClick = onCardsClick
        )

        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))

        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.profile,
            label = "Profile",
            onClick = onProfileClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.statement,
            label = "Statements",
            onClick = onStatementsClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.transactions,
            label = "Transactions",
            onClick = onTransactionsClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.beneficiaries,
            label = "Beneficiaries",
            onClick = onBeneficiariesClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.verification,
            label = "KYC Verification",
            onClick = onVerificationClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.notification,
            label = "Notifications",
            onClick = onNotificationsClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.security,
            label = "Security",
            onClick = onSecurityClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.help,
            label = "Help & Support",
            onClick = onHelpClick
        )
        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.about,
            label = "About",
            onClick = onAboutClick
        )

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = DividerColor)

        DrawerItem(
            icon = com.example.nexusbank.core.ui.R.drawable.logout,
            label = "Logout",
            onClick = onLogoutClick,
            tint = Color(0xFFD32F2F)
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DrawerItem(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    tint: Color = NexusGreen
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(22.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextDark
        )
    }
}

@Composable
private fun QuickActionButton(icon: Int, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(BgWhite)
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
