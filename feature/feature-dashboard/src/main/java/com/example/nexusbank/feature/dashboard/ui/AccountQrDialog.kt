package com.example.nexusbank.feature.dashboard.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import com.example.nexusbank.core.domain.model.Account
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.dashboard.util.QrCodeGenerator
import org.json.JSONObject
import java.io.File

@Composable
fun AccountQrDialog(
    user: User,
    account: Account,
    onDismiss: () -> Unit
) {
    val qrContent = remember(user, account) {
        JSONObject().apply {
            put("name", user.fullName)
            put("acc", account.accountNumber)
            put("bank", "Nexus Bank")
            put("currency", account.currency)
        }.toString()
    }

    val qrBitmap = remember(qrContent) {
        QrCodeGenerator.generate(qrContent, 512)
    }

    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NexusGreenDark)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "My QR Code",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // User name
            Text(
                text = user.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Account number
            Text(
                text = account.accountNumber,
                fontSize = 13.sp,
                color = TextLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            // QR Code
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Account QR Code",
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Scan this QR code to send money\nto this account",
                fontSize = 12.sp,
                color = TextLight,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Account details card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BgGray)
                    .padding(12.dp)
            ) {
                DetailRow(label = "Account Holder", value = user.fullName)
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(label = "Account Number", value = account.accountNumber)
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(label = "Account Type", value = account.type.name)
                Spacer(modifier = Modifier.height(6.dp))
                DetailRow(label = "Bank", value = "Nexus Bank")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share button
            Button(
                onClick = { shareQrCode(context, qrBitmap, user.fullName, account.accountNumber) },
                colors = ButtonDefaults.buttonColors(containerColor = NexusGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Share QR Code", fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TextLight)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = TextDark)
    }
}

private fun shareQrCode(
    context: android.content.Context,
    bitmap: Bitmap,
    userName: String,
    accountNumber: String
) {
    val cacheDir = File(context.cacheDir, "qr_codes")
    cacheDir.mkdirs()
    val file = File(cacheDir, "nexus_qr_${accountNumber}.png")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(android.content.Intent.EXTRA_STREAM, uri)
        putExtra(
            android.content.Intent.EXTRA_TEXT,
            "Pay $userName\nAccount: $accountNumber\nBank: Nexus Bank"
        )
        addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        android.content.Intent.createChooser(shareIntent, "Share QR Code")
    )
}
