package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.viewmodel.ChatInbox
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InboxItemRow(inbox: ChatInbox, onClick: () -> Unit) {
    val partner = inbox.partnerProfile
    val lastMsg = inbox.lastMessage

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
                                        "PHOTO" -> PhotoAttachmentVisualizer(message.mediaUrl, message.text, isMe, timeLabel, message.isRead)
                                        "VIDEO" -> VideoAttachmentVisualizer(message.mediaUrl, message.text, isMe, timeLabel, message.isRead)
                                        "DOCUMENT" -> DocumentAttachmentVisualizer(isMe, message.text, timeLabel, message.isRead)
                                        "LOCATION" -> LocationAttachmentVisualizer(isMe, message.text, message.mediaUrl, timeLabel, message.isRead)
                                        "CONTACT" -> ContactAttachmentVisualizer(isMe, message.text, timeLabel, message.isRead)
                                        "DEAL" -> DirectDealVisualizer(isMe, message.text, timeLabel, message.isRead)
                                        "POLL" -> PollVisualizer(isMe, message.text, timeLabel, message.isRead)
                                        else -> {
                                            Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp).fillMaxWidth()) {
                                                Column(modifier = Modifier.fillMaxWidth()) {
                                                    Text(
                                                        text = message.text,
                                                        color = Color.White,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Spacer(modifier = Modifier.height(20.dp)) // Increased space for timestamp
                                                }
                                                Row(
                                                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
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
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) } )
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
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barCount = 20
                val barWidth = 4f
                val spacing = (size.width - (barCount * barWidth)) / (barCount - 1)

                repeat(barCount) { i ->
                    val h = (10..25).random().dp.toPx()
                    val x = i * (barWidth + spacing)

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
fun PhotoAttachmentVisualizer(url: String?, caption: String, isMe: Boolean, time: String, isRead: Boolean) {
    Column(modifier = Modifier.width(240.dp).clip(RoundedCornerShape(12.dp))) {
        Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            AsyncImage(
                model = url,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            if (caption.isBlank()) {
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, fontSize = 9.sp, color = Color.White)
                    if (isMe) {
                        Spacer(Modifier.width(3.dp))
                        Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
                    }
                }
            }
        }
        if (caption.isNotBlank()) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                Column {
                    Text(text = caption, color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(20.dp)) // Space for timestamp
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, fontSize = 9.sp, color = Color.White.copy(0.7f))
                    if (isMe) {
                        Spacer(Modifier.width(3.dp))
                        Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun VideoAttachmentVisualizer(url: String?, caption: String, isMe: Boolean, time: String, isRead: Boolean) {
    Column(modifier = Modifier.width(240.dp).clip(RoundedCornerShape(12.dp))) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f).background(Color.Black)) {
            Icon(Icons.Default.SmartDisplay, null, modifier = Modifier.size(48.dp).align(Alignment.Center), tint = Color.Red)
            if (caption.isBlank()) {
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).background(Color.Black.copy(0.4f), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, fontSize = 9.sp, color = Color.White)
                    if (isMe) {
                        Spacer(Modifier.width(3.dp))
                        Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
                    }
                }
            }
        }
        if (caption.isNotBlank()) {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp)) {
                Column {
                    Text(text = caption, color = Color.White, fontSize = 14.sp)
                    Spacer(Modifier.height(20.dp)) // Space for timestamp
                }
                Row(
                    modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, fontSize = 9.sp, color = Color.White.copy(0.7f))
                    if (isMe) {
                        Spacer(Modifier.width(3.dp))
                        Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
                    }
                }
            }
        }
    }
}

@Composable
fun LocationAttachmentVisualizer(isMe: Boolean, address: String, coords: String?, time: String, isRead: Boolean) {
    Box(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = if (isMe) Color.White else Color(0xFF4CAF50))
                Spacer(Modifier.width(8.dp))
                Text(address, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(40.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Open Maps", fontSize = 12.sp)
            }
            Spacer(Modifier.height(20.dp)) // Space for timestamp
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(time, fontSize = 9.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
            if (isMe) {
                Spacer(Modifier.width(3.dp))
                Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
            }
        }
    }
}

@Composable
fun ContactAttachmentVisualizer(isMe: Boolean, text: String, time: String, isRead: Boolean) {
    val parts = text.split("|")
    val name = parts.getOrNull(0) ?: "Contact"
    val phone = parts.getOrNull(1) ?: ""
    Box(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ContactPage, null, tint = if (isMe) Color.White else Color(0xFFFF9800))
                Spacer(Modifier.width(8.dp))
                Text(name, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
            }
            Text(phone, fontSize = 11.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
            Spacer(Modifier.height(8.dp))
            Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(32.dp), shape = RoundedCornerShape(8.dp)) {
                Text("Save Contact", fontSize = 10.sp)
            }
            Spacer(Modifier.height(20.dp)) // Space for timestamp
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(time, fontSize = 9.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
            if (isMe) {
                Spacer(Modifier.width(3.dp))
                Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
            }
        }
    }
}

@Composable
fun DirectDealVisualizer(isMe: Boolean, text: String, time: String, isRead: Boolean) {
    val parts = text.split("|")
    val title = parts.getOrNull(0) ?: "Project Deal"
    val budget = parts.getOrNull(1) ?: "N/A"
    val deadline = parts.getOrNull(2) ?: "N/A"
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        modifier = Modifier.fillMaxWidth().padding(4.dp)
    ) {
        Box(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
            Column {
                Text("Business Proposal", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Budget: ", fontSize = 15.sp, color = Color.White.copy(0.7f))
                    Text(budget, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                }
                Text("Deadline: $deadline", fontSize = 15.sp, color = Color.White.copy(0.7f))
                Spacer(Modifier.height(16.dp))
                Button(onClick = { }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("Review Proposal", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(20.dp)) // Space for timestamp
            }
            Row(
                modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(time, fontSize = 9.sp, color = Color.White.copy(0.7f))
                if (isMe) {
                    Spacer(Modifier.width(3.dp))
                    Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
                }
            }
        }
    }
}

@Composable
fun PollVisualizer(isMe: Boolean, text: String, time: String, isRead: Boolean) {
    val parts = text.split("|")
    val question = parts.getOrNull(0) ?: "Poll Question"
    val options = parts.drop(1)
    Box(modifier = Modifier.padding(4.dp).fillMaxWidth()) {
        Column {
            Text(question, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black)
            Spacer(Modifier.height(8.dp))
            options.forEach { option ->
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(8.dp)).background(if (isMe) Color.White.copy(0.2f) else Color.Gray.copy(0.1f)).padding(8.dp)) {
                    Text(option, fontSize = 11.sp, color = if (isMe) Color.White else Color.Black)
                }
            }
            Spacer(Modifier.height(20.dp)) // Space for timestamp
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(time, fontSize = 9.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
            if (isMe) {
                Spacer(Modifier.width(3.dp))
                Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
            }
        }
    }
}

@Composable
fun DocumentAttachmentVisualizer(isMe: Boolean, name: String, time: String, isRead: Boolean) {
    Box(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Description, null, tint = if (isMe) Color.White else Color.Red)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(name, fontWeight = FontWeight.Bold, color = if (isMe) Color.White else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Open Document", fontSize = 10.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
                Spacer(Modifier.height(20.dp)) // Space for timestamp
            }
        }
        Row(
            modifier = Modifier.align(Alignment.BottomEnd).offset(y = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(time, fontSize = 9.sp, color = if (isMe) Color.White.copy(0.7f) else Color.Gray)
            if (isMe) {
                Spacer(Modifier.width(3.dp))
                Text(if (isRead) "✓✓" else "✓", fontSize = 10.sp, color = if (isRead) Color(0xFF00E676) else Color.White.copy(0.6f))
            }
        }
    }
}

@Composable
fun MediaAttachmentItem(icon: ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(0.2f)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = color) }
        Text(label, fontSize = 10.sp)
    }
}