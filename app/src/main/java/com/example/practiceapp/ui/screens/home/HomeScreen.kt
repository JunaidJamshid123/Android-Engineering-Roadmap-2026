package com.example.practiceapp.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practiceapp.R

private val Green = Color(0xFF006A4E)
private val GreenDark = Color(0xFF00543E)
private val GreenLight = Color(0xFFE8F5E9)
private val TextDark = Color(0xFF1A1A1A)
private val TextMedium = Color(0xFF555555)
private val TextLight = Color(0xFF9E9E9E)
private val BgWhite = Color(0xFFFFFFFF)
private val DividerLight = Color(0xFFBDBDBD)
private val gray = Color(0xFFF2F2F2L)

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
    ) {
        // ── Top Bar (dark green) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenDark)
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: menu
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = "Menu",
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            // Center: Logo + Name
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.nexus_app_icon),
                    contentDescription = "Nexus Bank",
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Nexus Bank",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Right: logout
            IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.logout),
                    contentDescription = "Logout",
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        // ── Welcome Strip ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Green)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = "WELCOME, Muhammad Muneeb",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        // ── Quick Actions (white bg, bordered, uniform height) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgWhite)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            data class QuickItem(val icon: Int, val label: String)
            val items = listOf(
                QuickItem(R.drawable.send_money, "Send\nMoney"),
                QuickItem(R.drawable.payment, "Payment"),
                QuickItem(R.drawable.cards, "Cards"),
                QuickItem(R.drawable.more, "More")
            )
            
            items.forEach { item ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFFD0D0D0),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(Green),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextDark,
                        textAlign = TextAlign.Center,
                        lineHeight = 11.sp,
                        maxLines = 2
                    )
                }
            }
        }

        // ── Thin separator ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerLight)
        )

        // ── What I Have row (with unlock icon + PKR) ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = gray)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.unlock),
                    contentDescription = "Unlock",
                    modifier = Modifier.size(14.dp),
                    colorFilter = ColorFilter.tint(TextDark)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "What I Have",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
            }
            Text(
                text = "PKR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Green
            )
        }

        // ── Thin divider ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerLight)
        )

        // ── Deposit Account ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gray)
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Deposit Account",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextDark
                        )
                        Text(
                            text = "Muneeb Aslam",
                            fontSize = 11.sp,
                            color = TextMedium
                        )
                    }
                }
                // QR code icon (replacing the Pr Ck badge)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GreenLight)
                        .padding(4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.qr_code),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(Green)
                    )
                }
            }

            // Account number
            Text(
                text = "4788  ●●●●  ●●●●  8818",
                fontSize = 11.sp,
                color = TextLight,
                modifier = Modifier.padding(start = 22.dp)
            )

            // Balance
            Text(
                text = "2,555.19",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(start = 22.dp)
            )
            Text(
                text = "Available Balance",
                fontSize = 11.sp,
                color = TextLight,
                modifier = Modifier.padding(start = 22.dp)
            )
        }

        // ── Thin divider ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerLight)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // ── What I Owe ──
        Text(
            text = "What I Owe",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark,
            modifier = Modifier
                .fillMaxWidth()
                .background(gray)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
