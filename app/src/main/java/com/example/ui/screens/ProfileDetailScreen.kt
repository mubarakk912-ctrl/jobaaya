package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.model.WorkStatus
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel
import java.util.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailScreen(
    viewModel: JobaayaViewModel,
    profileId: String,
    onBack: () -> Unit,
    onStartChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()

    // Query specific profile details
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    
    // Seed and trigger tracking views
    LaunchedEffect(profileId, profiles) {
        val found = profiles.find { it.id == profileId }
        if (found != null && profile == null) {
            profile = found
            viewModel.incrementViewsCount(profileId)
        } else if (found != null) {
            profile = found
        }
    }

    // Interactive Review states
    var reviewerName by remember { mutableStateOf("") }
    var userRatingInput by remember { mutableFloatStateOf(5.0f) }
    var reviewComment by remember { mutableStateOf("") }

    // Dialog flags
    var showQrDialog by remember { mutableStateOf(false) }
    var showHiredDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(profile?.name ?: "Profile Card") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Show QR Code")
                    }
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Connect with ${profile?.name} (${profile?.profession}) on JOBAAYA: https://jobaaya.com/profile/${profile?.id}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Profile Link")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading Profile Card...")
            }
        } else {
            val prof = profile!!
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bio with Name & Star Status
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar representing single Allowed profile photo
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = prof.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = prof.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (prof.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Partner Badge",
                                        tint = Color(0xFF1976D2)
                                    )
                                }
                            }

                            Text(
                                text = prof.profession,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )

                            // Availability bubble
                            Spacer(modifier = Modifier.height(8.dp))
                            val isAvail = prof.availabilityStatus == WorkStatus.AVAILABLE.name
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isAvail) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                border = BorderStroke(1.dp, if (isAvail) Color(0xFF81C784) else Color(0xFFFFB74D))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (isAvail) Color(0xFF4CAF50) else Color(0xFFFF9800), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isAvail) {
                                            JobaayaLocalization.translate("status_available", currentLang)
                                        } else {
                                            JobaayaLocalization.translate("status_busy", currentLang)
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAvail) Color(0xFF2E7D32) else Color(0xFFE65100)
                                    )
                                }
                            }

                            // Star indicator
                            Row(
                                modifier = Modifier.padding(top = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", prof.averageRating),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = " (${prof.reviewCount} ${JobaayaLocalization.translate("reviews", currentLang)})",
                                    color = MaterialTheme.colorScheme.outline,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }

                // Interactive Contact Strip: Call, WhatsApp, Meet/Chat, Connect/Request
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Call launcher
                        QuickContactButton(
                            icon = Icons.Default.Call,
                            label = JobaayaLocalization.translate("call", currentLang),
                            color = Color(0xFF2E7D32),
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:${prof.mobileNumber}")
                                    }
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "DIAL Failed: ${prof.mobileNumber}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // WhatsApp launcher
                        QuickContactButton(
                            icon = Icons.Default.Message,
                            label = JobaayaLocalization.translate("whatsapp", currentLang),
                            color = Color(0xFF25D366),
                            onClick = {
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=${prof.mobileNumber.replace(" ", "")}"
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse(url)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp Dispatch: ${prof.mobileNumber}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // One-to-one Chat
                        QuickContactButton(
                            icon = Icons.Default.Chat,
                            label = JobaayaLocalization.translate("chats", currentLang),
                            color = MaterialTheme.colorScheme.primary,
                            onClick = { onStartChat(prof.id) }
                        )

                        // Follow / Connect
                        val connectsActive = prof.followStatus == 2
                        QuickContactButton(
                            icon = Icons.Default.PersonAdd,
                            label = if (connectsActive) "Connected" else "Connect",
                            color = if (connectsActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                            onClick = { viewModel.toggleConnectWithUser(prof.id) }
                        )
                    }
                }

                // Core Bio Information details list
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            DetailRowLabel("Experience", "${prof.yearsOfExperience} Years of Work Practice")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                            
                            DetailRowLabel("Working Hours", prof.workingHours)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            DetailRowLabel("Languages", prof.languagesRaw)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            DetailRowLabel("Contact Mobile", prof.mobileNumber)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            DetailRowLabel("Contact Email", prof.emailAddress)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                            DetailRowLabel("Office/Home Location", prof.fullAddress)
                            if (prof.isLocationPublic) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable {
                                        // Trigger system maps route directions
                                        try {
                                            val gmmIntentUri = Uri.parse("google.navigation:q=${prof.latitude},${prof.longitude}")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            mapIntent.setPackage("com.google.android.apps.maps")
                                            context.startActivity(mapIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Opening Route coordinates: ${prof.latitude}, ${prof.longitude}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = JobaayaLocalization.translate("directions", currentLang),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                // Skills tag displays
                item {
                    Text(
                        text = "Expertise & Custom Skills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        prof.skills.forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = skill,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }

                // About section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "About & Trade Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prof.aboutSection,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Star Rating & Review addition form
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = JobaayaLocalization.translate("write_review", currentLang),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Name field
                            OutlinedTextField(
                                value = reviewerName,
                                onValueChange = { reviewerName = it },
                                label = { Text("Your Name") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Star Selector Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Rating:")
                                Row {
                                    (1..5).forEach { starIdx ->
                                        IconButton(onClick = { userRatingInput = starIdx.toFloat() }) {
                                            Icon(
                                                imageVector = if (userRatingInput >= starIdx) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = "$starIdx Stars",
                                                tint = Color(0xFFFFB300)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Comment field
                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                label = { Text("Describe experience...") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (reviewerName.isNotBlank() && reviewComment.isNotBlank()) {
                                        viewModel.submitClientReview(
                                            profileId = prof.id,
                                            reviewerName = reviewerName,
                                            rating = userRatingInput,
                                            comment = reviewComment
                                        )
                                        reviewerName = ""
                                        reviewComment = ""
                                    }
                                },
                                enabled = reviewerName.isNotBlank() && reviewComment.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(JobaayaLocalization.translate("submit_review", currentLang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Safety buttons (Report user & Block user)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.reportUserProfile(prof.id)
                                Toast.makeText(context, JobaayaLocalization.translate("sp_report_success", currentLang), Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(JobaayaLocalization.translate("report", currentLang), style = MaterialTheme.typography.bodyMedium)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.blockUserProfile(prof.id)
                                Toast.makeText(context, JobaayaLocalization.translate("sp_block_success", currentLang), Toast.LENGTH_LONG).show()
                                onBack()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(JobaayaLocalization.translate("block", currentLang), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Hire Me CTA Button
                item {
                    Button(
                        onClick = { showHiredDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = "${JobaayaLocalization.translate("hire_me", currentLang)} – ${prof.name}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Modal dialogue for QR Sharing showing dynamic vector blocks drawn on Canvas
    if (showQrDialog && profile != null) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = JobaayaLocalization.translate("qr_share", currentLang),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Scan to instantly download ${profile?.name} digital card",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Draw Premium Graphic QR Code Matrix dynamically on Custom Canvas
                    val textString = "JOBAAYA_PROF_${profile?.id}"
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellSize = size.width / 15f
                            val random = Random(textString.hashCode().toLong())

                            // Draw QR Finder Patterns (Corners)
                            drawFinderPattern(0f, 0f, cellSize)
                            drawFinderPattern(size.width - cellSize * 5, 0f, cellSize)
                            drawFinderPattern(0f, size.height - cellSize * 5, cellSize)

                            // Populate standard randomized pseudo-matrix blocks
                            for (row in 0 until 15) {
                                for (col in 0 until 15) {
                                    // Skip corners
                                    val isNearTopLeft = row < 5 && col < 5
                                    val isNearTopRight = row < 5 && col > 9
                                    val isNearBottomLeft = row > 9 && col < 5
                                    if (isNearTopLeft || isNearTopRight || isNearBottomLeft) continue

                                    if (random.nextBoolean()) {
                                        drawRect(
                                            color = Color.Black,
                                            topLeft = Offset(col * cellSize, row * cellSize),
                                            size = Size(cellSize, cellSize)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = profile?.name ?: "",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = profile?.profession ?: "",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = { showQrDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close Scanner")
                    }
                }
            }
        }
    }

    // Modal success dialogue for Hiring Action
    if (showHiredDialog) {
        Dialog(onDismissRequest = { showHiredDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Booking Dispatched!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = JobaayaLocalization.translate("hiring_success", currentLang),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showHiredDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Awesome")
                    }
                }
            }
        }
    }
}

// Extension to draw standard nested QR anchors on custom canvas
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFinderPattern(
    x: Float,
    y: Float,
    cellSize: Float
) {
    // Outer black boundary
    drawRect(
        color = Color.Black,
        topLeft = Offset(x, y),
        size = Size(cellSize * 5, cellSize * 5)
    )
    // Inner white block
    drawRect(
        color = Color.White,
        topLeft = Offset(x + cellSize, y + cellSize),
        size = Size(cellSize * 3, cellSize * 3)
    )
    // Middle solid pattern
    drawRect(
        color = Color.Black,
        topLeft = Offset(x + cellSize * 1.5f, y + cellSize * 1.5f),
        size = Size(cellSize * 2, cellSize * 2)
    )
}

@Composable
fun QuickContactButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun DetailRowLabel(label: String, valText: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        Text(text = valText, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 2.dp))
    }
}

// Custom Border stroke to avoid imports clashes
@Composable
fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) = 
    androidx.compose.foundation.BorderStroke(width, color)
