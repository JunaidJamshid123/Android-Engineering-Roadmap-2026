package com.example.nexusbank.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.profile.ui.components.*

@Composable
fun ContactInfoScreen(
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    ContactInfoContent(
        user = state.user,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onEditClick = onEditClick
    )
}

@Composable
private fun ContactInfoContent(
    user: User?,
    isLoading: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileTopBar(
            title = "Contact Info",
            onBackClick = onBackClick,
            trailing = {
                IconButton(onClick = onEditClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )
        SectionStrip("Contact Information")

        if (user == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = NexusGreen)
                } else {
                    Text(
                        text = error ?: "No contact info available",
                        color = TextMedium,
                        fontSize = 13.sp
                    )
                }
            }
            return@Column
        }

        val address = buildAddress(user)

        GroupLabel("Channels")
        GroupCard {
            ContactChannelRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = user.email.orDash(),
                verified = false
            )
            RowDivider()
            ContactChannelRow(
                icon = Icons.Default.Phone,
                label = "Phone",
                value = user.phone.orDash(),
                verified = false
            )
            RowDivider()
            ContactChannelRow(
                icon = Icons.Default.Phone,
                label = "Emergency Contact",
                value = listOfNotNull(
                    user.emergencyContactName?.takeIf { it.isNotBlank() },
                    user.emergencyContactPhone?.takeIf { it.isNotBlank() }
                ).joinToString(" • ").ifBlank { "—" },
                verified = false
            )
        }

        GroupLabel("Addresses")
        GroupCard {
            ContactChannelRow(
                icon = Icons.Default.Home,
                label = "Residential Address",
                value = address,
                verified = false
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NexusGreen)
        ) {
            Text(
                text = "Edit Contact Info",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun buildAddress(user: User): String {
    val parts = listOfNotNull(
        user.addressLine?.takeIf { it.isNotBlank() },
        user.city?.takeIf { it.isNotBlank() },
        user.country?.takeIf { it.isNotBlank() }
    )
    return if (parts.isEmpty()) "—" else parts.joinToString(", ")
}

@Composable
private fun ContactChannelRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    verified: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = label, fontSize = 12.sp, color = TextLight)
                if (verified) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = NexusGreen,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                color = TextDark,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview
@Composable
private fun ContactInfoPreview() {
    ContactInfoContent(
        user = User(
            id = "fc87dbdc-1e59-4c92-a01e-4ef982fc575d",
            userId = "0a67c940",
            fullName = "Junaid khan",
            phone = "+923001234568",
            email = "junaid2@nexusbank.com",
            dateOfBirth = "2003-12-03",
            gender = "MALE",
            kycStatus = com.example.nexusbank.core.domain.model.KycStatus.NOT_STARTED,
            fatherName = null,
            cnic = null,
            maritalStatus = null,
            nationality = "Pakistani",
            occupation = null,
            monthlyIncome = null,
            addressLine = null,
            city = null,
            country = "Pakistan",
            emergencyContactName = null,
            emergencyContactPhone = null,
            profilePictureUrl = null,
            createdAt = null,
            updatedAt = null
        ),
        isLoading = false,
        error = null,
        onBackClick = {},
        onEditClick = {}
    )
}
