package com.example.practiceapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practiceapp.ui.theme.PracticeAppTheme
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

// Chat state enum
enum class ChatState {
    IDLE,           // Initial state - showing welcome text
    THINKING,       // AI is processing - show orb animation
    CHAT            // Conversation mode - show messages
}

// Message data class
data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// Colors
private val DarkBackground = Color(0xFF0D0D0F)
private val SurfaceColor = Color(0xFF141417)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF8A8A8E)
private val TextDimmed = Color(0xFF4A4A4E)
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentIndigo = Color(0xFF6366F1)
private val DotColor = Color(0xFF1A1A1E)

@Composable
fun AIChatScreen(
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    var chatState by remember { mutableStateOf(ChatState.IDLE) }
    val messages = remember { mutableStateListOf<ChatMessage>() }
    
    // Animate background dim
    val backgroundDim by animateFloatAsState(
        targetValue = when (chatState) {
            ChatState.IDLE -> 1f
            ChatState.THINKING -> 0.6f
            ChatState.CHAT -> 0.8f
        },
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "backgroundDim"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated dotted background
        AnimatedDottedBackground(
            modifier = Modifier.alpha(backgroundDim * 0.5f)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header
            Header()
            
            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Welcome text - visible only in IDLE state
                androidx.compose.animation.AnimatedVisibility(
                    visible = chatState == ChatState.IDLE,
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(400))
                ) {
                    WelcomeContent()
                }
                
                // AI Orb - visible in THINKING state
                androidx.compose.animation.AnimatedVisibility(
                    visible = chatState == ChatState.THINKING,
                    enter = scaleIn(
                        initialScale = 0.5f,
                        animationSpec = tween(600, easing = EaseOutCubic)
                    ) + fadeIn(tween(400)),
                    exit = scaleOut(
                        targetScale = 0.8f,
                        animationSpec = tween(300)
                    ) + fadeOut(tween(300))
                ) {
                    ThinkingOrb()
                }
                
                // Chat messages - visible in CHAT state
                androidx.compose.animation.AnimatedVisibility(
                    visible = chatState == ChatState.CHAT,
                    enter = fadeIn(tween(400)),
                    exit = fadeOut(tween(300))
                ) {
                    ChatMessages(messages = messages)
                }
            }

            // Input field
            MessageInputField(
                value = messageText,
                onValueChange = { messageText = it },
                onSendClick = {
                    if (messageText.isNotBlank()) {
                        val userMessage = messageText.trim()
                        messageText = ""
                        
                        // Add user message
                        messages.add(ChatMessage(userMessage, isUser = true))
                        
                        // Start thinking
                        chatState = ChatState.THINKING
                    }
                },
                onMicClick = { },
                enabled = chatState != ChatState.THINKING,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp)
            )
        }
    }
    
    // Simulate AI response
    LaunchedEffect(chatState) {
        if (chatState == ChatState.THINKING) {
            delay(2500) // Simulate thinking time
            messages.add(
                ChatMessage(
                    "I'm here to help! This is a simulated AI response. " +
                    "In a real app, this would connect to an AI service.",
                    isUser = false
                )
            )
            chatState = ChatState.CHAT
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Atomova",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "AI",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light,
                    color = TextSecondary
                )
            )
        }
        
        // Settings icon placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SurfaceColor),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(TextDimmed)
            )
        }
    }
}

@Composable
private fun WelcomeContent(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "welcome")
    
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "textAlpha"
    )

    Column(
        modifier = modifier.padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How can I help you?",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary.copy(alpha = textAlpha),
                letterSpacing = (-1).sp
            )
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "Ask me anything",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = TextDimmed
            )
        )
    }
}

@Composable
private fun ThinkingOrb(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb")
    
    // Floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Scale breathing
    val breathe by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = modifier
                .offset(y = floatOffset.dp)
                .graphicsLayer {
                    scaleX = breathe
                    scaleY = breathe
                },
            contentAlignment = Alignment.Center
        ) {
            // Outer glow
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .blur(50.dp)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentIndigo.copy(alpha = pulse * 0.4f),
                            AccentPurple.copy(alpha = pulse * 0.2f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2
                )
            }
            
            // Secondary glow
            Canvas(
                modifier = Modifier
                    .size(160.dp)
                    .blur(30.dp)
            ) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0E7FF).copy(alpha = pulse * 0.5f),
                            AccentIndigo.copy(alpha = pulse * 0.3f),
                            Color.Transparent
                        )
                    ),
                    radius = size.minDimension / 2
                )
            }

            // Main orb
            Canvas(
                modifier = Modifier.size(120.dp)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.minDimension / 2
                
                // Base sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF3A3A4A),
                            Color(0xFF252530),
                            Color(0xFF1A1A22),
                            Color(0xFF101014)
                        ),
                        center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f),
                        radius = radius * 1.5f
                    ),
                    radius = radius,
                    center = center
                )
                
                // Inner reflection
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                        radius = radius * 0.7f
                    ),
                    radius = radius * 0.9f,
                    center = center
                )
                
                // Primary highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            Color.White.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(center.x - radius * 0.4f, center.y - radius * 0.45f),
                        radius = radius * 0.4f
                    ),
                    radius = radius * 0.35f,
                    center = Offset(center.x - radius * 0.4f, center.y - radius * 0.4f)
                )
                
                // Moving highlight
                val highlightAngle = Math.toRadians(rotation.toDouble())
                val hx = center.x + (radius * 0.3f * cos(highlightAngle)).toFloat()
                val hy = center.y + (radius * 0.3f * sin(highlightAngle)).toFloat()
                
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.3f * pulse),
                            Color.Transparent
                        ),
                        center = Offset(hx, hy),
                        radius = radius * 0.25f
                    ),
                    radius = radius * 0.18f,
                    center = Offset(hx, hy)
                )
                
                // Rim light
                drawCircle(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            AccentIndigo.copy(alpha = 0f),
                            AccentIndigo.copy(alpha = 0.4f * pulse),
                            AccentPurple.copy(alpha = 0.5f * pulse),
                            AccentIndigo.copy(alpha = 0.4f * pulse),
                            AccentIndigo.copy(alpha = 0f)
                        ),
                        center = center
                    ),
                    radius = radius,
                    center = center,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                )
                
                // Bottom ambient
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.2f),
                            Color.Transparent
                        ),
                        center = Offset(center.x + radius * 0.15f, center.y + radius * 0.4f),
                        radius = radius * 0.35f
                    ),
                    radius = radius * 0.3f,
                    center = Offset(center.x + radius * 0.15f, center.y + radius * 0.4f)
                )
                
                // Specular dots
                drawCircle(
                    color = Color.White,
                    radius = radius * 0.06f,
                    center = Offset(center.x - radius * 0.42f, center.y - radius * 0.45f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.7f),
                    radius = radius * 0.03f,
                    center = Offset(center.x - radius * 0.25f, center.y - radius * 0.52f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Thinking text with animation
        val dotsCount by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "dots"
        )
        
        Text(
            text = "Thinking" + ".".repeat(dotsCount.toInt().coerceIn(0, 3)),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun ChatMessages(
    messages: List<ChatMessage>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(messages) { message ->
            MessageBubble(message = message)
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (message.isUser) 20.dp else 6.dp,
                bottomEnd = if (message.isUser) 6.dp else 20.dp
            ),
            color = if (message.isUser) AccentPurple else SurfaceColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content,
                style = TextStyle(
                    fontSize = 15.sp,
                    color = if (message.isUser) Color.White else TextPrimary,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun AnimatedDottedBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val pulseAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val dotRadius = 1.2.dp.toPx()
        val spacing = 40.dp.toPx()
        
        val cols = (size.width / spacing).toInt() + 1
        val rows = (size.height / spacing).toInt() + 1
        
        val centerX = size.width / 2
        val centerY = size.height / 3

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * spacing + spacing / 2
                val y = row * spacing + spacing / 2
                
                val dx = x - centerX
                val dy = y - centerY
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                val maxDist = kotlin.math.sqrt(centerX * centerX + centerY * centerY)
                val normDist = (dist / maxDist).coerceIn(0f, 1f)
                
                val ripple = sin((normDist * 4 - pulseAnimation * Math.PI * 2).toFloat()) * 0.5f + 0.5f
                val alpha = (0.06f + ripple * 0.1f * (1f - normDist * 0.6f)).coerceIn(0.02f, 0.2f)
                
                drawCircle(
                    color = DotColor.copy(alpha = alpha),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun MessageInputField(
    value: String,
    onValueChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val hasText = value.isNotEmpty()
    
    Surface(
        modifier = modifier
            .height(56.dp)
            .alpha(if (enabled) 1f else 0.6f)
            .border(
                width = 1.dp,
                brush = if (hasText) {
                    Brush.linearGradient(
                        colors = listOf(
                            AccentIndigo.copy(alpha = 0.5f),
                            AccentPurple.copy(alpha = 0.5f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A30),
                            Color(0xFF2A2A30)
                        )
                    )
                },
                shape = RoundedCornerShape(28.dp)
            )
            .drawBehind {
                if (hasText) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentIndigo.copy(alpha = 0.12f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width * 0.6f,
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            },
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF151518),
        shadowElevation = if (hasText) 16.dp else 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMicClick,
                enabled = enabled,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1E22))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Mic,
                    contentDescription = "Voice input",
                    tint = TextDimmed,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = "Send a message...",
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = TextDimmed
                        )
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    enabled = enabled,
                    textStyle = TextStyle(
                        fontSize = 15.sp,
                        color = TextPrimary
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AccentIndigo),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            androidx.compose.animation.AnimatedVisibility(
                visible = hasText,
                enter = scaleIn(tween(200)) + fadeIn(tween(150)),
                exit = scaleOut(tween(150)) + fadeOut(tween(100))
            ) {
                IconButton(
                    onClick = onSendClick,
                    enabled = enabled,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(AccentIndigo, AccentPurple)
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier
                            .size(18.dp)
                            .offset(x = 1.dp)
                    )
                }
            }
            
            if (!hasText) {
                Spacer(modifier = Modifier.width(42.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AIChatScreenPreview() {
    PracticeAppTheme(darkTheme = true, dynamicColor = false) {
        AIChatScreen()
    }
}
