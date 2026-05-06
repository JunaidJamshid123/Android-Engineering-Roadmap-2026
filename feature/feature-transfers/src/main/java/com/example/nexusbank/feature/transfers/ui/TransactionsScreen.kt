package com.example.nexusbank.feature.transfers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexusbank.core.network.model.TransferHistoryItem
import com.example.nexusbank.core.ui.components.ErrorDialog
import com.example.nexusbank.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Composable
fun TransactionsScreen(
    onBackClick: () -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.error != null) {
        ErrorDialog(
            message = state.error.orEmpty(),
            onDismiss = viewModel::clearError
        )
    }

    Scaffold(
        containerColor = BgGray,
        topBar = { TransfersTopBar(title = "Transactions", onBackClick = onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NexusGreen,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                state.items.isEmpty() && state.error == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = TextLight,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "No transactions yet",
                                fontSize = 14.sp,
                                color = TextLight
                            )
                        }
                    }
                }
                else -> {
                    val grouped = remember(state.items) {
                        state.items.groupBy { item ->
                            formatGroupDate(item.completedAt ?: item.createdAt)
                        }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${state.total} transaction${if (state.total == 1) "" else "s"}",
                                fontSize = 11.sp,
                                color = TextLight,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        grouped.forEach { (day, txns) ->
                            item(key = "header-$day") {
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextMedium,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(txns, key = { it.id }) { txn ->
                                TransactionRow(txn)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(txn: TransferHistoryItem) {
    val isCredit = txn.direction.equals("CREDIT", ignoreCase = true)
    val tint = if (isCredit) NexusGreen else Color(0xFFE53935)
    val sign = if (isCredit) "+" else "-"
    val name = txn.counterpartyName?.takeIf { it.isNotBlank() } ?: "Transfer"
    val account = txn.counterpartyAccountNumberMasked.orEmpty()
    val time = formatRowTime(txn.completedAt ?: txn.createdAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = buildString {
                    if (account.isNotBlank()) append(account)
                    if (!txn.purpose.isNullOrBlank()) {
                        if (account.isNotBlank()) append(" • ")
                        append(
                            txn.purpose!!.lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    }
                },
                fontSize = 11.sp,
                color = TextLight,
                maxLines = 1
            )
            if (!txn.remarks.isNullOrBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "“${txn.remarks}”",
                    fontSize = 11.sp,
                    color = TextLight,
                    maxLines = 1
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$sign ${txn.currency} ${txn.amount}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = time,
                fontSize = 10.sp,
                color = TextLight
            )
        }
    }
}

private fun parseIso(value: String?): Long? {
    if (value.isNullOrBlank()) return null
    return runCatching {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        fmt.parse(value)?.time
    }.getOrNull() ?: runCatching {
        val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        fmt.parse(value)?.time
    }.getOrNull()
}

private fun formatGroupDate(value: String?): String {
    val ts = parseIso(value) ?: return "—"
    val fmt = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
    return fmt.format(java.util.Date(ts))
}

private fun formatRowTime(value: String?): String {
    val ts = parseIso(value) ?: return ""
    val fmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    return fmt.format(java.util.Date(ts))
}
