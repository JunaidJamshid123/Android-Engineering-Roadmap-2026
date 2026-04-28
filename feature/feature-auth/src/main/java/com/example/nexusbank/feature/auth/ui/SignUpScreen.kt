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
import com.example.nexusbank.core.ui.theme.*

@Composable
fun SignUpContent(
    onSignUpClick: () -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var cnicNumber by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    SectionLabel("Personal Information")
    Spacer(modifier = Modifier.height(16.dp))

    SignUpIconField(
        label = "Full Name (as per CNIC)",
        value = fullName,
        onValueChange = { fullName = it },
        placeholder = "Enter your full name",
        icon = Icons.Default.Person,
        keyboardType = KeyboardType.Text
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "CNIC Number",
        value = cnicNumber,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || it == '-' }
            if (filtered.length <= 15) cnicNumber = filtered
        },
        placeholder = "XXXXX-XXXXXXX-X",
        icon = Icons.Default.CreditCard,
        keyboardType = KeyboardType.Number
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "Date of Birth",
        value = dateOfBirth,
        onValueChange = { dateOfBirth = it },
        placeholder = "DD/MM/YYYY",
        icon = Icons.Default.CalendarToday,
        keyboardType = KeyboardType.Number
    )

    Spacer(modifier = Modifier.height(32.dp))

    SectionLabel("Contact Information")
    Spacer(modifier = Modifier.height(16.dp))

    SignUpIconField(
        label = "Mobile Number",
        value = phoneNumber,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() || it == '+' }
            if (filtered.length <= 13) phoneNumber = filtered
        },
        placeholder = "+92 3XX XXXXXXX",
        icon = Icons.Default.Phone,
        keyboardType = KeyboardType.Phone
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "Email Address",
        value = email,
        onValueChange = { email = it },
        placeholder = "Enter your email",
        icon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
    )

    Spacer(modifier = Modifier.height(32.dp))

    SectionLabel("Set Password")
    Spacer(modifier = Modifier.height(16.dp))

    SignUpIconField(
        label = "Password",
        value = password,
        onValueChange = { password = it },
        placeholder = "Min. 8 characters",
        icon = Icons.Default.Lock,
        isPassword = true,
        passwordVisible = passwordVisible,
        onTogglePassword = { passwordVisible = !passwordVisible },
        keyboardType = KeyboardType.Password
    )
    Spacer(modifier = Modifier.height(20.dp))

    SignUpIconField(
        label = "Confirm Password",
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        placeholder = "Re-enter your password",
        icon = Icons.Default.Lock,
        isPassword = true,
        passwordVisible = confirmPasswordVisible,
        onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done
    )

    Spacer(modifier = Modifier.height(24.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = termsAccepted,
            onCheckedChange = { termsAccepted = it },
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

    Spacer(modifier = Modifier.height(28.dp))

    Button(
        onClick = onSignUpClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NexusGreen),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        enabled = termsAccepted
    ) {
        Text(
            text = "Create Account",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
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
