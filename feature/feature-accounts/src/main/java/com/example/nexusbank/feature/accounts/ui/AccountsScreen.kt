package com.example.nexusbank.feature.accounts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.common.util.CurrencyUtils
import com.example.nexusbank.core.common.util.MaskUtils
import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.ui.components.NexusErrorView
import com.example.nexusbank.core.ui.components.NexusLoadingIndicator
import com.example.nexusbank.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onBackClick: () -> Unit = {},
    onAccountClick: (String) -> Unit = {},
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Accounts") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NexusGreen,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> NexusLoadingIndicator(modifier = Modifier.padding(padding))
            uiState.error != null -> NexusErrorView(
                message = uiState.error ?: "Unknown error",
                onRetry = { viewModel.loadAccounts() },
                modifier = Modifier.padding(padding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.accounts) { account ->
                        AccountCard(account = account, onClick = { onAccountClick(account.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountCard(account: Account, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NexusGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.AccountBalance, contentDescription = null, tint = NexusGreen, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.type.name.replace("_", " "), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(text = MaskUtils.maskAccountNumber(account.accountNumber), fontSize = 12.sp, color = TextMedium)
            }
            Text(
                text = CurrencyUtils.formatAmount(account.balance, account.currency),
                fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NexusGreen
            )
        }
    }
}
