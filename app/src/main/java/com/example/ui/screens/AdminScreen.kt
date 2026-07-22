package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.TextStyle
import com.example.data.model.ContactMessageWithId
import androidx.compose.runtime.LaunchedEffect

@Composable
fun AdminScreen(
    viewModel: JobaayaViewModel,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()

    var selectedAdminTab by remember { mutableIntStateOf(-1) } // -1: Menu, 0: User Management, 1: Spam/Reports, 2: Analytics, 3: Support

    BackHandler(enabled = selectedAdminTab != -1) {
        selectedAdminTab = -1
    }

    val reportedProfiles = remember(profiles) {
        profiles.filter { it.isReported }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D131D))
    ) {
        if (selectedAdminTab == -1) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Admin Panel",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp, start = 8.dp, top = 16.dp)
                )

                val adminTools = listOf(
                    Triple(JobaayaLocalization.translate("admin_users", currentLang), Icons.Default.FolderShared, 0),
                    Triple(JobaayaLocalization.translate("admin_reports", currentLang), Icons.Default.Gavel, 1),
                    Triple(JobaayaLocalization.translate("admin_analytics", currentLang), Icons.Default.SignalCellularAlt, 2),
                    Triple("Support", Icons.Default.Check, 3)
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        adminTools.take(2).forEach { (title, icon, index) ->
                            AdminToolCard(title, icon, index) { selectedAdminTab = it }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        adminTools.drop(2).forEach { (title, icon, index) ->
                            AdminToolCard(title, icon, index) { selectedAdminTab = it }
                        }
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedAdminTab = -1 }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = when (selectedAdminTab) {
                            0 -> JobaayaLocalization.translate("admin_users", currentLang)
                            1 -> JobaayaLocalization.translate("admin_reports", currentLang)
                            2 -> JobaayaLocalization.translate("admin_analytics", currentLang)
                            3 -> "Support"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Box(modifier = Modifier.weight(1f)) {
                    // Content Routing based on selectors
                    when (selectedAdminTab) {
                        0 -> AdminUserManagementSection(viewModel = viewModel, profileList = profiles, currentLang = currentLang, onProfileClick = onProfileClick)
                        1 -> AdminReportManagementSection(viewModel = viewModel, reportedList = reportedProfiles, currentLang = currentLang)
                        2 -> AdminAnalyticsSection(profileList = profiles, currentLang = currentLang)
                        3 -> AdminSupportSection(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.AdminToolCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, index: Int, onClick: (Int) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Card(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick(index) }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isPressed) Color.White else Color(0xFFBDBDBD),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun AdminSupportSection(viewModel: JobaayaViewModel) {
    val messages by viewModel.contactMessages.collectAsState()
    var replyTextMap by remember { mutableStateOf(mapOf<String, String>()) }

    LaunchedEffect(Unit) {
        viewModel.fetchContactMessages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Support Messages",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Reply to user problems and suggestions via push notifications.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (messages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No messages found.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(messages) { msg ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(msg.userName, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    color = if (msg.status == "Pending") Color.Red.copy(alpha = 0.15f) else Color(0xFF00281F).copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = msg.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (msg.status == "Pending") Color(0xFFFF5252) else Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("Mobile: ${msg.registeredMobile}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            Text("Device: ${msg.deviceModel} (Android ${msg.androidVersion})", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.05f))
                            
                            Text(msg.message, fontSize = 14.sp, color = Color.White.copy(alpha = 0.9f), lineHeight = 20.sp)
                            
                            if (msg.status == "Pending") {
                                Spacer(modifier = Modifier.height(16.dp))
                                OutlinedTextField(
                                    value = replyTextMap[msg.id] ?: "",
                                    onValueChange = { replyTextMap = replyTextMap + (msg.id to it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Type your reply here...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                        unfocusedContainerColor = Color.Black.copy(alpha = 0.2f),
                                        focusedBorderColor = Color(0xFF00281F),
                                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    maxLines = 4
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        val reply = replyTextMap[msg.id] ?: ""
                                        if (reply.isNotBlank()) {
                                            viewModel.replyToContactMessage(msg.id, msg.userId, reply)
                                            replyTextMap = replyTextMap - msg.id
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End).height(38.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00281F)),
                                    enabled = (replyTextMap[msg.id] ?: "").isNotBlank()
                                ) {
                                    Text("Send Reply & Resolve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// SUB SECTION 1: VERIFICATION BADGE SYSTEM & USERS LISTING
@Composable
fun AdminUserManagementSection(
    viewModel: JobaayaViewModel,
    profileList: List<UserProfile>,
    currentLang: AppLanguage,
    onProfileClick: (String) -> Unit
) {
    var adminSearchQuery by remember { mutableStateOf("") }
    var adminFilter by remember { mutableStateOf(JobaayaLocalization.translate("all", currentLang)) } // "All", "Unverified", "Verified", "Blocked"

    val filteredList = remember(profileList, adminSearchQuery, adminFilter) {
        profileList.filter { user ->
            val matchesSearch = user.name.contains(adminSearchQuery, ignoreCase = true) ||
                                user.profession.contains(adminSearchQuery, ignoreCase = true) ||
                                user.mobileNumber.contains(adminSearchQuery) ||
                                user.emailAddress.contains(adminSearchQuery, ignoreCase = true)
            
            val matchesFilter = when (adminFilter) {
                JobaayaLocalization.translate("unverified", currentLang) -> !user.isVerified
                JobaayaLocalization.translate("verified_filter", currentLang) -> user.isVerified
                JobaayaLocalization.translate("blocked_filter", currentLang) -> user.isBlocked
                else -> true
            }
            
            matchesSearch && matchesFilter
        }.sortedByDescending { it.lastSeen }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = adminSearchQuery,
            onValueChange = { adminSearchQuery = it },
            placeholder = { Text("Search by name, phone, or profession...", fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f)) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF16202E),
                unfocusedContainerColor = Color(0xFF16202E),
                focusedBorderColor = Color(0xFF00281F),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
            ),
            textStyle = TextStyle(fontSize = 14.sp)
        )

        // Filter Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf(
                JobaayaLocalization.translate("all", currentLang), 
                JobaayaLocalization.translate("unverified", currentLang), 
                JobaayaLocalization.translate("verified_filter", currentLang), 
                JobaayaLocalization.translate("blocked_filter", currentLang)
            )
            items(filters) { filter ->
                val isSelected = adminFilter == filter
                Surface(
                    modifier = Modifier.clickable { adminFilter = filter },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) Color(0xFF00281F) else Color(0xFF16202E),
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else Color.White.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = Color.White.copy(alpha = 0.1f))

        Text(
            text = "Showing ${filteredList.size} Results",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredList) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (user.isBlocked) Color.Red.copy(alpha = 0.2f) else Color(0xFF00281F).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.name.take(2).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(user.isBlocked) Color(0xFFFF5252) else Color(0xFF4CAF50))
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1E88E5))
                                }
                                if (user.isBlocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(Blocked)", color = Color(0xFFFF5252), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(user.profession, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(user.mobileNumber, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                        }

                        IconButton(onClick = { onProfileClick(user.id) }) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Preview Profile",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Toggle Badge
                        Button(
                            onClick = { viewModel.adminToggleVerification(user.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isVerified) Color(0xFFD32F2F) else Color(0xFF00281F)
                            )
                        ) {
                            Text(
                                text = if (user.isVerified) "Revoke" else "Verify",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

// SUB SECTION 2: AUDIT LOGS MODERATION COMPLIANCE LAYOUT
@Composable
fun AdminReportManagementSection(
    viewModel: JobaayaViewModel,
    reportedList: List<UserProfile>,
    currentLang: AppLanguage
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Moderation Compliance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Review user complaints, spam detection issues, and block requests.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (reportedList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Clean moderation queue! No active reported profiles.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reportedList) { reported ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Red.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFF5252))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(reported.name, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("Profession: ${reported.profession}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Violation flags: Reported for spamming / pricing discrepancy coordinates.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        viewModel.adminPardonUser(reported.id)
                                        Toast.makeText(context, "Pardoned user successfully.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                                ) {
                                    Text("Dismiss Flag", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                Button(
                                    onClick = { 
                                        viewModel.blockUserProfile(reported.id)
                                        Toast.makeText(context, "Blocked and banned from searches.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                                ) {
                                    Text("Ban Account", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// SUB SECTION 3: VISUAL GRAPHING OF VIEWS & BUSINESS INSIGHTS
@Composable
fun AdminAnalyticsSection(
    profileList: List<UserProfile>,
    currentLang: AppLanguage
) {
    val totalViewsSum = remember(profileList) { profileList.sumOf { it.profileViewsCount } }
    val totalClicksSum = remember(profileList) { profileList.sumOf { it.interactionsCount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Growth Analytics Insights",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Real-time engagement audit metrics (profile click-through & searches)",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Directory Views", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Text("$totalViewsSum", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Lead Clicks", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Text("$totalClicksSum", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom drawn visual bar graphs!
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Profile Impact Audit Charts",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                profileList.take(4).forEach { user ->
                    val maxViewsScale = 600f
                    val filledPct = (user.profileViewsCount.toFloat() / maxViewsScale).coerceIn(0.1f, 1f)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user.name.split(" ").first(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.width(60.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Visual graph bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(filledPct)
                                    .background(Color(0xFF00281F), CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${user.profileViewsCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
