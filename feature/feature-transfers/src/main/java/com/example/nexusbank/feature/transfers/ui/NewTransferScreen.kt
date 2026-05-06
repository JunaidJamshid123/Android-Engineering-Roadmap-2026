package com.example.nexusbank.feature.transfers.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nexusbank.core.network.model.BankAccountDto
import com.example.nexusbank.core.network.model.ResolveRecipientData
import com.example.nexusbank.core.ui.components.ErrorDialog
import com.example.nexusbank.core.ui.theme.*

@Composable
fun NewTransferScreen(
    onBackClick: () -> Unit = {},
    onRecipientResolved: (
        fromAccountId: String,
        toAccountNumber: String,
        resolved: ResolveRecipientData
    ) -> Unit = { _, _, _ -> },
    viewModel: NewTransferViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.resolved) {
        val resolved = state.resolved
        val fromId = state.selectedFromAccountId
        if (resolved != null && fromId != null) {
            onRecipientResolved(fromId, state.toAccountNumber, resolved)
            viewModel.consumeResolved()
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
        topBar = { TransfersTopBar(title = "New Transfer", onBackClick = onBackClick) }
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
                SectionLabel("From Account")
                Spacer(Modifier.height(8.dp))

                if (state.isLoadingAccounts) {
                    LoadingCard()
                } else {
                    FromAccountSelector(
                        accounts = state.accounts,
                        selectedId = state.selectedFromAccountId,
                        onSelect = viewModel::selectFromAccount
                    )
                }

                Spacer(Modifier.height(24.dp))
                SectionLabel("Recipient")
                Spacer(Modifier.height(8.dp))

                CardSurface {
                    UnderlineField(
                        label = "Account Number",
                        value = state.toAccountNumber,
                        onValueChange = viewModel::onToAccountNumberChange,
                        placeholder = "e.g. NXB2118056783219",
                        icon = Icons.Default.AccountBalance,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    text = "We'll fetch and verify the recipient's name on the next step.",
                    fontSize = 11.sp,
                    color = TextLight
                )
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
                            onClick = viewModel::onContinueClick,
                            enabled = state.canContinue,
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
                            if (state.isResolving) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = "Continue",
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

/* ───────── components ───────── */

@Composable
private fun LoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = NexusGreen,
            strokeWidth = 2.dp,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun FromAccountSelector(
    accounts: List<BankAccountDto>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    if (accounts.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text(
                text = "No accounts available.",
                fontSize = 13.sp,
                color = TextLight
            )
        }
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedId } ?: accounts.first()
    val multi = accounts.size > 1

    Box {
        AccountTile(
            account = selected,
            trailing = {
                if (multi) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            },
            onClick = if (multi) {
                { expanded = true }
            } else null
        )

        if (multi) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                accounts.forEach { acc ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "${acc.accountType} • ${acc.accountNumber.maskAccount()}  ${acc.currency} ${acc.balance}",
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            onSelect(acc.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountTile(
    account: BankAccountDto,
    trailing: @Composable () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(NexusGreen)
    Row(
        modifier = (if (onClick != null) baseModifier.clickable { onClick() } else baseModifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.accountType.replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = account.accountNumber.maskAccount(),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Available",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "${account.currency} ${account.balance}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

internal fun String.maskAccount(): String =
    if (length <= 4) this else "${take(3)}•••••••${takeLast(4)}"

/* ─────────── shared field composables ─────────── */

@Composable
internal fun UnderlineField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextLight,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextLight,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 14.sp,
                        color = TextLight
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
                    cursorBrush = SolidColor(NexusGreen),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = imeAction
                    )
                )
            }
        }
    }
}

@Composable
internal fun DropdownField(
    label: String,
    value: String,
    placeholder: String,
    icon: ImageVector,
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextLight,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExpandChange(true) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = value.ifEmpty { placeholder },
                    fontSize = 14.sp,
                    color = if (value.isEmpty()) TextLight else TextDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(22.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandChange(false) }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 14.sp) },
                        onClick = { onOptionSelected(option) }
                    )
                }
            }
        }
    }
}
