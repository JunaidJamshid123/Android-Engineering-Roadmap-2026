package com.example.nexusbank.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import android.widget.Toast
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.domain.repository.ProfileUpdateData
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.profile.ui.components.*

private val GENDER_OPTIONS = listOf("MALE", "FEMALE", "OTHER")
private val MARITAL_OPTIONS = listOf("SINGLE", "MARRIED", "DIVORCED", "WIDOWED")

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit = {},
    onUpdateSuccess: () -> Unit = onBackClick,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Show toast and navigate back when update is completed.
    LaunchedEffect(state.updateCompleted) {
        if (state.updateCompleted) {
            state.successMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            viewModel.consumeUpdateCompleted()
            onUpdateSuccess()
        }
    }

    // Show error toasts.
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    EditProfileContent(
        user = state.user,
        isUpdating = state.isUpdating,
        isLoading = state.isLoading,
        onBackClick = onBackClick,
        onSave = { data -> viewModel.updateProfile(data) }
    )
}

@Composable
private fun EditProfileContent(
    user: User?,
    isUpdating: Boolean,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onSave: (ProfileUpdateData) -> Unit
) {
    // Initialize form state from the loaded user, re-init when user reference changes.
    var fullName by remember(user?.id) { mutableStateOf(user?.fullName.orEmpty()) }
    var email by remember(user?.id) { mutableStateOf(user?.email.orEmpty()) }
    var dateOfBirth by remember(user?.id) { mutableStateOf(user?.dateOfBirth?.substringBefore('T').orEmpty()) }
    var gender by remember(user?.id) { mutableStateOf(user?.gender.orEmpty()) }
    var fatherName by remember(user?.id) { mutableStateOf(user?.fatherName.orEmpty()) }
    var cnic by remember(user?.id) { mutableStateOf(user?.cnic.orEmpty()) }
    var maritalStatus by remember(user?.id) { mutableStateOf(user?.maritalStatus.orEmpty()) }
    var nationality by remember(user?.id) { mutableStateOf(user?.nationality.orEmpty()) }
    var occupation by remember(user?.id) { mutableStateOf(user?.occupation.orEmpty()) }
    var monthlyIncome by remember(user?.id) { mutableStateOf(user?.monthlyIncome.orEmpty()) }
    var addressLine by remember(user?.id) { mutableStateOf(user?.addressLine.orEmpty()) }
    var city by remember(user?.id) { mutableStateOf(user?.city.orEmpty()) }
    var country by remember(user?.id) { mutableStateOf(user?.country.orEmpty()) }
    var emergencyName by remember(user?.id) { mutableStateOf(user?.emergencyContactName.orEmpty()) }
    var emergencyPhone by remember(user?.id) { mutableStateOf(user?.emergencyContactPhone.orEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
    ) {
        ProfileTopBar(title = "Edit Profile", onBackClick = onBackClick)
        SectionStrip("Update your information")

        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) CircularProgressIndicator(color = NexusGreen)
                else Text("No profile data", color = TextMedium, fontSize = 13.sp)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            GroupLabel("Personal Information")
            GroupCard {
                FormField("Full Name", fullName) { fullName = it }
                RowDivider()
                FormField("Email", email, keyboardType = KeyboardType.Email) { email = it }
                RowDivider()
                FormField("Date of Birth (YYYY-MM-DD)", dateOfBirth) { dateOfBirth = it }
                RowDivider()
                DropdownField("Gender", gender, GENDER_OPTIONS) { gender = it }
                RowDivider()
                FormField("Father's Name", fatherName) { fatherName = it }
                RowDivider()
                FormField("CNIC (xxxxx-xxxxxxx-x)", cnic) { cnic = it }
                RowDivider()
                DropdownField("Marital Status", maritalStatus, MARITAL_OPTIONS) {
                    maritalStatus = it
                }
                RowDivider()
                FormField("Nationality", nationality) { nationality = it }
                RowDivider()
                FormField("Occupation", occupation) { occupation = it }
                RowDivider()
                FormField(
                    "Monthly Income",
                    monthlyIncome,
                    keyboardType = KeyboardType.Number
                ) { value -> monthlyIncome = value.filter { it.isDigit() } }
            }

            GroupLabel("Contact Information")
            GroupCard {
                FormField("Address Line", addressLine) { addressLine = it }
                RowDivider()
                FormField("City", city) { city = it }
                RowDivider()
                FormField("Country", country) { country = it }
                RowDivider()
                FormField("Emergency Contact Name", emergencyName) { emergencyName = it }
                RowDivider()
                FormField(
                    "Emergency Contact Phone",
                    emergencyPhone,
                    keyboardType = KeyboardType.Phone
                ) { emergencyPhone = it }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        Surface(
            color = BgWhite,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val data = ProfileUpdateData(
                        fullName = fullName.trim().ifBlank { null },
                        email = email.trim().ifBlank { null },
                        dateOfBirth = dateOfBirth.trim().ifBlank { null },
                        gender = gender.trim().ifBlank { null },
                        fatherName = fatherName.trim().ifBlank { null },
                        cnic = cnic.trim().ifBlank { null },
                        maritalStatus = maritalStatus.trim().ifBlank { null },
                        nationality = nationality.trim().ifBlank { null },
                        occupation = occupation.trim().ifBlank { null },
                        monthlyIncome = monthlyIncome.trim().toLongOrNull(),
                        addressLine = addressLine.trim().ifBlank { null },
                        city = city.trim().ifBlank { null },
                        country = country.trim().ifBlank { null },
                        emergencyContactName = emergencyName.trim().ifBlank { null },
                        emergencyContactPhone = emergencyPhone.trim().ifBlank { null }
                    )
                    onSave(data)
                },
                enabled = !isUpdating,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NexusGreen)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Saving…", color = Color.White, fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold)
                } else {
                    Text(
                        "Save Changes",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = TextLight)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexusGreen,
                unfocusedBorderColor = DividerDark,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                cursorColor = NexusGreen
            )
        )
    }
}

@Composable
private fun DropdownField(
    label: String,
    value: String,
    options: List<String>,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 11.sp, color = TextLight)
        Spacer(modifier = Modifier.height(4.dp))
        Box {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexusGreen,
                    unfocusedBorderColor = DividerDark,
                    focusedTextColor = TextDark,
                    unfocusedTextColor = TextDark
                )
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt) },
                        onClick = {
                            onChange(opt)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
