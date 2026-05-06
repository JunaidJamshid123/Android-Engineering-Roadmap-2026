package com.example.nexusbank.feature.transfers.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexusbank.core.network.model.TransferResponseData
import com.example.nexusbank.core.ui.components.ErrorDialog
import com.example.nexusbank.core.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ConfirmTransferScreen(
    onBackClick: () -> Unit = {},
    onTransferSuccess: (TransferResponseData) -> Unit = {},
    viewModel: ConfirmTransferViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var purposeExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.success) {
        val s = state.success
        if (s != null) {
            onTransferSuccess(s)
            viewModel.consumeSuccess()
        }
    }

    if (state.error != null) {
        ErrorDialog(
            message = state.error.orEmpty(),
            onDismiss = viewModel::clearError
        )
    }

    Scaffold(
        containerColor = BgGray,
        topBar = { TransfersTopBar(title = "Confirm & Pay", onBackClick = onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                SectionLabel("Recipient")
                Spacer(Modifier.height(8.dp))
                RecipientCard(
                    name = state.recipientName,
                    account = state.recipientAccountMasked,
                    accountType = state.recipientAccountType
                )

                Spacer(Modifier.height(24.dp))
                SectionLabel("Amount")
                Spacer(Modifier.height(8.dp))
                AmountInput(
                    value = state.amount,
                    onValueChange = viewModel::onAmountChange
                )
                Spacer(Modifier.height(8.dp))
                QuickAmountRow(
                    amounts = listOf("500", "1,000", "5,000", "10,000"),
                    onPick = { viewModel.onAmountChange(it.replace(",", "")) }
                )

                Spacer(Modifier.height(24.dp))
                SectionLabel("Details")
                Spacer(Modifier.height(8.dp))
                CardSurface {
                    DropdownField(
                        label = "Purpose",
                        value = state.purpose,
                        placeholder = "Select a purpose",
                        icon = Icons.Default.Check,
                        expanded = purposeExpanded,
                        onExpandChange = { purposeExpanded = it },
                        options = TransferPurpose.ALL,
                        onOptionSelected = {
                            viewModel.onPurposeChange(it)
                            purposeExpanded = false
                        }
                    )
                    FieldDivider()

                    UnderlineField(
                        label = "Remarks (optional)",
                        value = state.remarks,
                        onValueChange = viewModel::onRemarksChange,
                        placeholder = "Add a short note",
                        icon = Icons.Default.Notes,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                }
            }

            Surface(color = Color.White, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DividerColor)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .navigationBarsPadding()
                    ) {
                        Button(
                            onClick = viewModel::onSendClick,
                            enabled = state.canSend,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NexusGreen,
                                disabledContainerColor = FieldLineColor
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            if (state.isSending) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = "Send PKR ${formatAmount(state.amountValue)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    letterSpacing = 0.3.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ───────── private components ───────── */

@Composable
private fun RecipientCard(name: String, account: String, accountType: String) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(NexusGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NexusGreen
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (accountType.isNotBlank())
                    "${accountType.replaceFirstChar { it.uppercase() }} \u2022 $account"
                else account,
                fontSize = 12.sp,
                color = TextLight
            )
        }
        Icon(
            imageVector = Icons.Default.AccountBalance,
            contentDescription = null,
            tint = NexusGreen,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun AmountInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "PKR",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = NexusGreen
        )
        Spacer(Modifier.width(12.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = "0.00",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextLight
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                ),
                cursorBrush = SolidColor(NexusGreen),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                )
            )
        }
    }
}

@Composable
private fun QuickAmountRow(
    amounts: List<String>,
    onPick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        amounts.forEach { a ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(
                        width = 1.dp,
                        color = DividerColor,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onPick(a) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ $a",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = NexusGreen
                )
            }
        }
    }
}

internal fun formatAmount(value: Double): String {
    if (value <= 0.0) return "0"
    val nf = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }
    return nf.format(value)
}
