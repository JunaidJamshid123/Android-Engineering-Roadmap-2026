package com.example.nexusbank.feature.profile.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.nexusbank.core.domain.model.KycStatus
import com.example.nexusbank.core.domain.model.User
import com.example.nexusbank.core.ui.theme.*
import com.example.nexusbank.feature.profile.ui.components.*
import com.example.nexusbank.feature.profile.util.decodeBase64DataUri

@Composable
fun ProfileScreen(
    onBackClick: () -> Unit = {},
    onPersonalInfoClick: () -> Unit = {},
    onContactInfoClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onPreferencesClick: () -> Unit = {},
    onHelpClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        Log.d("ProfilePic", "Photo picker returned uri=$uri")
        if (uri != null) viewModel.onProfilePicturePicked(uri)
    }

    ProfileScreenContent(
        state = state,
        onBackClick = onBackClick,
        onRetry = viewModel::refresh,
        onEditPhotoClick = {
            Log.d("ProfilePic", "Edit photo tapped — launching photo picker")
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onPersonalInfoClick = onPersonalInfoClick,
        onContactInfoClick = onContactInfoClick,
        onSecurityClick = onSecurityClick,
        onPreferencesClick = onPreferencesClick,
        onHelpClick = onHelpClick,
        onLogoutClick = onLogoutClick
    )
}

@Composable
private fun ProfileScreenContent(
    state: ProfileUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onEditPhotoClick: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onContactInfoClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgGray)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileTopBar(
            title = "My Profile",
            onBackClick = onBackClick,
            trailing = {
                IconButton(onClick = onRetry, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        val user = state.user
        when {
            user == null && state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NexusGreen)
                }
            }

            user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error ?: "Unable to load profile",
                            fontSize = 13.sp,
                            color = TextMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onRetry,
                            colors = ButtonDefaults.buttonColors(containerColor = NexusGreen)
                        ) { Text("Retry", color = Color.White) }
                    }
                }
            }

            else -> ProfileBody(
                user = user,
                isRefreshing = state.isLoading,
                isUploadingPicture = state.isUploadingPicture,
                errorMessage = state.error,
                onEditPhotoClick = onEditPhotoClick,
                onPersonalInfoClick = onPersonalInfoClick,
                onContactInfoClick = onContactInfoClick,
                onSecurityClick = onSecurityClick,
                onPreferencesClick = onPreferencesClick,
                onHelpClick = onHelpClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

@Composable
private fun ProfileBody(
    user: User,
    isRefreshing: Boolean,
    isUploadingPicture: Boolean,
    errorMessage: String?,
    onEditPhotoClick: () -> Unit,
    onPersonalInfoClick: () -> Unit,
    onContactInfoClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onPreferencesClick: () -> Unit,
    onHelpClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    // Header
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(NexusGreen)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Avatar(
                    pictureUrl = user.profilePictureUrl,
                    isUploading = isUploadingPicture,
                    onClick = onEditPhotoClick
                )
                IconButton(
                    onClick = onEditPhotoClick,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(NexusGreenDark)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change photo",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user.fullName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = maskCustomerId(user.id),
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = user.email,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = user.phone,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                KycChip(user.kycStatus)
            }

            if (isRefreshing) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(2.dp)
                )
            }
        }
    }

    // Account
    GroupLabel("Account")
    GroupCard {
        SettingsRow(
            icon = Icons.Default.Person,
            title = "Personal Information",
            subtitle = "Name, DOB, ID details",
            onClick = onPersonalInfoClick
        )
        RowDivider()
        SettingsRow(
            icon = Icons.Default.Email,
            title = "Contact Information",
            subtitle = "Email, phone, address",
            onClick = onContactInfoClick
        )
    }

    // Settings
    GroupLabel("Settings")
    GroupCard {
        SettingsRow(
            icon = Icons.Default.Security,
            title = "Security",
            subtitle = "Password, biometric, 2FA",
            onClick = onSecurityClick
        )
        RowDivider()
        SettingsRow(
            icon = Icons.Default.Tune,
            title = "App Preferences",
            subtitle = "Theme, language, notifications",
            onClick = onPreferencesClick
        )
    }

    // Support
    GroupLabel("Support")
    GroupCard {
        SettingsRow(
            icon = Icons.AutoMirrored.Filled.HelpOutline,
            title = "Help & Support",
            subtitle = "FAQs, contact us, about",
            onClick = onHelpClick
        )
    }

    if (errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = errorMessage,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }

    Spacer(modifier = Modifier.height(20.dp))
    OutlinedButton(
        onClick = onLogoutClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error
        )
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Logout,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Logout",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Nexus Bank • v1.0.0",
        fontSize = 11.sp,
        color = TextLight,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

@Composable
private fun Avatar(
    pictureUrl: String?,
    isUploading: Boolean,
    onClick: () -> Unit
) {
    val size = 96.dp
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(NexusGreenLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = remember(pictureUrl) {
            pictureUrl?.let { decodeBase64DataUri(it) }
        }
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            else -> Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = NexusGreen,
                modifier = Modifier.size(56.dp)
            )
        }
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun KycChip(status: KycStatus) {
    val (label, fg) = when (status) {
        KycStatus.VERIFIED -> "KYC Verified" to NexusGreen
        KycStatus.PENDING -> "KYC Pending" to Color(0xFFB45309)
        KycStatus.REJECTED -> "KYC Rejected" to Color(0xFFB00020)
        KycStatus.NOT_STARTED -> "KYC Not Started" to NexusGreenDark
    }
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = fg,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

internal fun maskCustomerId(id: String): String {
    val tail = id.takeLast(4).uppercase()
    return "NXB-•••• $tail"
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    ProfileScreenContent(
        state = ProfileUiState(
            user = User(
                id = "fc87dbdc-1e59-4c92-a01e-4ef982fc575d",
                userId = "0a67c940-b05b-4f34-955a-7d9cdd300a66",
                fullName = "Junaid khan",
                phone = "+923001234568",
                email = "junaid2@nexusbank.com",
                dateOfBirth = "2003-12-03",
                gender = "MALE",
                kycStatus = KycStatus.NOT_STARTED,
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
            )
        ),
        onBackClick = {},
        onRetry = {},
        onEditPhotoClick = {},
        onPersonalInfoClick = {},
        onContactInfoClick = {},
        onSecurityClick = {},
        onPreferencesClick = {},
        onHelpClick = {},
        onLogoutClick = {}
    )
}
