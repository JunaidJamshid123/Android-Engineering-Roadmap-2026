package com.example.nexusbank.feature.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
fun SignUpContent(
    onSignUpSuccess: () -> Unit = {},
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var genderExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSignUpSuccess) {
        if (uiState.isSignUpSuccess) onSignUpSuccess()
    }

    SectionLabel("Personal Information")
    Spacer(modifier = Modifier.height(16.dp))

    SignUpIconField(
        label = "Full Name",
        value = uiState.fullName,
        onValueChange = viewModel::onFullNameChange,
        placeholder = "Enter your full name",
        icon = Icons.Default.Person,
        keyboardType = KeyboardType.Text
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "Date of Birth",
        value = uiState.dateOfBirth,
        onValueChange = viewModel::onDateOfBirthChange,
        placeholder = "YYYY-MM-DD",
        icon = Icons.Default.CalendarToday,
        keyboardType = KeyboardType.Number
    )
    Spacer(modifier = Modifier.height(20.dp))

    // Gender dropdown
    Column {
        Text(
            text = "Gender",
            fontSize = 13.sp,
            color = TextMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { genderExpanded = true }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = uiState.gender.ifEmpty { "Select gender" },
                    fontSize = 15.sp,
                    color = if (uiState.gender.isEmpty()) TextLight else TextDark,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = TextLight,
                    modifier = Modifier.size(24.dp)
                )
            }
            DropdownMenu(
                expanded = genderExpanded,
                onDismissRequest = { genderExpanded = false }
            ) {
                listOf("MALE", "FEMALE", "OTHER").forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, fontSize = 15.sp) },
                        onClick = {
                            viewModel.onGenderChange(option)
                            genderExpanded = false
                        }
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

    Spacer(modifier = Modifier.height(32.dp))

    SectionLabel("Contact Information")
    Spacer(modifier = Modifier.height(16.dp))

    SignUpIconField(
        label = "Phone Number",
        value = uiState.phone,
        onValueChange = viewModel::onPhoneChange,
        placeholder = "+92 3XX XXXXXXX",
        icon = Icons.Default.Phone,
        keyboardType = KeyboardType.Phone
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "Email Address",
        value = uiState.email,
        onValueChange = viewModel::onEmailChange,
        placeholder = "Enter your email",
        icon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
    )

    Spacer(modifier = Modifier.height(32.dp))

    SectionLabel("Security")
    Spacer(modifier = Modifier.height(16.dp))

    SignUpIconField(
        label = "Password",
        value = uiState.password,
        onValueChange = viewModel::onPasswordChange,
        placeholder = "Min. 8 characters",
        icon = Icons.Default.Lock,
        isPassword = true,
        passwordVisible = uiState.isPasswordVisible,
        onTogglePassword = viewModel::onTogglePasswordVisibility,
        keyboardType = KeyboardType.Password
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "MPIN (4-digit)",
        value = uiState.mpin,
        onValueChange = viewModel::onMpinChange,
        placeholder = "Enter 4-digit MPIN",
        icon = Icons.Default.Pin,
        isPassword = true,
        passwordVisible = uiState.isMpinVisible,
        onTogglePassword = viewModel::onToggleMpinVisibility,
        keyboardType = KeyboardType.NumberPassword,
        imeAction = ImeAction.Done
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = uiState.termsAccepted,
            onCheckedChange = viewModel::onTermsAcceptedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = NexusGreen,
                uncheckedColor = FieldLineColor
            )
        )
        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(text = "I agree to the ", fontSize = 13.sp, color = TextMedium)
            Row {
                Text(
                    text = "Terms & Conditions",
                    fontSize = 13.sp,
                    color = NexusGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { }
                )
                Text(text = " and ", fontSize = 13.sp, color = TextMedium)
                Text(
                    text = "Privacy Policy",
                    fontSize = 13.sp,
                    color = NexusGreen,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { }
                )
            }
        }
    }

    if (uiState.error != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = uiState.error!!, fontSize = 13.sp, color = Color.Red)
    }

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = viewModel::onSignUpClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NexusGreen),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        enabled = uiState.termsAccepted && !uiState.isLoading
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Create Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }

    Spacer(modifier = Modifier.height(32.dp))
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = NexusGreen
    )
}

@Composable
private fun SignUpIconField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next
) {
    val visualTransformation = if (isPassword && !passwordVisible)
        PasswordVisualTransformation() else VisualTransformation.None

    Column {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontSize = 15.sp, color = TextDark),
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
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = TextLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
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
}
