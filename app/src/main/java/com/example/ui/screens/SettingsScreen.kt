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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val blockedUsers by viewModel.blockedProfiles.collectAsState()
    val isMobilePublic by viewModel.isMobilePublic.collectAsState()
    val isAccountPrivate by viewModel.isAccountPrivate.collectAsState()

    var showLanguagesDialog by remember { mutableStateOf(false) }
    var showPrivacyItems by remember { mutableStateOf(false) }
    var showBlockedDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App settings Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0B3A51))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = JobaayaLocalization.translate("settings", currentLang).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- APP & PRIVACY SECTION ---
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
                            Text(text = JobaayaLocalization.translate("privacy_settings", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
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
                                    Text(JobaayaLocalization.translate("public_mobile", currentLang), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(JobaayaLocalization.translate("public_mobile_desc", currentLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(checked = isMobilePublic, onCheckedChange = { viewModel.setMobilePublic(it) })
                            }
                            
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(JobaayaLocalization.translate("private_account", currentLang), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                    Text(JobaayaLocalization.translate("private_account_desc", currentLang), fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                }
                                Switch(checked = isAccountPrivate, onCheckedChange = { viewModel.setAccountPrivate(it) })
                            }
                            
                            Row(Modifier.fillMaxWidth().clickable { showBlockedDialog = true }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Block, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(12.dp))
                                Text(JobaayaLocalization.translate("blocked_list", currentLang), fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }

                            Button(
                                onClick = { showDeleteConfirmDialog = true },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer)
                            ) {
                                Icon(Icons.Default.Delete, null)
                                Spacer(Modifier.width(8.dp))
                                Text(JobaayaLocalization.translate("delete_account", currentLang))
                            }
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    
                    Text(text = JobaayaLocalization.translate("app_data", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(Modifier.fillMaxWidth().clickable { showLanguagesDialog = true }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text("${JobaayaLocalization.translate("languages", currentLang)}: ${currentLang.displayName}", color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth().clickable { Toast.makeText(context, JobaayaLocalization.translate("clear_cache", currentLang), Toast.LENGTH_SHORT).show() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SdStorage, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("clear_cache", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    
                    Text(text = JobaayaLocalization.translate("support_social", currentLang), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(Modifier.fillMaxWidth().clickable { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://jobaaya.com/support"))
                        context.startActivity(intent)
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Help, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("help_center", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth().clickable { 
                        Toast.makeText(context, JobaayaLocalization.translate("report_bug", currentLang), Toast.LENGTH_SHORT).show()
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BugReport, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("report_bug", currentLang), color = MaterialTheme.colorScheme.onSurface)
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
                        Text(JobaayaLocalization.translate("share_app", currentLang), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(Modifier.fillMaxWidth().clickable { 
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.example.jobaaya"))
                        try { context.startActivity(intent) } catch (e: Exception) {}
                    }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ThumbUp, null, tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(12.dp))
                        Text(JobaayaLocalization.translate("rate_us", currentLang), color = MaterialTheme.colorScheme.onSurface)
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
                Text(JobaayaLocalization.translate("logout", currentLang), color = Color.White, fontWeight = FontWeight.Bold)
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
                        text = JobaayaLocalization.translate("select_language", currentLang),
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
                        Text(JobaayaLocalization.translate("cancel", currentLang).uppercase(), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
