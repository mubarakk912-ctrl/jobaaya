package com.example.ui.screens.partnership

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.PartnershipDeal
import com.example.data.model.UserProfile
import com.example.data.model.DealMessage
import com.example.viewmodel.JobaayaViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PartnershipHomeScreen(
    viewModel: JobaayaViewModel,
    onStartDeal: (String) -> Unit,
    onViewDeal: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val myProfile by viewModel.myProfile.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()
    val myDeals by viewModel.getMyDeals().collectAsState()
    
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showActiveDeals by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // TOP DISCLAIMER
        DisclaimerCard()

        // Search for Partnership
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search Professionals...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
            // Section 1: Referral Partner Profile
            item {
                Text(
                    "My Partner Profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White
                )
                PartnerProfileCard(myProfile)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if(showActiveDeals) "My Active Deals" else "Professional Network",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    TextButton(onClick = { showActiveDeals = !showActiveDeals }) {
                        Text(if(showActiveDeals) "View Professionals" else "View My Deals", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (showActiveDeals) {
                if (myDeals.isEmpty()) {
                    item { Text("No deals found.", color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                }
                items(myDeals) { deal ->
                    DealSummaryCard(deal, onViewDeal)
                }
            } else {
                // Section 2: Professional List (reused logic)
                items(profiles) { pro ->
                    ProPartnershipCard(pro, onStartDeal)
                }
            }
        }
    }
}

@Composable
fun DisclaimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("IMPORTANT DISCLAIMER", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "This platform only helps users connect. All payments, commissions, agreements, services, disputes and legal matters are solely between involved users. Platform, company and developer are NOT responsible for any transaction or loss.",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
fun PartnerProfileCard(profile: UserProfile?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary)) {
                if (!profile?.profilePhotoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(profile?.profilePhotoUrl).crossfade(true).build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(profile?.name ?: "Partner Name", fontWeight = FontWeight.Bold, color = Color.White)
                Text(profile?.fullAddress ?: "City", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                    Text(" ${profile?.partnerRating ?: 0.0f}", fontSize = 12.sp, color = Color.White)
                    if (profile?.isVerifiedPartner == true) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProPartnershipCard(pro: UserProfile, onStartDeal: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pro.name, fontWeight = FontWeight.Bold, color = Color.White)
                Text(pro.profession, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text("${pro.fullAddress} • ${pro.yearsOfExperience}Y Exp", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            }
            Button(
                onClick = { onStartDeal(pro.id) },
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Start Deal", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun DealSummaryCard(deal: PartnershipDeal, onViewDeal: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onViewDeal(deal.id) },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00281F))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(deal.title.ifBlank { "Untitled Deal #${deal.id}" }, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Status: ${deal.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DealWorkspaceScreen(
    viewModel: JobaayaViewModel,
    dealId: Int,
    onBack: () -> Unit
) {
    val deal by viewModel.getDealById(dealId).collectAsState(initial = null)
    val messages by viewModel.getDealMessages(dealId).collectAsState(initial = emptyList())
    val myProfile by viewModel.myProfile.collectAsState()
    
    if (deal == null) return

    val isLocked = deal?.status == "Deal Done"
    val isPartner = deal?.partnerId == myProfile?.id
    val myUserId = myProfile?.id ?: ""

    var dealTitle by remember(deal) { mutableStateOf(deal?.title ?: "") }
    var dealDesc by remember(deal) { mutableStateOf(deal?.description ?: "") }
    var workType by remember(deal) { mutableStateOf(deal?.workType ?: "") }
    var terms by remember(deal) { mutableStateOf(deal?.terms ?: "") }
    var notes by remember(deal) { mutableStateOf(deal?.notes ?: "") }
    var commission by remember(deal) { mutableStateOf(deal?.commissionPercentage ?: 5) }
    
    var chatInput by remember { mutableStateOf("") }
    var showChat by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deal Workspace #${dealId}") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) }
                },
                actions = {
                    IconButton(onClick = { showChat = !showChat }) { 
                        BadgedBox(badge = { if(messages.isNotEmpty()) Badge { Text("${messages.size}") } }) {
                            Icon(Icons.Default.Chat, null) 
                        }
                    }
                    if (isLocked) {
                        IconButton(onClick = { /* Print/PDF placeholder */ }) { Icon(Icons.Default.PictureAsPdf, null) }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (showChat) {
                // DEAL CHAT SECTION
                Column(modifier = Modifier.weight(1f).background(MaterialTheme.colorScheme.surface)) {
                    Text("Private Deal Chat", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                    LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                        items(messages) { msg ->
                            val isMe = msg.senderId == myUserId
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalAlignment = if(isMe) Alignment.End else Alignment.Start
                            ) {
                                Surface(
                                    color = if(isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(msg.text, modifier = Modifier.padding(8.dp), color = if(isMe) Color.White else MaterialTheme.colorScheme.onSecondaryContainer)
                                }
                                Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(msg.timestamp)), fontSize = 8.sp, color = Color.Gray)
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = chatInput,
                            onValueChange = { chatInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message...") },
                            shape = RoundedCornerShape(20.dp)
                        )
                        IconButton(onClick = {
                            if(chatInput.isNotBlank()) {
                                viewModel.sendDealMessage(dealId, chatInput)
                                chatInput = ""
                            }
                        }) { Icon(Icons.Default.Send, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    DisclaimerCard()

                    // Edit Request Banner
                    if (deal?.editRequestActive == true) {
                        val requesterId = deal?.editRequestFromId
                        val isIRequester = requesterId == myUserId
                        
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(if(isIRequester) "Waiting for other party to approve edit." else "Other party requested to edit this deal.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                if (!isIRequester) {
                                    Button(onClick = { viewModel.updateDeal(deal!!.copy(status = "Edited", editRequestActive = false), "Edit Approved") }) {
                                        Text("Approve")
                                    }
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        val effectivelyLocked = isLocked && !deal!!.editRequestActive && deal?.status != "Edited"
                        
                        DealTextField("Deal Title", dealTitle, effectivelyLocked) { dealTitle = it }
                        DealTextField("Work Type", workType, effectivelyLocked) { workType = it }
                        DealTextField("Description", dealDesc, effectivelyLocked, true) { dealDesc = it }
                        DealTextField("Terms & Conditions", terms, effectivelyLocked, true) { terms = it }
                        DealTextField("Internal Notes", notes, effectivelyLocked, true) { notes = it }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Action Buttons
                        if (!effectivelyLocked) {
                            Button(
                                onClick = {
                                    viewModel.updateDeal(deal!!.copy(
                                        title = dealTitle,
                                        description = dealDesc,
                                        workType = workType,
                                        terms = terms,
                                        notes = notes,
                                        commissionPercentage = commission
                                    ), "Draft Saved")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Save Changes") }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val iAgreed = if(isPartner) deal?.partnerAgreed else deal?.proAgreed
                                Button(
                                    onClick = {
                                        val updated = if(isPartner) deal!!.copy(partnerAgreed = true) else deal!!.copy(proAgreed = true)
                                        viewModel.updateDeal(updated, "Agreed")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = if(iAgreed == true) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(if(iAgreed == true) Icons.Default.CheckCircle else Icons.Default.CheckBoxOutlineBlank, null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("I Agree")
                                }

                                Button(
                                    onClick = {
                                        val updated = if(isPartner) deal!!.copy(partnerDone = true) else deal!!.copy(proDone = true)
                                        val finalized = updated.partnerDone && updated.proDone
                                        val finalDeal = if(finalized) updated.copy(status = "Deal Done", finalizedAt = System.currentTimeMillis()) else updated
                                        viewModel.updateDeal(finalDeal, if(finalized) "Deal Done" else "Marked Done")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                                ) {
                                    Text("Deal Done")
                                }
                            }
                        } else {
                            // Locked state actions
                            Button(
                                onClick = { viewModel.updateDeal(deal!!.copy(editRequestActive = true, editRequestFromId = myUserId), "Edit Requested") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                            ) {
                                Icon(Icons.Default.Edit, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Request Edit")
                            }
                        }
                        
                        // Audit Trail Toggle
                        var showAudit by remember { mutableStateOf(false) }
                        TextButton(onClick = { showAudit = !showAudit }, modifier = Modifier.padding(top = 16.dp)) {
                            Text(if(showAudit) "Hide Audit Trail" else "Show Audit Trail")
                        }
                        
                        if (showAudit) {
                            val logs by viewModel.getAuditLogs(dealId).collectAsState(initial = emptyList())
                            logs.forEach { log ->
                                Text("${SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(log.timestamp))}: ${log.action}", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DealTextField(label: String, value: String, isLocked: Boolean, isLarge: Boolean = false, onValueChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        if (isLocked) {
            Text(value.ifBlank { "Not provided" }, modifier = Modifier.padding(vertical = 8.dp), color = Color.White)
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                minLines = if(isLarge) 3 else 1,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}
