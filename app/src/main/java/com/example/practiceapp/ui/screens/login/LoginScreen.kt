package com.example.practiceapp.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.practiceapp.R
import com.example.practiceapp.ui.screens.signup.SignUpContent

// Clean palette
private val BgWhite = Color(0xFFFFFFFF)
private val Green = Color(0xFF006A4E)
private val TextDark = Color(0xFF1A1A1A)
private val TextMedium = Color(0xFF555555)
private val TextLight = Color(0xFF9E9E9E)
private val Divider = Color(0xFFE0E0E0)
private val FieldLine = Color(0xFFCCCCCC)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {}
) {
    // 0 = Login, 1 = Sign Up
    var selectedTab by remember { mutableIntStateOf(0) }

    // Login state
    var loginId by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite)
            .statusBarsPadding()
    ) {
        // ── Logo + App name (always visible) ──
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.nexus_app_icon),
                contentDescription = "Nexus Bank",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nexus Bank",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Green
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (selectedTab == 0) "Welcome back" else "Create your account",
                fontSize = 14.sp,
                color = TextLight
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Tab bar: Login | Sign Up ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(44.dp)
        ) {
            TabItem(
                title = "Login",
                isSelected = selectedTab == 0,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 0 }
            )
            TabItem(
                title = "Sign Up",
                isSelected = selectedTab == 1,
                modifier = Modifier.weight(1f),
                onClick = { selectedTab = 1 }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Tab content (scrollable) ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            if (selectedTab == 0) {
                // ════════════ LOGIN TAB ════════════
                LoginTabContent(
                    loginId = loginId,
                    onLoginIdChange = { loginId = it },
                    password = loginPassword,
                    onPasswordChange = { loginPassword = it },
                    passwordVisible = loginPasswordVisible,
                    onTogglePassword = { loginPasswordVisible = !loginPasswordVisible },
                    onForgotPasswordClick = onForgotPasswordClick,
                    onLoginClick = onLoginSuccess
                )
            } else {
                // ════════════ SIGN UP TAB ════════════
                SignUpContent(onSignUpClick = onSignUpClick)
            }
        }
    }
}

// ── Tab item ──
@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .drawWithContent {
                drawContent()
                drawLine(
                    color = if (isSelected) Green else Divider,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = if (isSelected) 2.5.dp.toPx() else 1.dp.toPx()
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Green else TextLight
        )
    }
}

// ══════════════════════════════════════
// LOGIN TAB CONTENT
// ══════════════════════════════════════
@Composable
private fun LoginTabContent(
    loginId: String,
    onLoginIdChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    // Login ID
    FieldLabel("Login Id")
    UnderlineTextField(
        value = loginId,
        onValueChange = onLoginIdChange,
        placeholder = "Enter your login ID",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Password
    FieldLabel("Password")
    UnderlineTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = "Enter your password",
        isPassword = true,
        passwordVisible = passwordVisible,
        onTogglePassword = onTogglePassword,
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Forgot links
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Forgot ID?",
            fontSize = 13.sp,
            color = Green,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onForgotPasswordClick() }
        )
        Text(
            text = "Forgot Password?",
            fontSize = 13.sp,
            color = Green,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onForgotPasswordClick() }
        )
    }

    Spacer(modifier = Modifier.height(36.dp))

    // Login button
    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Green),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = "Login",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(32.dp))
}

// ── Shared components ──

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun UnderlineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    val visualTransformation = if (isPassword && !passwordVisible)
        PasswordVisualTransformation() else VisualTransformation.None

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, color = TextDark),
        cursorBrush = SolidColor(Green),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        decorationBox = { innerTextField ->
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, fontSize = 15.sp, color = TextLight)
                        }
                        innerTextField()
                    }
                    if (isPassword && onTogglePassword != null) {
                        IconButton(onClick = onTogglePassword, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(FieldLine)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
