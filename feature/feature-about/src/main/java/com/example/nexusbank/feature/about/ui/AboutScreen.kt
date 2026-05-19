package com.example.nexusbank.feature.about.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbank.core.ui.theme.*

private const val APP_VERSION = "1.0.0"
private const val WEBSITE_URL = "https://nexusbank.com"
private const val SUPPORT_EMAIL = "support@nexusbank.com"
private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.example.practiceapp"

@Composable
@Preview
fun AboutScreen(onBackClick: () -> Unit = {}) {
    val context = LocalContext.current

    fun open(url: String) = runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
    fun email() = runCatching {
        context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$SUPPORT_EMAIL")))
    }
    fun share() = runCatching {
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Try NexusBank — banking made simple.\n$PLAY_STORE_URL"
            )
        }
        context.startActivity(Intent.createChooser(i, "Share NexusBank"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(onBackClick)

        // Hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(NexusGreenLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = com.example.nexusbank.core.ui.R.drawable.nexus_app_icon
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("NexusBank", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text("Version $APP_VERSION", fontSize = 12.sp, color = TextLight)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Banking made simple, secure & smart.",
                fontSize = 13.sp,
                color = TextMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        // About description
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BgWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Text(
                text = "NexusBank is a modern digital banking app that lets you " +
                        "manage accounts, send money, track spending and stay in " +
                        "control — anywhere, anytime. Built with security and " +
                        "simplicity at its core.",
                fontSize = 13.sp,
                color = TextMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Single grouped card with the essentials
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = BgWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                AboutRow(Icons.Default.Language, "Website", "nexusbank.com") { open(WEBSITE_URL) }
                RowDivider()
                AboutRow(Icons.Default.Email, "Contact", SUPPORT_EMAIL) { email() }
                RowDivider()
                AboutRow(Icons.Default.Share, "Share App") { share() }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "© 2026 NexusBank",
                fontSize = 11.sp,
                color = TextLight
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Made with 💚 in Pakistan",
                fontSize = 11.sp,
                color = TextLight
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TopBar(onBackClick: () -> Unit) {
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
                painter = painterResource(
                    id = com.example.nexusbank.core.ui.R.drawable.nexus_app_icon
                ),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "About",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.size(40.dp))
    }
}

@Composable
private fun AboutRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(NexusGreenLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = NexusGreen, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextDark)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = TextLight)
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TextLight,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        color = DividerColor,
        thickness = 0.6.dp,
        modifier = Modifier.padding(start = 66.dp)
    )
}
