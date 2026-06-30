package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

    var showLanguagesSelector by remember { mutableStateOf(false) }
    var notificationState by remember { mutableStateOf(true) }

    // Professional details editing state
    var editName by remember { mutableStateOf(myProfile?.name ?: "") }
    var editEmail by remember { mutableStateOf(myProfile?.emailAddress ?: "") }
    var editMobile by remember { mutableStateOf(myProfile?.mobileNumber ?: "") }
    var editAddress by remember { mutableStateOf(myProfile?.fullAddress ?: "") }
    var editProfession by remember { mutableStateOf(myProfile?.profession ?: "Mechanic") }
    var editSkills by remember { mutableStateOf(myProfile?.skillsRaw ?: "Diagnostics, Tuning") }
    var editHours by remember { mutableStateOf(myProfile?.workingHours ?: "09:00 - 18:00") }
    var editAbout by remember { mutableStateOf(myProfile?.aboutSection ?: "Freelancer") }
    var editExperience by remember { mutableStateOf(myProfile?.yearsOfExperience?.toString() ?: "3") }
    var editPhotoUrl by remember { mutableStateOf(myProfile?.profilePhotoUrl ?: "") }
    var editLat by remember { mutableStateOf(myProfile?.latitude?.toString() ?: "28.7159") }
    var editLong by remember { mutableStateOf(myProfile?.longitude?.toString() ?: "77.1006") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        myProfile?.let { me ->
            // Section Header Title
            Text(
                text = "Preferences & Dashboard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Dynamic User Profile Summary Card inside settings
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        if (me.profilePhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(me.profilePhotoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = me.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = me.name.take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = me.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = me.accountType, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        Text(text = "Rating: ${me.averageRating}★", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Professional description and profile updates
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit Active Business Information",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ProfessionPicker(
                        currentProfession = editProfession,
                        onProfessionChange = { editProfession = it },
                        currentSkills = editSkills,
                        onSkillsChange = { editSkills = it },
                        label = "Business Trade / Profession"
                    )

                    OutlinedTextField(
                        value = editSkills,
                        onValueChange = { editSkills = it },
                        label = { Text("Skills List (Comma separated)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editExperience,
                        onValueChange = { editExperience = it },
                        label = { Text("Years of Experience") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editHours,
                        onValueChange = { editHours = it },
                        label = { Text("Official working hours") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editMobile,
                        onValueChange = { editMobile = it },
                        label = { Text("Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = { Text("Physical Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editAbout,
                        onValueChange = { editAbout = it },
                        label = { Text("About section free text") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.updateMyProfessionalProfile(me.copy(
                                name = editName,
                                emailAddress = editEmail,
                                mobileNumber = editMobile,
                                fullAddress = editAddress,
                                profession = editProfession,
                                skillsRaw = editSkills,
                                workingHours = editHours,
                                aboutSection = editAbout,
                                yearsOfExperience = editExperience.toIntOrNull() ?: me.yearsOfExperience,
                                profilePhotoUrl = editPhotoUrl,
                                latitude = editLat.toDoubleOrNull() ?: me.latitude,
                                longitude = editLong.toDoubleOrNull() ?: me.longitude
                            ))
                            Toast.makeText(context, "Saved Trade Information successfully.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Card Details", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { onPreviewClick(me.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null) // Using Star as a placeholder for 'Preview'
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview My Profile Card", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // APP LOGISTICS SETTINGS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Global App Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic multi-language spinner dropdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLanguagesSelector = !showLanguagesSelector }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Language Choice (भाषा की पसंद)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Current: ${currentLang.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                    }

                    if (showLanguagesSelector) {
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        ) {
                            AppLanguage.entries.forEach { lang ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.changeLanguage(lang)
                                            showLanguagesSelector = false
                                        }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lang.displayName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (lang == currentLang) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (lang == currentLang) {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Notification Switch toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (notificationState) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(JobaayaLocalization.translate("notification_settings", currentLang), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Alert on connections and directory views", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }

                        Switch(
                            checked = notificationState,
                            onCheckedChange = { notificationState = it }
                        )
                    }
                }
            }

            // Logout row button
            Button(
                onClick = { viewModel.handleLogout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = "Exit App", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(JobaayaLocalization.translate("logout", currentLang), fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
