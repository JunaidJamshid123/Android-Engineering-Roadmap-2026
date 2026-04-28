package com.example.nexusbank.feature.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nexusbank.core.ui.theme.SplashGradientBottom
import com.example.nexusbank.core.ui.theme.SplashGradientMid
import com.example.nexusbank.core.ui.theme.SplashGradientTop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit = {}
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val loaderAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
        }
        launch {
            alpha.animateTo(1f, animationSpec = tween(800))
        }
        launch {
            delay(400)
            textAlpha.animateTo(1f, animationSpec = tween(600))
        }
        launch {
            delay(800)
            loaderAlpha.animateTo(1f, animationSpec = tween(500))
        }
        delay(2500)
        onSplashFinished()
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(SplashGradientTop, SplashGradientMid, SplashGradientBottom)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(20.dp)
            ) {
                Image(
                    painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.nexus_app_icon),
                    contentDescription = "Nexus Bank",
                    modifier = Modifier.size(100.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Nexus Bank", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.alpha(textAlpha.value))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Your financial future, simplified", fontSize = 14.sp, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.alpha(textAlpha.value))
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp).alpha(loaderAlpha.value),
                color = Color.White, strokeWidth = 2.5.dp
            )
        }
    }
}
