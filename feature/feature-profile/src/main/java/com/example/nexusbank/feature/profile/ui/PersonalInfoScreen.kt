package com.example.nexusbank.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PersonalInfoScreen(
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    PersonalInfoContent(
        user = state.user,
        isLoading = state.isLoading,
        error = state.error,
        onBackClick = onBackClick,
        onEditClick = onEditClick
    )
}

@Composable
private fun PersonalInfoContent(
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
            title = "Personal Info",
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
        SectionStrip("Personal Information")

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
                        text = error ?: "No profile data available",
                        color = TextMedium,
                        fontSize = 13.sp
                    )
                }
            }
            return@Column
        }

        GroupLabel("Basic Details")
        GroupCard {
            InfoRow("Full Name", user.fullName.orDash())
            RowDivider()
            InfoRow("Date of Birth", formatDate(user.dateOfBirth))
            RowDivider()
            InfoRow("Gender", user.gender.prettify())
            RowDivider()
            InfoRow("Nationality", user.nationality.orDash())
            RowDivider()
            InfoRow("Marital Status", user.maritalStatus.prettify())
        }

        GroupLabel("Identification")
        GroupCard {
            InfoRow("CNIC / National ID", user.cnic.orDash())
            RowDivider()
            InfoRow("Father's Name", user.fatherName.orDash())
            RowDivider()
            InfoRow("Occupation", user.occupation.orDash())
            RowDivider()
            InfoRow("Monthly Income", user.monthlyIncome.orDash())
        }

        GroupLabel("KYC")
        GroupCard {
            InfoRow("KYC Status", user.kycStatus.name.replace('_', ' '))
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
                text = "Edit Personal Info",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

internal fun String?.orDash(): String = if (this.isNullOrBlank()) "—" else this

internal fun String?.prettify(): String {
    if (this.isNullOrBlank()) return "—"
    return lowercase().replaceFirstChar { it.uppercase() }
}

/** Formats an ISO-8601 date (yyyy-MM-dd or full timestamp) to "dd MMM yyyy". */
internal fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val datePart = raw.substringBefore('T')
    val parts = datePart.split('-')
    if (parts.size != 3) return raw
    val (y, m, d) = parts
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val month = m.toIntOrNull()?.let { months.getOrNull(it - 1) } ?: m
    return "$d $month $y"
}

@Preview
@Composable
private fun PersonalInfoPreview() {
    PersonalInfoContent(
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
