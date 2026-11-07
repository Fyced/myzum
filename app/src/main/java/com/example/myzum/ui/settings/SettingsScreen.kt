package com.example.myzum.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myzum.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: User?,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Profile Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = user?.personalInfo?.firstName?.firstOrNull()?.toString() ?: "U",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${user?.personalInfo?.firstName} ${user?.personalInfo?.lastName}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (user?.type == AccountType.MERCHANT) "Merchant Account" else "Particular Account",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            SettingsSection(title = "Account") {
                SettingsRow(icon = Icons.Default.Person, title = "Personal Information")
                SettingsRow(
                    icon = Icons.Default.Badge, 
                    title = if (user?.type == AccountType.MERCHANT) "Business NIF" else "DNI",
                    subtitle = if (user?.type == AccountType.MERCHANT) user?.businessInfo?.nif else user?.personalInfo?.dni
                )
                if (user?.type == AccountType.MERCHANT) {
                    SettingsRow(icon = Icons.Default.Store, title = "Business Profile")
                    SettingsRow(icon = Icons.Default.Receipt, title = "Tax Data")
                }
            }

            SettingsSection(title = "Terminal") {
                SettingsRow(icon = Icons.Default.Smartphone, title = "Terminal Settings", subtitle = user?.settings?.terminalName)
                if (user?.type == AccountType.MERCHANT) {
                    SettingsRow(icon = Icons.Default.Group, title = "Employees")
                    SettingsRow(icon = Icons.Default.Payments, title = "Payment Methods")
                }
            }

            SettingsSection(title = "Security") {
                SettingsRow(icon = Icons.Default.Lock, title = "Change Password")
                SettingsRow(
                    icon = Icons.Default.Fingerprint, 
                    title = "Biometric Unlock", 
                    onClick = onSecurityClick
                )
            }

            SettingsSection(title = "App") {
                SettingsRow(
                    icon = Icons.Default.Notifications, 
                    title = "Notifications", 
                    onClick = onNotificationsClick
                )
                SettingsRow(icon = Icons.Default.Palette, title = "Appearance")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF3B30).copy(alpha = 0.1f),
                    contentColor = Color(0xFFFF3B30)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Log Out", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        user = User(
            id = "12345678X",
            password = "password",
            type = AccountType.MERCHANT,
            personalInfo = PersonalInfo("Victor", "M", "victor@example.com", "123456789", dni = "12345678X"),
            businessInfo = BusinessInfo(
                tradeName = "My Shop",
                nif = "B12345678",
                address = Address("Street", "City", "Spain", "00000")
            ),
            bankInfo = BankInfo("")
        ),
        onBackClick = {},
        onNotificationsClick = {},
        onSecurityClick = {},
        onLogoutClick = {}
    )
}
