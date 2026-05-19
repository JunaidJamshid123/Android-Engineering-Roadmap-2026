package com.example.nexusbank.feature.statement.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.statement.domain.model.Statement
import com.example.nexusbank.feature.statement.domain.model.StatementTxn
import com.example.nexusbank.feature.statement.domain.model.TxnDirection

private val DebitRed = Color(0xFFD32F2F)
private val CreditGreen = Color(0xFF2E7D32)

@Composable
fun StatementScreen(
    onBackClick: () -> Unit = {},
    viewModel: StatementViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is StatementEvent.FileDownloaded -> {
                    Toast.makeText(context, "Statement saved to Downloads/NexusBank", Toast.LENGTH_LONG).show()
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(event.uri, event.mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
                    }
                }
                is StatementEvent.ShowMessage ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    StatementContent(
        isLoading = state.isLoading,
        error = state.error,
        statement = state.statement,
        isDownloading = state.isDownloading,
        onBackClick = onBackClick,
        onRefresh = viewModel::refresh,
        onDownload = { viewModel.downloadStatement("pdf") }
    )
}

@Composable
private fun StatementContent(
    isLoading: Boolean,
    error: String?,
    statement: Statement?,
    isDownloading: Boolean,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onDownload: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        TopBar(
            onBackClick = onBackClick,
            onRefresh = onRefresh,
            onDownload = onDownload,
            isDownloading = isDownloading,
            canDownload = statement != null
        )
        SectionStrip("Account Statement")

        when {
            isLoading && statement == null -> CenterMessage { CircularProgressIndicator(color = NexusGreen) }
            error != null && statement == null -> CenterMessage {
                Text(error, fontSize = 13.sp, color = TextMedium)
            }
            statement == null -> CenterMessage {
                Text("No statement data", fontSize = 13.sp, color = TextMedium)
            }
            else -> StatementBody(statement)
        }
    }
}

@Composable
private fun StatementBody(s: Statement) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        AccountHeaderCard(s)
        PeriodRow(s.periodFrom, s.periodTo)
        BalanceStrip(s)
        SummaryCard(s)
        TransactionsList(s.transactions, s.account.currency)
    }
}

// ── Components ──

@Composable
private fun TopBar(
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onDownload: () -> Unit,
    isDownloading: Boolean,
    canDownload: Boolean
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
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Statement", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        IconButton(
            onClick = onDownload,
            enabled = canDownload && !isDownloading,
            modifier = Modifier.size(40.dp)
        ) {
            if (isDownloading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download PDF",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
        IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.size(20.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun SectionStrip(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(NexusGreen)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

@Composable
private fun AccountHeaderCard(s: Statement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = NexusGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                s.account.holderName,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                s.account.accountNumberMasked,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Chip(s.account.accountType)
                Spacer(Modifier.width(6.dp))
                Chip(s.account.currency)
                Spacer(Modifier.width(6.dp))
                Chip(s.account.status)
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            fontSize = 10.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PeriodRow(from: String, to: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
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
                    Icons.Default.DateRange,
                    null,
                    tint = NexusGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Period", fontSize = 11.sp, color = TextLight)
                Text(
                    "${formatDate(from)} → ${formatDate(to)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextDark
                )
            }
        }
    }
}

@Composable
private fun BalanceStrip(s: Statement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BalanceTile(
            label = "Opening",
            value = formatAmount(s.openingBalance, s.account.currency),
            modifier = Modifier.weight(1f)
        )
        BalanceTile(
            label = "Closing",
            value = formatAmount(s.closingBalance, s.account.currency),
            modifier = Modifier.weight(1f)
        )
        BalanceTile(
            label = "Current",
            value = formatAmount(s.currentBalance, s.account.currency),
            modifier = Modifier.weight(1f),
            highlight = true
        )
    }
}

@Composable
private fun BalanceTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight) NexusGreenLight else BgWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, color = TextLight)
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (highlight) NexusGreenDark else TextDark
            )
        }
    }
}

@Composable
private fun SummaryCard(s: Statement) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Summary",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextLight,
                letterSpacing = 0.6.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
            SummaryRow(
                icon = Icons.Default.ArrowDownward,
                tint = CreditGreen,
                label = "Total Credits",
                value = "+ ${formatAmount(s.summary.totalCredits, s.account.currency)}"
            )
            SummaryRow(
                icon = Icons.Default.ArrowUpward,
                tint = DebitRed,
                label = "Total Debits",
                value = "− ${formatAmount(s.summary.totalDebits, s.account.currency)}"
            )
            HorizontalDivider(
                color = DividerColor,
                thickness = 0.6.dp,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Transactions", fontSize = 12.sp, color = TextMedium)
                Text(
                    s.summary.count.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Net", fontSize = 12.sp, color = TextMedium)
                Text(
                    text = (if (s.summary.net >= 0) "+ " else "− ") +
                            formatAmount(kotlin.math.abs(s.summary.net), s.account.currency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (s.summary.net >= 0) CreditGreen else DebitRed
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 12.sp, color = TextMedium, modifier = Modifier.weight(1f))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

@Composable
private fun TransactionsList(transactions: List<StatementTxn>, currency: String) {
    Text(
        text = "TRANSACTIONS",
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextLight,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
    )

    if (transactions.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BgWhite)
        ) {
            Text(
                "No transactions in this period.",
                fontSize = 13.sp,
                color = TextMedium,
                modifier = Modifier.padding(20.dp)
            )
        }
        return
    }

    // Group by day (yyyy-MM-dd)
    val grouped = transactions.groupBy { it.date.substringBefore('T') }
    grouped.forEach { (day, items) ->
        Text(
            formatDate(day),
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = TextMedium,
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, bottom = 6.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BgWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                items.forEachIndexed { idx, txn ->
                    TxnRow(txn, currency)
                    if (idx != items.lastIndex) {
                        HorizontalDivider(
                            color = DividerColor,
                            thickness = 0.6.dp,
                            modifier = Modifier.padding(start = 66.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TxnRow(txn: StatementTxn, currency: String) {
    val isCredit = txn.direction == TxnDirection.CREDIT
    val color = if (isCredit) CreditGreen else DebitRed
    val signed = (if (isCredit) "+ " else "− ") + formatAmount(txn.amount, currency)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                txn.counterpartyName ?: txn.purpose ?: "Transaction",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = TextDark
            )
            Spacer(Modifier.height(2.dp))
            val sub = listOfNotNull(
                txn.purpose?.takeIf { it.isNotBlank() }?.let { it.lowercase().replaceFirstChar(Char::uppercase) },
                txn.counterpartyAccountMasked?.takeIf { it.isNotBlank() }
            ).joinToString(" • ")
            if (sub.isNotBlank()) {
                Text(sub, fontSize = 11.sp, color = TextLight)
            }
            if (!txn.remarks.isNullOrBlank()) {
                Text(txn.remarks, fontSize = 11.sp, color = TextLight)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(signed, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = color)
            Text(
                "Bal: ${formatAmount(txn.runningBalance, currency)}",
                fontSize = 10.sp,
                color = TextLight
            )
        }
    }
}

@Composable
private fun CenterMessage(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ── Helpers ──

private fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val datePart = raw.substringBefore('T')
    val parts = datePart.split('-')
    if (parts.size != 3) return raw
    val (y, m, d) = parts
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val month = m.toIntOrNull()?.let { months.getOrNull(it - 1) } ?: m
    return "$d $month $y"
}

private fun formatAmount(value: Double, currency: String): String {
    val whole = value.toLong()
    val frac = kotlin.math.abs((value - whole) * 100).toLong()
    val grouped = whole.toString().reversed().chunked(3).joinToString(",").reversed()
    return if (frac == 0L) "$currency $grouped" else "$currency $grouped.${frac.toString().padStart(2, '0')}"
}
