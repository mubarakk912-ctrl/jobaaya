package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

    var selectedAdminTab by remember { mutableIntStateOf(0) } // 0: User Management, 1: Spam/Reports, 2: Analytics, 3: Support

    val reportedProfiles = remember(profiles) {
        profiles.filter { it.isReported }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header row
        TabRow(
            selectedTabIndex = selectedAdminTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedAdminTab == 0,
                onClick = { selectedAdminTab = 0 },
                text = { Text(JobaayaLocalization.translate("admin_users", currentLang), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedAdminTab == 1,
                onClick = { selectedAdminTab = 1 },
                text = { Text(JobaayaLocalization.translate("admin_reports", currentLang), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedAdminTab == 2,
                onClick = { selectedAdminTab = 2 },
                text = { Text(JobaayaLocalization.translate("admin_analytics", currentLang), fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
            Tab(
                selected = selectedAdminTab == 3,
                onClick = { selectedAdminTab = 3 },
                text = { Text("Support", fontWeight = FontWeight.Bold, fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Routing based on selectors
        when (selectedAdminTab) {
            0 -> AdminUserManagementSection(viewModel = viewModel, profileList = profiles, currentLang = currentLang, onProfileClick = onProfileClick)
            1 -> AdminReportManagementSection(viewModel = viewModel, reportedList = reportedProfiles, currentLang = currentLang)
            2 -> AdminAnalyticsSection(profileList = profiles, currentLang = currentLang)
            3 -> AdminSupportSection(viewModel = viewModel)
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
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Reply to user problems and suggestions via push notifications.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(msg.userName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    color = if (msg.status == "Pending") Color.Red.copy(alpha = 0.2f) else Color.Green.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = msg.status,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = if (msg.status == "Pending") Color.Red else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text("Mobile: ${msg.registeredMobile}", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text("Device: ${msg.deviceModel} (Android ${msg.androidVersion})", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(msg.message, fontSize = 14.sp)
                            
                            if (msg.status == "Pending") {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = replyTextMap[msg.id] ?: "",
                                    onValueChange = { replyTextMap = replyTextMap + (msg.id to it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Type your reply here...", fontSize = 12.sp) },
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val reply = replyTextMap[msg.id] ?: ""
                                        if (reply.isNotBlank()) {
                                            viewModel.replyToContactMessage(msg.id, msg.userId, reply)
                                            replyTextMap = replyTextMap - msg.id
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                    enabled = (replyTextMap[msg.id] ?: "").isNotBlank()
                                ) {
                                    Text("Send Reply & Resolve", fontSize = 12.sp)
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
            placeholder = { Text("Search by name, phone, or profession...", fontSize = 13.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
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
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        Text(
            text = "Showing ${filteredList.size} Results",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (user.isBlocked) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.name.take(2).uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if(user.isBlocked) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1E88E5))
                                }
                                if (user.isBlocked) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("(Blocked)", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(user.profession, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(user.mobileNumber, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline.copy(alpha=0.7f))
                        }

                        IconButton(onClick = { onProfileClick(user.id) }) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Preview Profile",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Toggle Badge
                        Button(
                            onClick = { viewModel.adminToggleVerification(user.id) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isVerified) Color(0xFFE53935) else Color(0xFF4CAF50)
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
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Review user complaints, spam detection issues, and block requests.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
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
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Clean moderation queue! No active reported profiles reported by clients.",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline,
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(Color.Red.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.Red)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(reported.name, fontWeight = FontWeight.Bold)
                                    Text("Profession: ${reported.profession}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Violation flags: Reported for spamming / pricing discrepancy coordinates.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                ) {
                                    Text("Dismiss Flag", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { 
                                        viewModel.blockUserProfile(reported.id)
                                        Toast.makeText(context, "Blocked and banned from searches.", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) {
                                    Text("Ban Account", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Real-time engagement audit metrics (profile click-through & searches)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Directory Views", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("$totalViewsSum", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Lead Clicks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("$totalClicksSum", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom drawn visual bar graphs!
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Profile Impact Audit Charts",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
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
                            modifier = Modifier.width(60.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Visual graph bar
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(14.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(filledPct)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${user.profileViewsCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(36.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
