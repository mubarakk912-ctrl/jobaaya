package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.AddAPhoto
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.model.WorkStatus
import com.example.ui.components.PhotoFitDialog
import com.example.ui.localization.JobaayaLocalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.viewmodel.JobaayaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    val myProfile by viewModel.myProfile.collectAsState()

    // Query specific profile details
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    val profileReviews by (profile?.let { viewModel.getProfileReviews(it.id) } ?: kotlinx.coroutines.flow.flowOf(emptyList())).collectAsState(initial = emptyList())

    // Seed and trigger tracking views
    LaunchedEffect(profileId, profiles, myProfile) {
        if (myProfile?.id == profileId) {
            profile = myProfile
        } else {
            val found = profiles.find { it.id == profileId }
            if (found != null && profile == null) {
                profile = found
                viewModel.incrementViewsCount(profileId)
            } else if (found != null) {
                profile = found
            }
        }
    }

    // Interactive Review states
    var reviewerName by remember { mutableStateOf("") }
    var userRatingInput by remember { mutableFloatStateOf(5.0f) }
    var reviewComment by remember { mutableStateOf("") }

    // Dialog flags
    var showQrDialog by remember { mutableStateOf(false) }

    val isMe = myProfile?.id == profileId
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showFitDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            showFitDialog = true
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 40.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Navigation Icon on Left
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                // Action Icons on Right
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showQrDialog = true }) {
                        Icon(Icons.Default.QrCode, contentDescription = "Show QR Code")
                    }
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Connect with ${profile?.name} (${profile?.profession}) on jobaaya: https://jobaaya.com/profile/${profile?.id}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Profile Link")
                    }
                }
            }
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar representing single Allowed profile photo
                            // [बदलाव: यहाँ से .clickable और लॉन्चर ट्रिगर हटा दिया गया है ताकि प्रीव्यू पूरी तरह रीड-ओनली रहे]
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (prof.profilePhotoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(if (isMe) java.io.File(prof.profilePhotoUrl) else prof.profilePhotoUrl)
                                            .crossfade(true)
                                            .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                                            .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                                            .build(),
                                        contentDescription = prof.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    // Khali Placeholder with Calculator Action color
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
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
                // [बदलाव: यदि यूजर खुद अपनी प्रोफाइल देख रहा है (isMe), तो कॉल/चैट/कनेक्ट बटन्स पूरी तरह छुप जाएंगे]
                if (!isMe) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Call launcher
                            QuickContactButton(
                                icon = Icons.Default.Call,
                                label = JobaayaLocalization.translate("call", currentLang),
                                color = Color(0xFF01796F),
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

                            // One-to-one Chat
                            QuickContactButton(
                                icon = Icons.Default.Chat,
                                label = JobaayaLocalization.translate("chats", currentLang),
                                color = Color(0xFF01796F),
                                onClick = { onStartChat(prof.id) }
                            )

                            // Follow / Connect
                            val connectsActive = prof.followStatus == 2
                            QuickContactButton(
                                icon = Icons.Default.PersonAdd,
                                label = if (connectsActive) JobaayaLocalization.translate("connected", currentLang) else JobaayaLocalization.translate("connect", currentLang),
                                color = if (connectsActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                                onClick = { viewModel.toggleConnectWithUser(prof.id) }
                            )
                        }
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
                            // Experience Box inside Profile Details
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Work, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(text = "Experience", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                        Text(text = "${prof.yearsOfExperience} Years of Professional Practice", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

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
                                            Toast.makeText(context, "Directions not available.", Toast.LENGTH_SHORT).show()
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
                                text = "About & Service Description",
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
                    Text(
                        text = "Client Testimonials (${profileReviews.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (profileReviews.isEmpty()) {
                    item {
                        Text(
                            text = "No reviews yet. Be the first to share your experience!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                } else {
                    items(profileReviews) { review ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = review.reviewerName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Row {
                                        repeat(5) { index ->
                                            Icon(
                                                imageVector = if (index < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = review.reviewText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(review.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }

                // [बदलाव: यदि यूजर खुद की प्रोफाइल का प्रीव्यू देख रहा है (isMe), तो नीचे वाला रिव्यू फॉर्म पूरी तरह छुप जाएगा]
                if (!isMe) {
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
                }

                // Safety buttons (Report user & Block user)
                // [बदलाव: खुद की प्रोफाइल पर ब्लॉक और रिपोर्ट बटन्स का कोई तुक नहीं बनता, इसलिए इन्हें भी !isMe में डाल दिया गया है]
                if (!isMe) {
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
                    val textString = "jobaaya_PROF_${profile?.id}"
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

    if (showFitDialog && selectedImageUri != null) {
        PhotoFitDialog(
            uri = selectedImageUri!!,
            onDismiss = { showFitDialog = false },
            onConfirm = { finalUri, scale, ox, oy ->
                viewModel.uploadProfilePhoto(finalUri, scale, ox, oy)
                showFitDialog = false
            }
        )
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

@Preview(showBackground = true)
@Composable
fun ProfileDetailPreview() {
    val mockProfile = UserProfile(
        name = "Rahul Verma",
        profession = "Software Engineer",
        skillsRaw = "Kotlin, Android, Firebase, Jetpack Compose",
        mobileNumber = "+91 99988 77766",
        emailAddress = "rahul@jobaaya.com",
        fullAddress = "Cyber City, Gurgaon, 122002",
        latitude = 28.4950,
        longitude = 77.0890,
        yearsOfExperience = 5,
        languagesRaw = "English, Hindi",
        aboutSection = "Professional Android Developer with 5 years experience in building high-scale apps.",
        isVerified = true,
        averageRating = 5.0f,
        reviewCount = 3
    )

    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Profile Preview (Mock Data)", style = MaterialTheme.typography.titleLarge)
            DetailRowLabel("Full Name", mockProfile.name)
            DetailRowLabel("Profession", mockProfile.profession)
            DetailRowLabel("Location", mockProfile.fullAddress)
            DetailRowLabel("Mobile", mockProfile.mobileNumber)
        }
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