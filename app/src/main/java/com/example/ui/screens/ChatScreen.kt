package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.ChatInbox
import com.example.viewmodel.JobaayaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: JobaayaViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val inboxList by viewModel.chatInboxList.collectAsState()
    val activeChatUserId by viewModel.activeChatUserId.collectAsState()
    val currentChatMessages by viewModel.activeChatMessages.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }

    // Visual back navigation from direct chat
    if (!activeChatUserId.isNullOrEmpty()) {
        val activePartner = inboxList.find { it.partnerProfile.id == activeChatUserId }?.partnerProfile
        
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Active Chat header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.selectActiveChat(null) }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Chat")
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = activePartner?.name?.take(2)?.uppercase() ?: "P",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activePartner?.name ?: "Professional Chat",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color(0xFF4CAF50), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Online status",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            HorizontalDivider()

            // Chat content area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentChatMessages) { msg ->
                    ChatBubble(message = msg)
                }
            }

            // Animated Attachment option list
            AnimatedVisibility(visible = showAttachmentSheet) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MediaAttachmentItem(
                            icon = Icons.Default.Mic,
                            label = "Voice message",
                            color = Color(0xFFEF5350),
                            onClick = {
                                viewModel.sendChatMessage("Voice Message (0:12)", "VOICE", "simulated_wav")
                                showAttachmentSheet = false
                            }
                        )
                        MediaAttachmentItem(
                            icon = Icons.Default.CameraAlt,
                            label = "Photo snap",
                            color = Color(0xFF26A69A),
                            onClick = {
                                viewModel.sendChatMessage("Shared a Photo Card", "PHOTO", "simulated_jpeg")
                                showAttachmentSheet = false
                            }
                        )
                        MediaAttachmentItem(
                            icon = Icons.Default.SmartDisplay,
                            label = "Video file",
                            color = Color(0xFFAB47BC),
                            onClick = {
                                viewModel.sendChatMessage("Video clip shared", "VIDEO", "simulated_mp4")
                                showAttachmentSheet = false
                            }
                        )
                        MediaAttachmentItem(
                            icon = Icons.Default.Description,
                            label = "PDF Doc",
                            color = Color(0xFF42A5F5),
                            onClick = {
                                viewModel.sendChatMessage("Work_Proposal_Quote.pdf (1.2 MB)", "DOCUMENT", "simulated_pdf")
                                showAttachmentSheet = false
                            }
                        )
                    }
                }
            }

            // Chat input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attachment Options",
                        tint = if (showAttachmentSheet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                OutlinedTextField(
                    value = chatTextInput,
                    onValueChange = { chatTextInput = it },
                    placeholder = { Text("Write message here...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (chatTextInput.isNotBlank()) {
                            viewModel.sendChatMessage(chatTextInput)
                            chatTextInput = ""
                        }
                    },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    } else {
        // Conversation lists (Inbox views)
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            Text(
                text = "Conversations Inbox",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Secure local peer-to-peer chats",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (inboxList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardVoice,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your inbox is empty. Meet high-quality tradespeople to start chatting!",
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
                    items(inboxList) { inbox ->
                        InboxItemRow(
                            inbox = inbox,
                            onClick = { viewModel.selectActiveChat(inbox.partnerProfile.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItemRow(
    inbox: ChatInbox,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = inbox.partnerProfile.name.take(2).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inbox.partnerProfile.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val timeString = remember(inbox.lastMessage.timestamp) {
                        try {
                            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(inbox.lastMessage.timestamp))
                        } catch (e: Exception) {
                            "Now"
                        }
                    }
                    Text(
                        text = timeString,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = inbox.lastMessage.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (inbox.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                        fontWeight = if (inbox.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )

                    if (inbox.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
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
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isMe = message.isFromMe
    val timeLabel = remember(message.timestamp) {
        try {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
        } catch (e: Exception) {
            "Just now"
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Check if it is a localized media type attachment
                    if (!message.mediaType.isNullOrEmpty()) {
                        when (message.mediaType) {
                            "VOICE" -> VoiceMessageVisualizer(isMe = isMe)
                            "PHOTO" -> PhotoAttachmentVisualizer()
                            "VIDEO" -> VideoAttachmentVisualizer()
                            "DOCUMENT" -> DocumentAttachmentVisualizer(isMe = isMe, name = message.text)
                        }
                    } else {
                        // Standard text query string
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Time and tick mark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.outline
                )
                if (isMe) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "✓✓",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Draw dynamic Voice wave bars on Canvas
@Composable
fun VoiceMessageVisualizer(isMe: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = if (isMe) Color.White else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Canvas(modifier = Modifier.size(width = 120.dp, height = 30.dp)) {
            val cellWidth = size.width / 16f
            val spacing = 2f
            val strokeWidth = cellWidth - spacing
            val barHeights = listOf(10f, 25f, 15f, 30f, 20f, 35f, 18f, 12f, 28f, 22f, 32f, 15f, 10f, 5f)
            
            barHeights.forEachIndexed { idx, barH ->
                val x = idx * cellWidth
                val y1 = (size.height - barH) / 2
                drawLine(
                    color = if (isMe) Color.White else Color.Black.copy(alpha = 0.5f),
                    start = Offset(x, y1),
                    end = Offset(x, y1 + barH),
                    strokeWidth = strokeWidth
                )
            }
        }

        Text(
            text = "0:12",
            fontSize = 11.sp,
            color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun PhotoAttachmentVisualizer() {
    Column {
        Box(
            modifier = Modifier
                .size(height = 130.dp, width = 220.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Received Photo file",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun VideoAttachmentVisualizer() {
    Column {
        Box(
            modifier = Modifier
                .size(height = 130.dp, width = 220.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(40.dp), tint = Color.Red)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Received Video clip",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DocumentAttachmentVisualizer(isMe: Boolean, name: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Description,
            contentDescription = null,
            tint = if (isMe) Color.White else Color.Red,
            modifier = Modifier.size(28.dp)
        )
        Column {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "PDF Document",
                fontSize = 10.sp,
                color = if (isMe) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun MediaAttachmentItem(
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
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
