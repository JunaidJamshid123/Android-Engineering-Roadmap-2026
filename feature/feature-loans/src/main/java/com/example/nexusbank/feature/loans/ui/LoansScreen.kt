package com.example.nexusbank.feature.loans.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.common.util.CurrencyUtils
import com.example.nexusbank.core.common.util.DateUtils
import com.example.nexusbank.core.domain.model.Loan
import com.example.nexusbank.core.ui.components.NexusErrorView
import com.example.nexusbank.core.ui.components.NexusLoadingIndicator
import com.example.nexusbank.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    onBackClick: () -> Unit = {},
    viewModel: LoansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Loans") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NexusGreen, titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> NexusLoadingIndicator(modifier = Modifier.padding(padding))
            uiState.error != null -> NexusErrorView(message = uiState.error ?: "", onRetry = { viewModel.loadLoans() }, modifier = Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.loans) { loan -> LoanCard(loan) }
            }
        }
    }
}

@Composable
private fun LoanCard(loan: Loan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(loan.type.name.replace("_", " ") + " Loan", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(loan.status.name, fontSize = 12.sp, color = if (loan.status.name == "ACTIVE") NexusGreen else TextLight)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LoanInfoRow("Principal", CurrencyUtils.formatAmountPlain(loan.principalAmount))
            LoanInfoRow("Outstanding", CurrencyUtils.formatAmountPlain(loan.outstandingAmount))
            LoanInfoRow("EMI", CurrencyUtils.formatAmountPlain(loan.emiAmount))
            LoanInfoRow("Rate", "${loan.interestRate}%")
            loan.nextEmiDate?.let { LoanInfoRow("Next EMI", DateUtils.formatDate(it)) }
        }
    }
}

@Composable
private fun LoanInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextMedium)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}
