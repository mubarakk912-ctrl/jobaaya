package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import java.io.File
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Shortcut
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.viewmodel.JobaayaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: JobaayaViewModel,
    modifier: Modifier = Modifier,
    onNavigateToProfile: (String) -> Unit = {}
) {
    val inboxList by viewModel.chatInboxList.collectAsState()
    val activeChatUserId by viewModel.activeChatUserId.collectAsState()
    val currentChatMessages by viewModel.activeChatMessages.collectAsState()
    val isTyping by viewModel.isPartnerTyping.collectAsState()
    val otherProfiles by viewModel.filteredProfiles.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val deviceLocation by viewModel.deviceLocation.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<com.example.data.model.ChatMessage?>(null) }
    var replyingToMessage by remember { mutableStateOf<com.example.data.model.ChatMessage?>(null) }
    var showForwardDialog by remember { mutableStateOf<List<com.example.data.model.ChatMessage>?>(null) }
    var selectedMessageIds by remember { mutableStateOf(setOf<Int>()) }
    var showInfoMessage by remember { mutableStateOf<com.example.data.model.ChatMessage?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showCallingDialog by remember { mutableStateOf<String?>(null) }
    var highlightedMessageId by remember { mutableStateOf<Int?>(null) }

    var chatSearchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    var showLocationPicker by remember { mutableStateOf(false) }
    var showDealDialog by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }

    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingAudioUri by remember { mutableStateOf<Uri?>(null) }
    var imageCaption by remember { mutableStateOf("") }

    var inboxSearchQuery by remember { mutableStateOf("") }
    var selectedInboxTab by remember { mutableIntStateOf(0) }

    val filteredInbox = remember(inboxList, inboxSearchQuery, selectedInboxTab) {
        inboxList.filter {
            val matchesSearch = it.partnerProfile.name.contains(inboxSearchQuery, ignoreCase = true) ||
                    it.lastMessage.text.contains(inboxSearchQuery, ignoreCase = true)
            val matchesTab = when (selectedInboxTab) {
                1 -> it.unreadCount > 0
                2 -> it.partnerProfile.isPinned
                3 -> it.lastMessage.isStarred
                else -> true
            }
            matchesSearch && matchesTab
        }
    }

    val filteredMessages = remember(currentChatMessages, chatSearchQuery) {
        if (chatSearchQuery.isBlank()) currentChatMessages
        else currentChatMessages.filter { it.text.contains(chatSearchQuery, ignoreCase = true) }
    }

    var activePlayingId by remember { mutableStateOf<Int?>(null) }
    var activePlayingUri by remember { mutableStateOf<String?>(null) }
    var currentPosition by remember { mutableIntStateOf(0) }
    var totalDuration by remember { mutableIntStateOf(0) }
    var playbackProgress by remember { mutableFloatStateOf(0f) }

    var fullscreenVideoUri by remember { mutableStateOf<String?>(null) }
    var fullscreenImageUri by remember { mutableStateOf<String?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val mediaPlayer = remember { MediaPlayer() }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var audioFile by remember { mutableStateOf<File?>(null) }
    val context = LocalContext.current

    fun startRecording() {
        try {
            val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.mp3")
            audioFile = file
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            isPaused = false
            recordingTime = 0
        } catch (e: Exception) { Log.e("ChatScreen", "startRecording error", e) }
    }

    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply { stop(); release() }
            mediaRecorder = null
            isRecording = false
            audioFile?.absolutePath
        } catch (e: Exception) { Log.e("ChatScreen", "stopRecording error", e); null }
    }

    LaunchedEffect(currentChatMessages.size) { if (currentChatMessages.isNotEmpty()) listState.animateScrollToItem(currentChatMessages.size - 1) }

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (isRecording) { delay(1000L); recordingTime++ }
        }
    }

    LaunchedEffect(activePlayingId, activePlayingUri) {
        if (activePlayingId != null && activePlayingUri != null) {
            while (activePlayingId != null && activePlayingUri != null) {
                if (mediaPlayer.isPlaying) {
                    currentPosition = mediaPlayer.currentPosition
                    totalDuration = mediaPlayer.duration
                    playbackProgress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f
                } else if (!mediaPlayer.isPlaying && playbackProgress < 0.99f && activePlayingId != null) {
                    // Keep seeker active
                } else if (playbackProgress >= 0.99f) {
                    activePlayingId = null
                    activePlayingUri = null
                }
                delay(100L)
            }
        } else { playbackProgress = 0f; currentPosition = 0 }
    }

    DisposableEffect(Unit) { onDispose { mediaPlayer.release() } }

    fun playAudio(messageId: Int, uriString: String) {
        if (uriString.isBlank()) return
        if (activePlayingId == messageId) {
            if (mediaPlayer.isPlaying) mediaPlayer.pause() else mediaPlayer.start()
            return
        }
        activePlayingId = messageId
        activePlayingUri = uriString
        try {
            mediaPlayer.reset()
            if (uriString.startsWith("/")) mediaPlayer.setDataSource(uriString)
            else mediaPlayer.setDataSource(context, uriString.toUri())
            mediaPlayer.prepare()
            mediaPlayer.start()
            mediaPlayer.setOnCompletionListener { activePlayingId = null; activePlayingUri = null }
        } catch (_: Exception) { activePlayingId = null; activePlayingUri = null }
    }

    val isSelectionMode = selectedMessageIds.isNotEmpty()

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { pendingImageUri = it } }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.sendChatMessage("Video Attachment", "VIDEO", it.toString()) } }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { viewModel.sendChatMessage("Document Shared", "DOCUMENT", it.toString()) } }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let { pendingAudioUri = it } }
    val contactPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        uri?.let {
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                    val name = if (nameIndex >= 0) cursor.getString(nameIndex) else "Unknown Contact"
                    viewModel.sendContactMessage(name, "Shared from phone")
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.RECORD_AUDIO] == true) startRecording()
    }

    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) pendingImageUri = result.uriContent
    }

    fun checkAndStartRecording() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startRecording()
        else permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
    }

    BackHandler(enabled = isSelectionMode) { selectedMessageIds = emptySet() }
    BackHandler(enabled = !activeChatUserId.isNullOrEmpty() && !isSelectionMode) { viewModel.selectActiveChat(null) }

    if (!activeChatUserId.isNullOrEmpty()) {
        val activePartner = inboxList.find { it.partnerProfile.id == activeChatUserId }?.partnerProfile ?: otherProfiles.find { it.id == activeChatUserId }

        Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AnimatedVisibility(visible = isSelectionMode) {
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedMessageIds = emptySet() }) { Icon(Icons.Default.Close, null) }
                        Text("${selectedMessageIds.size} selected", fontWeight = FontWeight.Bold)
                    }
                    Row {
                        if (selectedMessageIds.size == 1) {
                            val selectedMsg = currentChatMessages.find { it.id == selectedMessageIds.first() }
                            if (selectedMsg != null) {
                                if (selectedMsg.isFromMe && selectedMsg.mediaType == null) {
                                    IconButton(onClick = { editingMessage = selectedMsg; chatTextInput = selectedMsg.text; selectedMessageIds = emptySet() }) { Icon(Icons.Default.Edit, "Edit") }
                                }
                                IconButton(onClick = { showInfoMessage = selectedMsg }) { Icon(Icons.Default.Info, "Info") }
                            }
                        }
                        IconButton(onClick = { showForwardDialog = currentChatMessages.filter { it.id in selectedMessageIds } }) { Icon(Icons.AutoMirrored.Filled.Shortcut, "Forward") }
                        IconButton(onClick = { showDeleteDialog = true }) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red) }
                    }
                }
            }

            if (!isSelectionMode) {
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.selectActiveChat(null) }) { Icon(Icons.Default.Close, "Back") }
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer).clickable { onNavigateToProfile(activeChatUserId!!) }) {
                        if (activePartner?.profilePhotoUrl?.isNotBlank() == true) { AsyncImage(model = activePartner.profilePhotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f).clickable { onNavigateToProfile(activeChatUserId!!) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activePartner?.name ?: "Professional Chat", fontWeight = FontWeight.Bold)
                            if (activePartner?.isMuted == true) { Spacer(Modifier.width(4.dp)); Icon(Icons.Default.NotificationsOff, null, modifier = Modifier.size(14.dp), tint = Color.Gray) }
                        }
                        Text(text = if (isTyping) "Typing..." else "Online", style = MaterialTheme.typography.labelSmall, color = if (isTyping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                    }
                    Box {
                        IconButton(onClick = { showChatMenu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                        DropdownMenu(expanded = showChatMenu, onDismissRequest = { showChatMenu = false }) {
                            DropdownMenuItem(text = { Text(if (isSearching) "Close Search" else "Search Chat") }, onClick = { showChatMenu = false; isSearching = !isSearching; if (!isSearching) chatSearchQuery = "" }, leadingIcon = { Icon(Icons.Default.Search, null) })
                            DropdownMenuItem(text = { Text(if (activePartner?.isPinned == true) "Unpin Chat" else "Pin Chat") }, onClick = { showChatMenu = false; viewModel.togglePinChat(activeChatUserId!!) }, leadingIcon = { Icon(if (activePartner?.isPinned == true) Icons.Default.PinEnd else Icons.Default.PushPin, null) })
                            DropdownMenuItem(text = { Text(if (activePartner?.isMuted == true) "Unmute Chat" else "Mute Chat") }, onClick = { showChatMenu = false; viewModel.toggleMuteChat(activeChatUserId!!) }, leadingIcon = { Icon(Icons.Default.NotificationsOff, null) })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Report") }, onClick = { showChatMenu = false; viewModel.reportUserProfile(activeChatUserId!!) }, leadingIcon = { Icon(Icons.Default.Flag, null) })
                            DropdownMenuItem(text = { Text("Block") }, onClick = { showChatMenu = false; viewModel.blockUserProfile(activeChatUserId!!); viewModel.selectActiveChat(null) }, leadingIcon = { Icon(Icons.Default.Block, null) })
                            DropdownMenuItem(text = { Text("Clear chat") }, onClick = { showChatMenu = false; viewModel.clearChat() }, leadingIcon = { Icon(Icons.Default.DeleteSweep, null) })
                        }
                    }
                }
                HorizontalDivider()
                if (isSearching) {
                    OutlinedTextField(value = chatSearchQuery, onValueChange = { chatSearchQuery = it }, modifier = Modifier.fillMaxWidth().padding(8.dp), placeholder = { Text("Search messages...") }, leadingIcon = { Icon(Icons.Default.Search, null) }, trailingIcon = { IconButton(onClick = { isSearching = false; chatSearchQuery = "" }) { Icon(Icons.Default.Close, null) } }, shape = RoundedCornerShape(12.dp), singleLine = true)
                }
            }

            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)) {
                items(filteredMessages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        isSelected = selectedMessageIds.contains(msg.id),
                        isHighlighted = highlightedMessageId == msg.id,
                        isSelectionMode = isSelectionMode,
                        isAudioPlaying = activePlayingId == msg.id && (mediaPlayer.isPlaying || playbackProgress < 0.99f),
                        playbackProgress = if (activePlayingId == msg.id) playbackProgress else 0f,
                        currentPosition = if (activePlayingId == msg.id) currentPosition else 0,
                        selectedCount = selectedMessageIds.size,
                        onPlayAudio = { id, uri -> playAudio(id, uri) },
                        onShowVideo = { fullscreenVideoUri = it },
                        onShowImage = { fullscreenImageUri = it },
                        onDelete = { if (selectedMessageIds.contains(msg.id)) { viewModel.deleteChatMessages(currentChatMessages.filter { it.id in selectedMessageIds }); selectedMessageIds = emptySet() } else viewModel.deleteChatMessage(msg) },
                        onEdit = { editingMessage = msg; chatTextInput = msg.text },
                        onForward = { showForwardDialog = if (selectedMessageIds.contains(msg.id)) currentChatMessages.filter { it.id in selectedMessageIds } else listOf(msg) },
                        onReply = { replyingToMessage = msg },
                        onToggleSelection = { if (selectedMessageIds.contains(msg.id)) selectedMessageIds -= msg.id else selectedMessageIds += msg.id },
                        onReplyClicked = { replyId ->
                            val index = currentChatMessages.indexOfFirst { it.id == replyId }
                            if (index != -1) coroutineScope.launch { listState.animateScrollToItem(index); highlightedMessageId = replyId; delay(2000L); if (highlightedMessageId == replyId) highlightedMessageId = null }
                        },
                        onStarMessage = { viewModel.toggleStarMessage(it) },
                        onSeekAudio = { pos -> if (activePlayingId == msg.id) { mediaPlayer.seekTo((pos * totalDuration).toInt()) } }
                    )
                }
                if (isTyping) item { TypingIndicator() }
            }

            if (replyingToMessage != null) {
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(4.dp).height(40.dp).background(MaterialTheme.colorScheme.primary)); Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) { Text(if (replyingToMessage!!.isFromMe) "You" else activePartner?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 12.sp); Text(replyingToMessage!!.text.ifEmpty { "Media" }, maxLines = 1, style = MaterialTheme.typography.bodySmall) }
                    IconButton(onClick = { replyingToMessage = null }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                }
            }

            AnimatedVisibility(visible = showAttachmentSheet) {
                Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Column {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            MediaAttachmentItem(Icons.Default.Mic, "Audio", Color.Red) { audioPicker.launch("audio/*"); showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.CameraAlt, "Photo", Color.Cyan) { imagePicker.launch("image/*"); showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.SmartDisplay, "Video", Color.Magenta) { videoPicker.launch("video/*"); showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.Description, "File", Color.Blue) { docPicker.launch("*/*"); showAttachmentSheet = false }
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            MediaAttachmentItem(Icons.Default.LocationOn, "Location", Color(0xFF4CAF50)) { showLocationPicker = true; showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.ContactPage, "Contact", Color(0xFFFF9800)) { contactPicker.launch(null); showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.BusinessCenter, "Direct Deal", Color(0xFF673AB7)) { showDealDialog = true; showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.Poll, "Poll", Color(0xFFE91E63)) { showPollDialog = true; showAttachmentSheet = false }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp)) {
                if (isRecording) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Mic, null, tint = Color.Red, modifier = Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(text = String.format(java.util.Locale.getDefault(), "%02d:%02d", recordingTime / 60, recordingTime % 60), color = Color.Red, fontWeight = FontWeight.Bold)
                            if (isPaused) Text(" (Paused)", fontSize = 10.sp, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = { mediaRecorder?.stop(); mediaRecorder?.release(); mediaRecorder = null; isRecording = false; recordingTime = 0; isPaused = false }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                            IconButton(onClick = { if (isPaused) { mediaRecorder?.resume(); isPaused = false } else { mediaRecorder?.pause(); isPaused = true } }) { Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = MaterialTheme.colorScheme.primary) }
                            IconButton(onClick = { val path = stopRecording(); if (path != null) viewModel.sendChatMessage("Voice Note|$recordingTime", "VOICE", path); recordingTime = 0; isPaused = false }, modifier = Modifier.background(Color(0xFF4CAF50), CircleShape)) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White) }
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) { Icon(Icons.Default.AttachFile, null) }
                        OutlinedTextField(value = chatTextInput, onValueChange = { chatTextInput = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message") }, shape = RoundedCornerShape(24.dp), maxLines = 4)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (chatTextInput.isBlank()) IconButton(onClick = { checkAndStartRecording() }, modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape)) { Icon(Icons.Default.Mic, null, tint = Color.White) }
                        else IconButton(onClick = { if (editingMessage != null) { viewModel.editChatMessage(editingMessage!!, chatTextInput); editingMessage = null } else { viewModel.sendChatMessage(chatTextInput, replyToId = replyingToMessage?.id, replyToText = replyingToMessage?.text?.ifEmpty { "Media" }); replyingToMessage = null }; chatTextInput = "" }, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)) { Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White) }
                    }
                }
            }
        }
    } else {
        Scaffold(floatingActionButton = { FloatingActionButton(onClick = { showNewChatDialog = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White, shape = CircleShape) { Icon(Icons.Default.PersonAdd, "New Chat") } }) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = "Messages", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface); Spacer(Modifier.height(12.dp))
                    OutlinedTextField(value = inboxSearchQuery, onValueChange = { inboxSearchQuery = it }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), placeholder = { Text("Search conversations...", fontSize = 14.sp) }, leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) }, trailingIcon = { if (inboxSearchQuery.isNotEmpty()) IconButton(onClick = { inboxSearchQuery = "" }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp)) } }, shape = RoundedCornerShape(12.dp), singleLine = true, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                }
                ScrollableTabRow(selectedTabIndex = selectedInboxTab, containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.primary, edgePadding = 16.dp, divider = {}, indicator = { tabPositions -> if (selectedInboxTab < tabPositions.size) TabRowDefaults.SecondaryIndicator(modifier = Modifier.tabIndicatorOffset(tabPositions[selectedInboxTab]), color = MaterialTheme.colorScheme.primary) }) {
                    listOf("All", "Unread", "Pinned", "Starred").forEachIndexed { index: Int, title: String -> Tab(selected = selectedInboxTab == index, onClick = { selectedInboxTab = index }, text = { Text(text = title, fontSize = 17.sp, color = Color.White, fontWeight = if (selectedInboxTab == index) FontWeight.Bold else FontWeight.Medium) }) }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                if (filteredInbox.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)); Spacer(Modifier.height(12.dp)); Text("No conversations found", color = MaterialTheme.colorScheme.outline) } } }
                else { LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = WindowInsets.navigationBars.asPaddingValues()) { items(filteredInbox, key = { it.partnerProfile.id }) { inbox -> InboxItemRow(inbox) { viewModel.selectActiveChat(inbox.partnerProfile.id) }; HorizontalDivider(modifier = Modifier.padding(start = 76.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)) } } }
            }
        }
    }

    if (fullscreenVideoUri != null) FullscreenVideoDialog(fullscreenVideoUri!!) { fullscreenVideoUri = null }
    if (fullscreenImageUri != null) FullscreenImageDialog(fullscreenImageUri!!) { fullscreenImageUri = null }
    if (showForwardDialog != null) ForwardMessageDialog(showForwardDialog!!, otherProfiles, { msgs, id -> viewModel.forwardChatMessages(msgs, id) }) { showForwardDialog = null; selectedMessageIds = emptySet() }
    if (showInfoMessage != null) MessageInfoDialog(showInfoMessage!!) { showInfoMessage = null }
    if (showLocationPicker) LocationPickerDialog(deviceLocation, myProfile, { lat, lon, addr -> viewModel.sendLocationMessage(lat, lon, addr) }) { showLocationPicker = false }
    if (showDealDialog) DirectDealDialog({ t, b, d -> viewModel.sendDirectDealMessage(t, b, d) }) { showDealDialog = false }
    if (showPollDialog) PollCreationDialog({ q, opts -> viewModel.sendPollMessage(q, opts) }) { showPollDialog = false }
    if (showDeleteDialog) DeleteMessagesDialog(currentChatMessages.filter { it.id in selectedMessageIds }, { msgs -> viewModel.deleteChatMessages(msgs); selectedMessageIds = emptySet() }) { showDeleteDialog = false }
    if (showCallingDialog != null) { val activePartner = inboxList.find { it.partnerProfile.id == activeChatUserId }?.partnerProfile ?: otherProfiles.find { it.id == activeChatUserId }; CallingSimulationDialog(activePartner, showCallingDialog!!) { showCallingDialog = null } }
    if (showNewChatDialog) NewChatDialog(otherProfiles, { id -> viewModel.selectActiveChat(id) }) { showNewChatDialog = false }

    if (pendingImageUri != null) {
        ImagePreviewDialog(uri = pendingImageUri!!, caption = imageCaption, onCaptionChange = { imageCaption = it }, onSend = { viewModel.sendChatMessage(imageCaption, "PHOTO", pendingImageUri.toString()); pendingImageUri = null; imageCaption = "" }, onCrop = { cropImageLauncher.launch(CropImageContractOptions(uri = pendingImageUri, cropImageOptions = CropImageOptions(guidelines = CropImageView.Guidelines.ON, fixAspectRatio = false, initialCropWindowPaddingRatio = 0.1f, aspectRatioX = 16, aspectRatioY = 9))) }, onDismiss = { pendingImageUri = null; imageCaption = "" })
    }

    if (pendingAudioUri != null) {
        AudioPreviewDialog(uri = pendingAudioUri!!, onSend = { viewModel.sendChatMessage("Audio Attachment", "VOICE", pendingAudioUri.toString()); pendingAudioUri = null }, onDismiss = { pendingAudioUri = null })
    }
}
