package com.example.nexusbank.feature.investments.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbank.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investments") },
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InvestmentCategory("Fixed Deposits", "Guaranteed returns with flexible tenures", "6.5% - 7.5% p.a.")
            }
            item {
                InvestmentCategory("Recurring Deposits", "Build savings with monthly contributions", "6.0% - 7.0% p.a.")
            }
            item {
                InvestmentCategory("Mutual Funds", "Diversified portfolio investments", "Market-linked returns")
            }
        }
    }
}

@Composable
private fun InvestmentCategory(title: String, description: String, returns: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BgWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = NexusGreen, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                Text(description, fontSize = 12.sp, color = TextMedium)
            }
            Text(returns, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = NexusGreen, textAlign = TextAlign.End)
        }
    }
}
