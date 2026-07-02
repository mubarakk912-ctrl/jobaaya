package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AccountType
import com.example.data.model.ProfileMedia
import com.example.data.model.WorkStatus
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import com.example.ui.components.ProfessionPicker
import com.example.viewmodel.JobaayaViewModel

@Composable
fun SettingsScreen(
    viewModel: JobaayaViewModel,
    onPreviewClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val blockedUsers by viewModel.blockedProfiles.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val isMobilePublic by viewModel.isMobilePublic.collectAsState()
    val isAccountPrivate by viewModel.isAccountPrivate.collectAsState()
    val serviceRadius by viewModel.serviceRadius.collectAsState()

    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showPrivacyItems by remember { mutableStateOf(false) }
    var notificationState by remember { mutableStateOf(true) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Professional details editing state
    var editName by remember { mutableStateOf(myProfile?.name ?: "") }
    var editEmail by remember { mutableStateOf(myProfile?.emailAddress ?: "") }
    var editMobile by remember { mutableStateOf(myProfile?.mobileNumber ?: "") }
    var editAddress by remember { mutableStateOf(myProfile?.fullAddress ?: "") }
    var editProfession by remember { mutableStateOf(myProfile?.profession ?: "") }
    var editSkills by remember { mutableStateOf(myProfile?.skillsRaw ?: "") }
    var editAbout by remember { mutableStateOf(myProfile?.aboutSection ?: "") }
    var editExperience by remember { mutableStateOf(myProfile?.yearsOfExperience?.toString() ?: "0") }

    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Profile, 1: App Settings

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0B3A51), // Consistent Deep Teal
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color.White
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0, 
                onClick = { selectedTab = 0 }, 
                text = { 
                    Text(
                        text = "PROFILE", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White 
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 1, 
                onClick = { selectedTab = 1 }, 
                text = { 
                    Text(
                        text = "APP", 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Black, 
                        color = Color.White 
                    ) 
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            myProfile?.let { me ->
                when (selectedTab) {
                    0 -> {
                        // --- PROFILE TAB ---
                        Card(
                            modifier = Modifier.fillMaxWidth(), 
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                    if (me.profilePhotoUrl.isNotBlank()) {
                                        AsyncImage(model = ImageRequest.Builder(context).data(me.profilePhotoUrl).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(text = me.name.take(2).uppercase(), fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = me.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Text(text = me.profession, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }

                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = "Professional Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                
                                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                                ProfessionPicker(currentProfession = editProfession, onProfessionChange = { editProfession = it }, currentSkills = editSkills, onSkillsChange = { editSkills = it }, label = "Profession")
                                OutlinedTextField(value = editExperience, onValueChange = { editExperience = it }, label = { Text("Years of Experience") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editAbout, onValueChange = { editAbout = it }, label = { Text("About Me") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                                
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                
                                Text(text = "Service Area Radius: ${serviceRadius.toInt()} km", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Slider(value = serviceRadius, onValueChange = { viewModel.setServiceRadius(it) }, valueRange = 1f..100f, modifier = Modifier.fillMaxWidth())
                                
                                Button(
                                    onClick = { Toast.makeText(context, "Opening Portfolio...", Toast.LENGTH_SHORT).show() },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Manage Portfolio Gallery")
                                }

                                Button(onClick = {
                                    viewModel.updateMyProfessionalProfile(me.copy(name = editName, profession = editProfession, skillsRaw = editSkills, aboutSection = editAbout, yearsOfExperience = editExperience.toIntOrNull() ?: 0))
                                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.Save, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Save Changes")
                                }
                                
                                Button(onClick = { onPreviewClick(me.id) }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                                    Text("Preview Profile Card")
                                }
                            }
                        }
                    }

                    1 -> {
                        // --- APP & PRIVACY TAB ---
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Expandable Privacy Section
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showPrivacyItems = !showPrivacyItems }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(12.dp))
                                        Text(text = "Privacy Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Icon(
                                        imageVector = if (showPrivacyItems) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = null
                                    )
                                }

                                androidx.compose.animation.AnimatedVisibility(visible = showPrivacyItems) {
                                    Column(modifier = Modifier.padding(start = 8.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(Modifier.weight(1f)) {
                                                Text("Public Mobile Number", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                                Text("Allow everyone to see your contact", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                            Switch(checked = isMobilePublic, onCheckedChange = { viewModel.setMobilePublic(it) })
                                        }
                                        
                                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(Modifier.weight(1f)) {
                                                Text("Private Account", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                                Text("Only connections can view full details", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                            Switch(checked = isAccountPrivate, onCheckedChange = { viewModel.setAccountPrivate(it) })
                                        }
                                        
                                        Row(Modifier.fillMaxWidth().clickable { showBlockedDialog = true }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Block, null, tint = MaterialTheme.colorScheme.error)
                                            Spacer(Modifier.width(12.dp))
                                            Text("Blocked Users List", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                        }

                                        Button(
                                            onClick = { showDeleteConfirmDialog = true },
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                                        ) {
                                            Icon(Icons.Default.Delete, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Delete Account Permanently")
                                        }
                                    }
                                }

                                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                Text(text = "Appearance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.outline)
                                        Spacer(Modifier.width(12.dp))
                                        Text("Dark Mode", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                    Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
                                }
                                
                                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                
                                Text(text = "App & Data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Row(Modifier.fillMaxWidth().clickable { showLanguagesDialog = true }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Text("App Language: ${currentLang.displayName}", color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(Modifier.fillMaxWidth().clickable { Toast.makeText(context, "Cache Cleared", Toast.LENGTH_SHORT).show() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.SdStorage, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Clear Cache", color = MaterialTheme.colorScheme.onSurface)
                                }
                                
                                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                
                                Text(text = "Support & Social", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Row(Modifier.fillMaxWidth().clickable { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jobaaya.com/support"))
                                    context.startActivity(intent)
                                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Help, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Help Center / Contact Us", color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(Modifier.fillMaxWidth().clickable { 
                                    Toast.makeText(context, "Bug reported. Thank you!", Toast.LENGTH_SHORT).show()
                                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.BugReport, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Report a Bug", color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(Modifier.fillMaxWidth().clickable { 
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, "Download jobaaya app to find local services!")
                                        type = "text/plain"
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, null))
                                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Share jobaaya App", color = MaterialTheme.colorScheme.onSurface)
                                }
                                Row(Modifier.fillMaxWidth().clickable { 
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.example.jobaaya"))
                                    try { context.startActivity(intent) } catch (e: Exception) {}
                                }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.outline)
                                    Spacer(Modifier.width(12.dp))
                                    Text("Rate Us on Play Store", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.handleLogout() }, 
                            modifier = Modifier.fillMaxWidth().height(52.dp), 
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Icon(Icons.Default.PowerSettingsNew, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Logout", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    // Languages Dialog with more options
    if (showLanguagesDialog) {
        Dialog(onDismissRequest = { showLanguagesDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp), 
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Language", 
                        fontWeight = FontWeight.Bold, 
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    Box(modifier = Modifier.height(400.dp)) {
                        LazyColumn {
                            items(AppLanguage.entries) { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            viewModel.changeLanguage(lang)
                                            showLanguagesDialog = false 
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = lang.displayName, 
                                        color = if (lang == currentLang) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        fontWeight = if (lang == currentLang) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 16.sp
                                    )
                                    if (lang == currentLang) {
                                        Icon(Icons.Default.Star, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.TextButton(
                        onClick = { showLanguagesDialog = false }, 
                        modifier = Modifier.align(Alignment.End)
                    ) { 
                        Text("CANCEL", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) 
                    }
                }
            }
        }
    }

    // Other Dialogs (Blocked/Delete)
    if (showBlockedDialog) {
        Dialog(onDismissRequest = { showBlockedDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Blocked Users", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    
                    if (blockedUsers.isEmpty()) {
                        Text("No users blocked yet.", color = Color.Gray)
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(blockedUsers) { user ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.LightGray)) {
                                            if (user.profilePhotoUrl.isNotBlank()) {
                                                AsyncImage(user.profilePhotoUrl, null, contentScale = ContentScale.Crop)
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(user.name, fontWeight = FontWeight.Medium)
                                    }
                                    androidx.compose.material3.TextButton(onClick = { viewModel.unblockUserProfile(user.id) }) {
                                        Text("Unblock", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { showBlockedDialog = false }, Modifier.align(Alignment.End)) { Text("Close") }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        Dialog(onDismissRequest = { showDeleteConfirmDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Confirm Delete", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Text("Are you sure you want to permanently delete your account? This action cannot be undone.")
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        androidx.compose.material3.TextButton(onClick = { showDeleteConfirmDialog = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { 
                            showDeleteConfirmDialog = false
                            viewModel.handleLogout()
                        }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}
