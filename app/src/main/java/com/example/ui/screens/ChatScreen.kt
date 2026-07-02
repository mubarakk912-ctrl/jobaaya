package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.widget.MediaController
import android.widget.VideoView
import java.io.File
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PinEnd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shortcut
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Scaffold
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.ChatMessage
import com.example.viewmodel.ChatInbox
import com.example.viewmodel.JobaayaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    var chatTextInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showForwardDialog by remember { mutableStateOf<List<ChatMessage>?>(null) }
    var selectedMessageIds by remember { mutableStateOf(setOf<Int>()) }
    var showInfoMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showChatMenu by remember { mutableStateOf(false) }
    var showCallingDialog by remember { mutableStateOf<String?>(null) } // "VOICE" or "VIDEO"
    var highlightedMessageId by remember { mutableStateOf<Int?>(null) }
    
    var chatSearchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    
    var showLocationPicker by remember { mutableStateOf(false) }
    var showContactPicker by remember { mutableStateOf(false) }
    var showDealDialog by remember { mutableStateOf(false) }
    var showPollDialog by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }

    var inboxSearchQuery by remember { mutableStateOf("") }
    var selectedInboxTab by remember { mutableIntStateOf(0) } // 0: All, 1: Unread, 2: Pinned, 3: Starred

    val filteredInbox = remember(inboxList, inboxSearchQuery, selectedInboxTab) {
        inboxList.filter {
            val matchesSearch = it.partnerProfile.name.contains(inboxSearchQuery, ignoreCase = true) ||
                    it.lastMessage.text.contains(inboxSearchQuery, ignoreCase = true)
            val matchesTab = when (selectedInboxTab) {
                1 -> it.unreadCount > 0
                2 -> it.partnerProfile.isPinned
                3 -> it.lastMessage.isStarred // Simplified
                else -> true
            }
            matchesSearch && matchesTab
        }
    }
    
    val filteredMessages = remember(currentChatMessages, chatSearchQuery) {
        if (chatSearchQuery.isBlank()) currentChatMessages
        else currentChatMessages.filter { it.text.contains(chatSearchQuery, ignoreCase = true) }
    }
    
    // Media Playback & Recording State
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
    var recordingLocked by remember { mutableStateOf(false) }
    
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
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            isPaused = false
            recordingTime = 0
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopRecording(): String? {
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            audioFile?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    LaunchedEffect(currentChatMessages.size) {
        if (currentChatMessages.isNotEmpty()) {
            listState.animateScrollToItem(currentChatMessages.size - 1)
        }
    }

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {
            while (isRecording) {
                delay(1000)
                recordingTime++
            }
        }
    }

    LaunchedEffect(activePlayingId, activePlayingUri) {
        if (activePlayingId != null && activePlayingUri != null) {
            if (activePlayingUri == "simulated_audio_uri") {
                // Simulate playback for dummy URI
                val msg = currentChatMessages.find { it.id == activePlayingId }
                val durationSecs = try { msg?.text?.split("|")?.get(1)?.toInt() ?: 10 } catch(e: Exception) { 10 }
                totalDuration = durationSecs * 1000
                val startTime = System.currentTimeMillis()
                
                while (activePlayingId != null && activePlayingUri == "simulated_audio_uri") {
                    val elapsed = (System.currentTimeMillis() - startTime).toInt()
                    currentPosition = elapsed
                    playbackProgress = (elapsed.toFloat() / totalDuration).coerceIn(0f, 1f)
                    
                    if (playbackProgress >= 1f) {
                        activePlayingId = null
                        activePlayingUri = null
                        break
                    }
                    delay(100)
                }
            } else {
                // Real MediaPlayer monitoring
                while (activePlayingId != null && activePlayingUri != null) {
                    if (mediaPlayer.isPlaying) {
                        currentPosition = mediaPlayer.currentPosition
                        totalDuration = mediaPlayer.duration
                        playbackProgress = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f
                    }
                    delay(100)
                }
            }
        } else {
            playbackProgress = 0f
            currentPosition = 0
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    fun playAudio(messageId: Int, uriString: String) {
        if (uriString.isBlank()) return

        if (activePlayingId == messageId) {
            // Toggle Pause/Stop
            if (uriString == "simulated_audio_uri") {
                activePlayingId = null
                activePlayingUri = null
            } else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                } else {
                    mediaPlayer.start()
                }
            }
            return
        }

        // New playback started
        activePlayingId = messageId
        activePlayingUri = uriString
        
        if (uriString != "simulated_audio_uri") {
            try {
                mediaPlayer.reset()
                // Use File path directly if it's a local file
                if (uriString.startsWith("/")) {
                    mediaPlayer.setDataSource(uriString)
                } else {
                    mediaPlayer.setDataSource(context, Uri.parse(uriString))
                }
                mediaPlayer.prepare()
                mediaPlayer.start()
                mediaPlayer.setOnCompletionListener { 
                    activePlayingId = null
                    activePlayingUri = null
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatScreen", "Playback failed", e)
                activePlayingId = null
                activePlayingUri = null
            }
        }
    }

    val isSelectionMode = selectedMessageIds.isNotEmpty()

    // Activity Result Launchers
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendChatMessage("", "PHOTO", it.toString()) }
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendChatMessage("Video Attachment", "VIDEO", it.toString()) }
    }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendChatMessage("Document Shared", "DOCUMENT", it.toString()) }
    }
    val audioPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.sendChatMessage("Audio Attachment", "VOICE", it.toString()) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        if (audioGranted) {
            startRecording()
        }
    }

    fun checkAndStartRecording() {
        val hasAudioPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasAudioPermission) {
            startRecording()
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.RECORD_AUDIO))
        }
    }

    BackHandler(enabled = isSelectionMode) {
        selectedMessageIds = emptySet()
    }

    BackHandler(enabled = !activeChatUserId.isNullOrEmpty() && !isSelectionMode) {
        viewModel.selectActiveChat(null)
    }

    if (!activeChatUserId.isNullOrEmpty()) {
        val activePartner = inboxList.find { it.partnerProfile.id == activeChatUserId }?.partnerProfile
            ?: otherProfiles.find { it.id == activeChatUserId }
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Selection Bar
            AnimatedVisibility(visible = isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selectedMessageIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                        Text("${selectedMessageIds.size} selected", fontWeight = FontWeight.Bold)
                    }
                    Row {
                        if (selectedMessageIds.size == 1) {
                            val selectedMsg = currentChatMessages.find { it.id == selectedMessageIds.first() }
                            if (selectedMsg != null) {
                                if (selectedMsg.isFromMe && selectedMsg.mediaType == null) {
                                    IconButton(onClick = { 
                                        editingMessage = selectedMsg
                                        chatTextInput = selectedMsg.text
                                        selectedMessageIds = emptySet()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                }
                                IconButton(onClick = { showInfoMessage = selectedMsg }) {
                                    Icon(Icons.Default.Info, contentDescription = "Info")
                                }
                            }
                        }
                        IconButton(onClick = { 
                            showForwardDialog = currentChatMessages.filter { it.id in selectedMessageIds }
                        }) {
                            Icon(Icons.Default.Shortcut, contentDescription = "Forward")
                        }
                        IconButton(onClick = { 
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
                    }
                }
            }

            // Chat Header
            if (!isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectActiveChat(null) }) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onNavigateToProfile(activeChatUserId!!) }
                    ) {
                        if (activePartner?.profilePhotoUrl?.isNotBlank() == true) {
                            AsyncImage(
                                model = activePartner.profilePhotoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onNavigateToProfile(activeChatUserId!!) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(activePartner?.name ?: "Professional Chat", fontWeight = FontWeight.Bold)
                            if (activePartner?.isMuted == true) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.NotificationsOff, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            }
                        }
                        Text(
                            text = if (isTyping) "Typing..." else "Online",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isTyping) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }
                    Box {
                        IconButton(onClick = { showChatMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showChatMenu,
                            onDismissRequest = { showChatMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isSearching) "Close Search" else "Search Chat") },
                                onClick = { showChatMenu = false; isSearching = !isSearching; if (!isSearching) chatSearchQuery = "" },
                                leadingIcon = { Icon(Icons.Default.Search, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (activePartner?.isPinned == true) "Unpin Chat" else "Pin Chat") },
                                onClick = { showChatMenu = false; viewModel.togglePinChat(activeChatUserId!!) },
                                leadingIcon = { Icon(if (activePartner?.isPinned == true) Icons.Default.PinEnd else Icons.Default.PushPin, null) }
                            )
                            DropdownMenuItem(
                                text = { Text(if (activePartner?.isMuted == true) "Unmute Chat" else "Mute Chat") },
                                onClick = { showChatMenu = false; viewModel.toggleMuteChat(activeChatUserId!!) },
                                leadingIcon = { Icon(if (activePartner?.isMuted == true) Icons.Default.NotificationsOff else Icons.Default.NotificationsOff, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Report") },
                                onClick = { 
                                    showChatMenu = false
                                    viewModel.reportUserProfile(activeChatUserId!!)
                                },
                                leadingIcon = { Icon(Icons.Default.Flag, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Block") },
                                onClick = { 
                                    showChatMenu = false
                                    viewModel.blockUserProfile(activeChatUserId!!)
                                    viewModel.selectActiveChat(null)
                                },
                                leadingIcon = { Icon(Icons.Default.Block, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear chat") },
                                onClick = { 
                                    showChatMenu = false
                                    viewModel.clearChat(activeChatUserId!!)
                                },
                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) }
                            )
                        }
                    }
                }
                HorizontalDivider()
                if (isSearching) {
                    OutlinedTextField(
                        value = chatSearchQuery,
                        onValueChange = { chatSearchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        placeholder = { Text("Search messages...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = { IconButton(onClick = { isSearching = false; chatSearchQuery = "" }) { Icon(Icons.Default.Close, null) } },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
            ) {
                items(filteredMessages, key = { it.id }) { msg ->
                    ChatBubble(
                        message = msg,
                        isSelected = selectedMessageIds.contains(msg.id),
                        isHighlighted = highlightedMessageId == msg.id,
                        isSelectionMode = isSelectionMode,
                        isAudioPlaying = activePlayingId == msg.id && (mediaPlayer.isPlaying || msg.mediaUrl == "simulated_audio_uri"),
                        playbackProgress = if (activePlayingId == msg.id) playbackProgress else 0f,
                        currentPosition = if (activePlayingId == msg.id) currentPosition else 0,
                        selectedCount = selectedMessageIds.size,
                        onPlayAudio = { id, uri -> playAudio(id, uri) },
                        onShowVideo = { fullscreenVideoUri = it },
                        onShowImage = { fullscreenImageUri = it },
                        onDelete = { 
                            if (selectedMessageIds.contains(msg.id)) {
                                val toDelete = currentChatMessages.filter { it.id in selectedMessageIds }
                                viewModel.deleteChatMessages(toDelete)
                                selectedMessageIds = emptySet()
                            } else {
                                viewModel.deleteChatMessage(msg)
                            }
                        },
                        onEdit = { editingMessage = msg; chatTextInput = msg.text },
                        onForward = { 
                            if (selectedMessageIds.contains(msg.id)) {
                                showForwardDialog = currentChatMessages.filter { it.id in selectedMessageIds }
                            } else {
                                showForwardDialog = listOf(msg)
                            }
                        },
                        onReply = { replyingToMessage = msg },
                        onToggleSelection = {
                            if (selectedMessageIds.contains(msg.id)) selectedMessageIds -= msg.id
                            else selectedMessageIds += msg.id
                        },
                        onReplyClicked = { replyId: Int ->
                            val index = currentChatMessages.indexOfFirst { it.id == replyId }
                            if (index != -1) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                    highlightedMessageId = replyId
                                    delay(2000)
                                    if (highlightedMessageId == replyId) {
                                        highlightedMessageId = null
                                    }
                                }
                            }
                        },
                        onStarMessage = { viewModel.toggleStarMessage(it) }
                    )
                }
                if (isTyping) item { TypingIndicator() }
            }

            // Reply Preview
            if (replyingToMessage != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(4.dp).height(40.dp).background(MaterialTheme.colorScheme.primary))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (replyingToMessage!!.isFromMe) "You" else activePartner?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(replyingToMessage!!.text.ifEmpty { "Media" }, maxLines = 1, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { replyingToMessage = null }) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                }
            }

            // Attachment Sheet
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
                            MediaAttachmentItem(Icons.Default.ContactPage, "Contact", Color(0xFFFF9800)) { showContactPicker = true; showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.BusinessCenter, "Direct Deal", Color(0xFF673AB7)) { showDealDialog = true; showAttachmentSheet = false }
                            MediaAttachmentItem(Icons.Default.Poll, "Poll", Color(0xFFE91E63)) { showPollDialog = true; showAttachmentSheet = false }
                        }
                    }
                }
            }

            // Input Bar & Voice Note UI
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp)) {
                if (isRecording) {
                    // Recording UI
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Mic, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d:%02d", recordingTime / 60, recordingTime % 60),
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                            if (isPaused) {
                                Text(" (Paused)", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Row {
                            IconButton(onClick = { 
                                mediaRecorder?.stop()
                                mediaRecorder?.release()
                                mediaRecorder = null
                                isRecording = false
                                recordingTime = 0
                                isPaused = false 
                            }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Gray)
                            }
                            IconButton(onClick = { 
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    if (isPaused) {
                                        mediaRecorder?.resume()
                                        isPaused = false
                                    } else {
                                        mediaRecorder?.pause()
                                        isPaused = true
                                    }
                                }
                            }) {
                                Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, null, tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(
                                onClick = { 
                                    val path = stopRecording()
                                    if (path != null) {
                                        viewModel.sendChatMessage("Voice Note|$recordingTime", "VOICE", path)
                                    }
                                    recordingTime = 0
                                    isPaused = false
                                },
                                modifier = Modifier.background(Color(0xFF4CAF50), CircleShape)
                            ) {
                                Icon(Icons.Default.Send, null, tint = Color.White)
                            }
                        }
                    }
                } else {
                    // Standard Input UI
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) { Icon(Icons.Default.AttachFile, null) }
                        
                        OutlinedTextField(
                            value = chatTextInput,
                            onValueChange = { chatTextInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Message") },
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (chatTextInput.isBlank()) {
                            // Simple Mic Button
                            IconButton(
                                onClick = { checkAndStartRecording() },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(Icons.Default.Mic, null, tint = Color.White)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    if (editingMessage != null) { 
                                        viewModel.editChatMessage(editingMessage!!, chatTextInput)
                                        editingMessage = null 
                                    } else { 
                                        viewModel.sendChatMessage(chatTextInput, replyToId = replyingToMessage?.id, replyToText = replyingToMessage?.text?.ifEmpty { "Media" })
                                        replyingToMessage = null 
                                    }
                                    chatTextInput = ""
                                },
                                modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) { Icon(Icons.Default.Send, null, tint = Color.White) }
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showNewChatDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "New Chat")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Professional Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Inbox Search
                    OutlinedTextField(
                        value = inboxSearchQuery,
                        onValueChange = { inboxSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp),
                        placeholder = { Text("Search conversations...", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (inboxSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { inboxSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                }

                // Filter Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedInboxTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    edgePadding = 16.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        if (selectedInboxTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedInboxTab]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    val tabs = listOf("All", "Unread", "Pinned", "Starred")
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedInboxTab == index,
                            onClick = { selectedInboxTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedInboxTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                if (filteredInbox.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "No conversations found",
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = WindowInsets.navigationBars.asPaddingValues()
                    ) {
                        items(filteredInbox, key = { it.partnerProfile.id }) { inbox ->
                            InboxItemRow(inbox) {
                                viewModel.selectActiveChat(inbox.partnerProfile.id)
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 76.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Full Screen Video Player
    if (fullscreenVideoUri != null) {
        Dialog(onDismissRequest = { fullscreenVideoUri = null }) {
            Box(modifier = Modifier.fillMaxWidth().height(450.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
                AndroidView(
                    factory = { ctx ->
                        VideoView(ctx).apply {
                            setVideoURI(Uri.parse(fullscreenVideoUri))
                            val ctrl = MediaController(ctx)
                            ctrl.setAnchorView(this)
                            setMediaController(ctrl)
                            setOnPreparedListener { start() }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(onClick = { fullscreenVideoUri = null }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }

    // Full Screen Image Viewer
    if (fullscreenImageUri != null) {
        Dialog(onDismissRequest = { fullscreenImageUri = null }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { fullscreenImageUri = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = fullscreenImageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { fullscreenImageUri = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }
        }
    }

    // Forward Dialog
    if (showForwardDialog != null) {
        Dialog(onDismissRequest = { showForwardDialog = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Forward to...", fontWeight = FontWeight.Bold)
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(otherProfiles) { profile ->
                            Row(modifier = Modifier.fillMaxWidth().clickable {
                                showForwardDialog?.let { viewModel.forwardChatMessages(it, profile.id) }
                                showForwardDialog = null; selectedMessageIds = emptySet()
                            }.padding(12.dp)) {
                                Text(profile.name)
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    // Message Info Dialog (Top Bar Action)
    if (showInfoMessage != null) {
        Dialog(onDismissRequest = { showInfoMessage = null }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Message Details", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Sent: " + SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(showInfoMessage!!.timestamp)), fontSize = 12.sp)
                    Button(onClick = { showInfoMessage = null }, Modifier.align(Alignment.End)) { Text("Close") }
                }
            }
        }
    }

    // Dialogs for new attachments
    if (showLocationPicker) {
        Dialog(onDismissRequest = { showLocationPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Share Location", fontWeight = FontWeight.Bold)
                    Text("Mocking location fetch...", fontSize = 12.sp)
                    Button(onClick = { 
                        viewModel.sendLocationMessage(28.6139, 77.2090, "Connaught Place, New Delhi")
                        showLocationPicker = false 
                    }, Modifier.align(Alignment.End)) { Text("Send CP, Delhi") }
                }
            }
        }
    }

    if (showContactPicker) {
        Dialog(onDismissRequest = { showContactPicker = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Share Contact", fontWeight = FontWeight.Bold)
                    var name by remember { mutableStateOf("") }
                    var phone by remember { mutableStateOf("") }
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                    Button(onClick = { 
                        viewModel.sendContactMessage(name, phone)
                        showContactPicker = false 
                    }, Modifier.align(Alignment.End)) { Text("Share") }
                }
            }
        }
    }

    if (showDealDialog) {
        Dialog(onDismissRequest = { showDealDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Direct Business Proposal", fontWeight = FontWeight.Bold)
                    var title by remember { mutableStateOf("") }
                    var budget by remember { mutableStateOf("") }
                    var date by remember { mutableStateOf("") }
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") })
                    OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget (INR)") })
                    OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Completion Date") })
                    Button(onClick = { 
                        viewModel.sendDirectDealMessage(title, budget, date)
                        showDealDialog = false 
                    }, Modifier.align(Alignment.End)) { Text("Send Proposal") }
                }
            }
        }
    }

    if (showPollDialog) {
        Dialog(onDismissRequest = { showPollDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Create Poll", fontWeight = FontWeight.Bold)
                    var q by remember { mutableStateOf("") }
                    var opt1 by remember { mutableStateOf("") }
                    var opt2 by remember { mutableStateOf("") }
                    OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("Question") })
                    OutlinedTextField(value = opt1, onValueChange = { opt1 = it }, label = { Text("Option 1") })
                    OutlinedTextField(value = opt2, onValueChange = { opt2 = it }, label = { Text("Option 2") })
                    Button(onClick = { 
                        viewModel.sendPollMessage(q, listOf(opt1, opt2))
                        showPollDialog = false 
                    }, Modifier.align(Alignment.End)) { Text("Create Poll") }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        val selectedMsgs = currentChatMessages.filter { it.id in selectedMessageIds }
        val allFromMe = selectedMsgs.all { it.isFromMe }

        Dialog(onDismissRequest = { showDeleteDialog = false }) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Delete Messages?",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Are you sure you want to delete the selected messages?",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = { 
                            viewModel.deleteChatMessages(selectedMsgs)
                            selectedMessageIds = emptySet()
                            showDeleteDialog = false 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Delete for me", fontWeight = FontWeight.Bold) }

                    if (allFromMe) {
                        Spacer(Modifier.height(8.dp))
                        androidx.compose.material3.OutlinedButton(
                            onClick = { 
                                viewModel.deleteChatMessages(selectedMsgs)
                                android.widget.Toast.makeText(context, "Deleted for everyone", android.widget.Toast.LENGTH_SHORT).show()
                                selectedMessageIds = emptySet()
                                showDeleteDialog = false 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) { 
                            Text(
                                "Delete for everyone", 
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ) 
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.TextButton(
                        onClick = { showDeleteDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { 
                        Text(
                            "Cancel", 
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ) 
                    }
                }
            }
        }
    }

    // Calling Simulation Dialog
    if (showCallingDialog != null) {
        val activePartner = inboxList.find { it.partnerProfile.id == activeChatUserId }?.partnerProfile
            ?: otherProfiles.find { it.id == activeChatUserId }
            
        Dialog(onDismissRequest = { showCallingDialog = null }) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(0.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color.DarkGray)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.Gray)) {
                        if (activePartner?.profilePhotoUrl?.isNotBlank() == true) {
                            AsyncImage(activePartner.profilePhotoUrl, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(activePartner?.name ?: "User", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                    Text(if (showCallingDialog == "VIDEO") "Video Calling..." else "Voice Calling...", color = Color.White.copy(0.7f))
                    
                    Spacer(Modifier.weight(1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { showCallingDialog = null },
                            modifier = Modifier.size(64.dp).background(Color.Red, CircleShape)
                        ) {
                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            }
        }
    }

    // New Chat Dialog
    if (showNewChatDialog) {
        Dialog(onDismissRequest = { showNewChatDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "Start New Conversation",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.height(400.dp)) {
                        items(otherProfiles) { profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectActiveChat(profile.id)
                                        showNewChatDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    if (profile.profilePhotoUrl.isNotBlank()) {
                                        AsyncImage(profile.profilePhotoUrl, null, contentScale = ContentScale.Crop)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(profile.name, fontWeight = FontWeight.Bold)
                                    Text(profile.profession, fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    androidx.compose.material3.TextButton(
                        onClick = { showNewChatDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("CANCEL")
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItemRow(inbox: ChatInbox, onClick: () -> Unit) {
    val partner = inbox.partnerProfile
    val lastMsg = inbox.lastMessage
    val context = LocalContext.current
    
    val timeLabel = remember(lastMsg.timestamp) {
        val now = System.currentTimeMillis()
        val diff = now - lastMsg.timestamp
        when {
            diff < 24 * 60 * 60 * 1000 -> SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastMsg.timestamp))
            diff < 48 * 60 * 60 * 1000 -> "Yesterday"
            else -> SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(lastMsg.timestamp))
        }
    }

    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        leadingContent = {
            Box {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    if (partner.profilePhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = partner.profilePhotoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.align(Alignment.Center),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                // Online Status Indicator
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                )
            }
        },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = partner.name,
                    fontWeight = if (inbox.unreadCount > 0) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (partner.isVerified) {
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(14.dp)
                    )
                }
                if (partner.isPinned) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        Icons.Default.PushPin,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (lastMsg.isFromMe) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (lastMsg.isRead) Color(0xFF00E676) else Color.Gray
                    )
                    Spacer(Modifier.width(4.dp))
                }
                
                val previewPrefix = when (lastMsg.mediaType) {
                    "PHOTO" -> "📷 Photo"
                    "VIDEO" -> "🎥 Video"
                    "VOICE", "AUDIO" -> "🎤 Audio"
                    "LOCATION" -> "📍 Location"
                    "DOCUMENT" -> "📄 Document"
                    "DEAL" -> "💼 Business Deal"
                    "POLL" -> "📊 Poll"
                    else -> ""
                }
                
                Text(
                    text = if (previewPrefix.isNotEmpty()) previewPrefix else lastMsg.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (inbox.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = timeLabel,
                    fontSize = 11.sp,
                    color = if (inbox.unreadCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                    fontWeight = if (inbox.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(Modifier.height(6.dp))
                if (inbox.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${inbox.unreadCount}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (partner.isMuted) {
                    Icon(
                        Icons.Default.NotificationsOff,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray.copy(alpha = 0.6f)
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = if (partner.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else Color.Transparent
        )
    )
}

@Composable
fun TypingIndicator() {
    Text("...", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    isSelected: Boolean,
    isHighlighted: Boolean,
    isSelectionMode: Boolean,
    isAudioPlaying: Boolean,
    playbackProgress: Float,
    currentPosition: Int,
    selectedCount: Int,
    onPlayAudio: (Int, String) -> Unit,
    onShowVideo: (String) -> Unit,
    onShowImage: (String) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onForward: () -> Unit,
    onReply: () -> Unit,
    onToggleSelection: () -> Unit,
    onReplyClicked: (Int) -> Unit,
    onStarMessage: (ChatMessage) -> Unit
) {
    val isMe = message.isFromMe
    var showMenu by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val timeLabel = remember(message.timestamp) { SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)) }

    val bubbleColor = if (isHighlighted) {
        Color(0xFF0B3A51).copy(alpha = 0.8f)
    } else {
        if (isMe) Color(0xFF0B3A51) else Color(0xFF1E1E1E)
    }

    Row(
        modifier = Modifier.fillMaxWidth().background(
            if (isSelected) MaterialTheme.colorScheme.primary.copy(0.1f) 
            else if (isHighlighted) MaterialTheme.colorScheme.secondary.copy(0.2f)
            else Color.Transparent
        )
            .padding(vertical = 2.dp, horizontal = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Icon(if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, modifier = Modifier.size(20.dp).padding(end = 4.dp))
        }
        if (!isMe && !isSelectionMode) {
            IconButton(onClick = onForward, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Shortcut, null, modifier = Modifier.size(18.dp), tint = Color.Gray) }
        }

        Column(modifier = Modifier.widthIn(max = 280.dp), horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (!message.forwardedFrom.isNullOrEmpty()) Text("Forwarded", fontSize = 9.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(bottom = 2.dp))
            Card(
                shape = RoundedCornerShape(16.dp, 16.dp, if (isMe) 4.dp else 16.dp, if (isMe) 16.dp else 4.dp),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                modifier = Modifier.combinedClickable(
                    onClick = {
                        if (isSelectionMode) onToggleSelection()
                        else {
                            when (message.mediaType) {
                                "VOICE", "AUDIO" -> message.mediaUrl?.let { onPlayAudio(message.id, it) }
                                "PHOTO" -> message.mediaUrl?.let { onShowImage(it) }
                                "VIDEO" -> message.mediaUrl?.let { onShowVideo(it) }
                                "DOCUMENT" -> {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.mediaUrl))
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        context.startActivity(intent)
                                    } catch (e: Exception) {}
                                }
                            }
                        }
                    },
                    onLongClick = { 
                        if (!isSelected) onToggleSelection()
                        showMenu = true 
                    }
                )
            ) {
                Column {
                    if (message.replyToId != null) {
                        Box(modifier = Modifier.padding(4.dp).fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.Black.copy(0.15f)).clickable { 
                            onReplyClicked(message.replyToId)
                        }.padding(8.dp)) {
                            Text(message.replyToText ?: "Original Message", fontSize = 10.sp, color = Color.White.copy(0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Box(modifier = Modifier.padding(if (message.mediaType == "PHOTO") 2.dp else 4.dp)) {
                        Column {
                            if (message.isStarred) {
                                Icon(Icons.Default.Star, null, modifier = Modifier.size(10.dp).align(Alignment.End), tint = Color(0xFFFFD700))
                            }
                            
                            Box(modifier = Modifier.widthIn(min = 60.dp)) {
                                Column {
                                    when (message.mediaType) {
                                        "VOICE", "AUDIO" -> {
                                            val parts = message.text.split("|")
                                            val staticDuration = if (parts.size > 1) {
                                                val secs = parts[1].toIntOrNull() ?: 0
                                                String.format("%02d:%02d", secs / 60, secs % 60)
                                            } else "0:00"

                                            VoiceMessageVisualizer(
                                                isMe = isMe,
                                                isPlaying = isAudioPlaying,
                                                progress = playbackProgress,
                                                durationLabel = if (isAudioPlaying && message.mediaUrl != "simulated_audio_uri") {
                                                    String.format("%02d:%02d", (currentPosition / 1000) / 60, (currentPosition / 1000) % 60)
                                                } else staticDuration
                                            )
                                        }
                                        "PHOTO" -> PhotoAttachmentVisualizer(message.mediaUrl, isMe, timeLabel, message.isRead)
                                        "VIDEO" -> VideoAttachmentVisualizer(message.mediaUrl, isMe, timeLabel, message.isRead)
                                        "DOCUMENT" -> DocumentAttachmentVisualizer(isMe, message.text)
                                        "LOCATION" -> LocationAttachmentVisualizer(isMe, message.text, message.mediaUrl)
                                        "CONTACT" -> ContactAttachmentVisualizer(isMe, message.text)
                                        "DEAL" -> DirectDealVisualizer(isMe, message.text)
                                        "POLL" -> PollVisualizer(isMe, message.text)
                                        else -> {
                                            Column(modifier = Modifier.padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)) {
                                                Text(
                                                    text = message.text,
                                                    color = Color.White,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    modifier = Modifier.align(Alignment.End),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (message.isEdited) Text("Edited ", fontSize = 8.sp, color = Color.White.copy(0.5f))
                                                    Text(timeLabel, fontSize = 9.sp, color = Color.White.copy(0.7f), fontWeight = FontWeight.Medium)
                                                    if (isMe) {
                                                        Spacer(Modifier.width(3.dp))
                                                        Text(if (message.isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (message.isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (isMe && !isSelectionMode) {
            IconButton(onClick = onForward, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Shortcut, null, modifier = Modifier.size(18.dp), tint = Color.Gray) }
        }

        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            if (selectedCount <= 1) {
                DropdownMenuItem(
                    text = { Text("Copy") }, 
                    onClick = { 
                        showMenu = false
                        clipboardManager.setText(AnnotatedString(message.text))
                        android.widget.Toast.makeText(context, "Text copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                    }, 
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null) }
                )
                DropdownMenuItem(text = { Text("Reply") }, onClick = { showMenu = false; onReply() }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, null) })
                DropdownMenuItem(text = { Text(if (message.isStarred) "Unstar" else "Star") }, onClick = { showMenu = false; onStarMessage(message) }, leadingIcon = { Icon(if (message.isStarred) Icons.Default.StarBorder else Icons.Default.Star, null) })
                if (isMe) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                }
            }
            DropdownMenuItem(text = { Text(if (selectedCount > 1) "Delete Selection" else "Delete") }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) })
            DropdownMenuItem(text = { Text(if (selectedCount > 1) "Forward Selection" else "Forward") }, onClick = { showMenu = false; onForward() }, leadingIcon = { Icon(Icons.Default.Shortcut, null) })
            DropdownMenuItem(text = { Text("Report Message") }, onClick = { showMenu = false; android.widget.Toast.makeText(context, "Message reported", android.widget.Toast.LENGTH_SHORT).show() }, leadingIcon = { Icon(Icons.Default.Flag, null) })
            DropdownMenuItem(text = { Text("Info") }, onClick = { showMenu = false; showInfoDialog = true }, leadingIcon = { Icon(Icons.Default.Info, null) })
        }
    }

    if (showInfoDialog) {
        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text("Message Details", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text("Sent: " + SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(message.timestamp)), fontSize = 12.sp)
                    Button(onClick = { showInfoDialog = false }, Modifier.align(Alignment.End)) { Text("Close") }
                }
            }
        }
    }
}

@Composable
fun VoiceMessageVisualizer(
    isMe: Boolean,
    isPlaying: Boolean,
    progress: Float,
    durationLabel: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val grayColor = Color.Gray.copy(0.3f)
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            null,
            tint = if (isMe) Color.White else primaryColor,
            modifier = Modifier.size(32.dp)
        )
        
        Box(modifier = Modifier.width(150.dp).height(30.dp).padding(horizontal = 8.dp)) {
            // Background Bars
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = 20
                val barWidth = 4f
                val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)
                
                repeat(barCount) { i ->
                    val h = (10..25).random().dp.toPx()
                    val x = i * (barWidth + spacing)
                    
                    // Determine color based on progress
                    val isPlayed = (i.toFloat() / barCount) < progress
                    val color = if (isMe) {
                        if (isPlayed) Color.White else Color.White.copy(0.4f)
                    } else {
                        if (isPlayed) primaryColor else grayColor
                    }

                    drawLine(
                        color = color,
                        start = Offset(x, (size.height - h) / 2),
                        end = Offset(x, (size.height + h) / 2),
                        strokeWidth = barWidth
                    )
                }
            }
        }
        
        Text(
            text = durationLabel,
            fontSize = 11.sp,
            color = if (isMe) Color.White else Color.Black,
            modifier = Modifier.width(35.dp)
        )
    }
}

@Composable
fun PhotoAttachmentVisualizer(url: String?, isMe: Boolean, time: String, isRead: Boolean) {
    Box(modifier = Modifier.size(240.dp, 240.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)) {
        AsyncImage(url, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(4.dp)) {
            Text(time, fontSize = 9.sp, color = Color.White)
            if (isMe) Text(if (isRead) " ✓✓" else " ✓", fontSize = 10.sp, color = if (isRead) Color.Green else Color.White)
        }
    }
}

@Composable
fun VideoAttachmentVisualizer(url: String?, isMe: Boolean, time: String, isRead: Boolean) {
    Box(modifier = Modifier.size(240.dp, 240.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black)) {
        Icon(Icons.Default.SmartDisplay, null, modifier = Modifier.size(48.dp).align(Alignment.Center), tint = Color.Red)
        Row(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(4.dp)) {
            Text(time, fontSize = 9.sp, color = Color.White)
            if (isMe) Text(if (isRead) " ✓✓" else " ✓", fontSize = 10.sp, color = if (isRead) Color.Green else Color.White)
        }
    }
}

@Composable
fun LocationAttachmentVisualizer(isMe: Boolean, address: String, coords: String?) {
    Column(modifier = Modifier.padding(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = if (isMe) Color.White else Color(0xFF4CAF50))
            Spacer(Modifier.width(8.dp))
            Text(address, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* Open maps intent */ }, modifier = Modifier.fillMaxWidth().height(32.dp), shape = RoundedCornerShape(8.dp)) {
            Text("Open Maps", fontSize = 10.sp)
        }
    }
}

@Composable
fun ContactAttachmentVisualizer(isMe: Boolean, text: String) {
    val parts = text.split("|")
    val name = parts.getOrNull(0) ?: "Contact"
    val phone = parts.getOrNull(1) ?: ""
    Column(modifier = Modifier.padding(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ContactPage, null, tint = if (isMe) Color.White else Color(0xFFFF9800))
            Spacer(Modifier.width(8.dp))
            Text(name, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
        }
        Text(phone, fontSize = 11.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
        Spacer(Modifier.height(8.dp))
        Button(onClick = { /* Call intent */ }, modifier = Modifier.fillMaxWidth().height(32.dp), shape = RoundedCornerShape(8.dp)) {
            Text("Save Contact", fontSize = 10.sp)
        }
    }
}

@Composable
fun DirectDealVisualizer(isMe: Boolean, text: String) {
    val parts = text.split("|")
    val title = parts.getOrNull(0) ?: "Project Deal"
    val budget = parts.getOrNull(1) ?: "N/A"
    val deadline = parts.getOrNull(2) ?: "N/A"
    Card(colors = CardDefaults.cardColors(containerColor = if (isMe) Color.White.copy(0.1f) else Color.White.copy(0.5f))) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text("Business Proposal", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(title, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
            Row {
                Text("Budget: ", fontSize = 11.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
                Text(budget, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
            }
            Text("Deadline: $deadline", fontSize = 11.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { /* View details */ }, modifier = Modifier.fillMaxWidth().height(32.dp)) {
                Text("Review Proposal", fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun PollVisualizer(isMe: Boolean, text: String) {
    val parts = text.split("|")
    val question = parts.getOrNull(0) ?: "Poll Question"
    val options = parts.drop(1)
    Column(modifier = Modifier.padding(4.dp)) {
        Text(question, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
        Spacer(Modifier.height(8.dp))
        options.forEach { option ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isMe) Color.White.copy(0.2f) else Color.Gray.copy(0.1f))
                    .clickable { }
                    .padding(8.dp)
            ) {
                Text(option, fontSize = 11.sp, color = if (isMe) Color.White else Color.Black)
            }
        }
    }
}

@Composable
fun DocumentAttachmentVisualizer(isMe: Boolean, name: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Description, null, tint = if (isMe) Color.White else Color.Red)
        Spacer(Modifier.width(8.dp))
        Column {
            Text(name, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("Open Document", fontSize = 10.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
        }
    }
}

@Composable
fun MediaAttachmentItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(0.2f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
        Text(label, fontSize = 10.sp)
    }
}
