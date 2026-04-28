package com.example.nexusbank.feature.cards.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
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
import com.example.nexusbank.core.common.util.MaskUtils
import com.example.nexusbank.core.domain.model.Card
import com.example.nexusbank.core.ui.components.NexusErrorView
import com.example.nexusbank.core.ui.components.NexusLoadingIndicator
import com.example.nexusbank.core.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    onBackClick: () -> Unit = {},
    viewModel: CardsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Cards") },
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
                message = uiState.error ?: "", onRetry = { viewModel.loadCards() },
                modifier = Modifier.padding(padding)
            )
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.cards) { card ->
                        CardItem(card = card, onToggleLock = { viewModel.toggleLock(card.id, !card.isLocked) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CardItem(card: Card, onToggleLock: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NexusGreenDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = card.network.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = card.type.name, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = MaskUtils.maskCardNumber(card.cardNumber),
                fontSize = 18.sp, fontWeight = FontWeight.Medium, color = Color.White, letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Card Holder", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    Text(card.nameOnCard, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Expires", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                    Text("${card.expiryMonth}/${card.expiryYear}", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = card.status.name, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onToggleLock) {
                    Icon(
                        imageVector = if (card.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (card.isLocked) "Unlock" else "Lock",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
