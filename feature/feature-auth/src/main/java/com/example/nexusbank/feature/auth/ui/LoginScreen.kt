package com.example.nexusbank.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.ui.theme.*

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) onLoginSuccess()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgWhite)
            .statusBarsPadding()
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = com.example.nexusbank.core.ui.R.drawable.nexus_app_icon),
                contentDescription = "Nexus Bank",
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Nexus Bank",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NexusGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (selectedTab == 0) "Welcome back" else "Create your account",
                fontSize = 14.sp,
                color = TextLight
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            if (selectedTab == 0) {
                LoginTabContent(
                    loginId = uiState.loginId,
                    onLoginIdChange = viewModel::onLoginIdChange,
                    password = uiState.password,
                    onPasswordChange = viewModel::onPasswordChange,
                    passwordVisible = uiState.isPasswordVisible,
                    onTogglePassword = viewModel::onTogglePasswordVisibility,
                    onForgotPasswordClick = onForgotPasswordClick,
                    onLoginClick = viewModel::onLoginClick,
                    isLoading = uiState.isLoading,
                    error = uiState.error
                )
            } else {
                SignUpContent(onSignUpClick = onSignUpClick)
            }
        }
    }
}

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
                    color = if (isSelected) NexusGreen else DividerColor,
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
            color = if (isSelected) NexusGreen else TextLight
        )
    }
}

@Composable
private fun LoginTabContent(
    loginId: String,
    onLoginIdChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onLoginClick: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    FieldLabel("Login Id")
    UnderlineTextField(
        value = loginId,
        onValueChange = onLoginIdChange,
        placeholder = "Enter your login ID",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
    Spacer(modifier = Modifier.height(28.dp))

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

    if (error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error, fontSize = 13.sp, color = Color.Red)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Forgot ID?",
            fontSize = 13.sp,
            color = NexusGreen,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onForgotPasswordClick() }
        )
        Text(
            text = "Forgot Password?",
            fontSize = 13.sp,
            color = NexusGreen,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onForgotPasswordClick() }
        )
    }

    Spacer(modifier = Modifier.height(36.dp))

    Button(
        onClick = onLoginClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NexusGreen),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Login",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = TextMedium,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
internal fun UnderlineTextField(
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
        cursorBrush = SolidColor(NexusGreen),
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
                        .background(FieldLineColor)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
