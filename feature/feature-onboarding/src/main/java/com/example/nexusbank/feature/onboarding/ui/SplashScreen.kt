package com.example.nexusbank.feature.onboarding.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        val isCompact = maxWidth < 360.dp
        val logoBox = if (isCompact) 132.dp else 156.dp
        val logoIcon = if (isCompact) 76.dp else 96.dp
        val titleSize = if (isCompact) 24.sp else 28.sp
        val taglineSize = if (isCompact) 13.sp else 14.sp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(logoBox)
                    .scale(scale.value)
                    .alpha(alpha.value)
                    .shadow(elevation = 12.dp, shape = CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.nexus_app_icon),
                    contentDescription = "Nexus Bank",
                    modifier = Modifier.size(logoIcon)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Nexus Bank",
                fontSize = titleSize,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.4.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Your financial future, simplified",
                fontSize = taglineSize,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.78f),
                letterSpacing = 0.2.sp,
                modifier = Modifier.alpha(textAlpha.value)
            )

            Spacer(modifier = Modifier.height(if (isCompact) 36.dp else 48.dp))

            CircularProgressIndicator(
                modifier = Modifier
                    .size(26.dp)
                    .alpha(loaderAlpha.value),
                color = Color.White,
                strokeWidth = 2.dp
            )
        }

        Text(
            text = "Secured by Nexus  •  v1.0",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.55f),
            letterSpacing = 0.4.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .alpha(loaderAlpha.value)
        )
    }
}
