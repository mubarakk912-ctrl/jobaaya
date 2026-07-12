package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.viewmodel.ChatInbox
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FullscreenVideoDialog(uri: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxWidth().height(450.dp).clip(RoundedCornerShape(16.dp)).background(Color.Black)) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoURI(Uri.parse(uri))
                        val ctrl = MediaController(ctx)
                        ctrl.setAnchorView(this)
                        setMediaController(ctrl)
                        setOnPreparedListener { start() }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Default.Close, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun FullscreenImageDialog(uri: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    Dialog(onDismissRequest = onDismiss) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.8f)).pointerInput(Unit) { detectTransformGestures { _, pan, zoom, _ -> scale = (scale * zoom).coerceIn(1f, 5f); if (scale > 1f) offset += pan else offset = Offset.Zero } }, contentAlignment = Alignment.Center) {
            AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxWidth().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y), contentScale = ContentScale.Fit)
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) { Icon(Icons.Default.Close, null, tint = Color.White) }
        }
    }
}

@Composable
fun ForwardMessageDialog(messages: List<ChatMessage>, profiles: List<UserProfile>, onForward: (List<ChatMessage>, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Forward to...", fontWeight = FontWeight.Bold)
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(profiles) { profile ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onForward(messages, profile.id); onDismiss() }.padding(12.dp)) { Text(profile.name) }
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
fun MessageInfoDialog(message: ChatMessage, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Message Details", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Sent: " + SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(message.timestamp)), fontSize = 12.sp)
                Button(onClick = onDismiss, Modifier.align(Alignment.End)) { Text("Close") }
            }
        }
    }
}

@Composable
fun LocationPickerDialog(deviceLocation: android.location.Location?, myProfile: UserProfile?, onSendLocation: (Double, Double, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Share Location", fontWeight = FontWeight.Bold)
                if (deviceLocation != null) {
                    Text("Real-time device location detected.", fontSize = 12.sp)
                    Button(onClick = { onSendLocation(deviceLocation.latitude, deviceLocation.longitude, "Current Device Location"); onDismiss() }, Modifier.align(Alignment.End)) { Text("Send Live Location") }
                } else if (myProfile != null) {
                    Text("Location off. Sharing your profile address.", fontSize = 12.sp)
                    Text(myProfile.fullAddress, fontSize = 11.sp, color = Color.Gray)
                    Button(onClick = { onSendLocation(myProfile.latitude, myProfile.longitude, myProfile.fullAddress); onDismiss() }, Modifier.align(Alignment.End)) { Text("Send Profile Address") }
                } else {
                    Text("Location unavailable. Please enable GPS.", fontSize = 12.sp)
                    Button(onClick = onDismiss, Modifier.align(Alignment.End)) { Text("Close") }
                }
            }
        }
    }
}

@Composable
fun DirectDealDialog(onSendDeal: (String, String, String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Direct Business Proposal", fontWeight = FontWeight.Bold)
                var title by remember { mutableStateOf("") }
                var budget by remember { mutableStateOf("") }
                var date by remember { mutableStateOf("") }
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Project Title") })
                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("Budget (INR)") })
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Completion Date") })
                Button(onClick = { onSendDeal(title, budget, date); onDismiss() }, Modifier.align(Alignment.End)) { Text("Send Proposal") }
            }
        }
    }
}

@Composable
fun PollCreationDialog(onSendPoll: (String, List<String>) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Text("Create Poll", fontWeight = FontWeight.Bold)
                var q by remember { mutableStateOf("") }
                var opt1 by remember { mutableStateOf("") }
                var opt2 by remember { mutableStateOf("") }
                OutlinedTextField(value = q, onValueChange = { q = it }, label = { Text("Question") })
                OutlinedTextField(value = opt1, onValueChange = { opt1 = it }, label = { Text("Option 1") })
                OutlinedTextField(value = opt2, onValueChange = { opt2 = it }, label = { Text("Option 2") })
                Button(onClick = { onSendPoll(q, listOf(opt1, opt2)); onDismiss() }, Modifier.align(Alignment.End)) { Text("Create Poll") }
            }
        }
    }
}

@Composable
fun DeleteMessagesDialog(selectedMessages: List<ChatMessage>, onDeleteMessages: (List<ChatMessage>) -> Unit, onDismiss: () -> Unit) {
    val allFromMe = selectedMessages.all { it.isFromMe }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(16.dp)); Text("Delete Messages?", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp)); Text("Are you sure you want to delete the selected messages?", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Button(onClick = { onDeleteMessages(selectedMessages); onDismiss() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer), shape = RoundedCornerShape(12.dp)) { Text("Delete for me", fontWeight = FontWeight.Bold) }
                if (allFromMe) { Spacer(Modifier.height(8.dp)); OutlinedButton(onClick = { onDeleteMessages(selectedMessages); onDismiss() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) { Text("Delete for everyone", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) } }
                Spacer(Modifier.height(12.dp)); TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
fun CallingSimulationDialog(activePartner: UserProfile?, type: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(0.dp), colors = CardDefaults.cardColors(containerColor = Color.DarkGray)) {
            Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Box(modifier = Modifier.size(120.dp).clip(CircleShape).background(Color.Gray)) { if (activePartner?.profilePhotoUrl?.isNotBlank() == true) { AsyncImage(activePartner.profilePhotoUrl, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop) } }
                Spacer(Modifier.height(24.dp)); Text(activePartner?.name ?: "User", color = Color.White, style = MaterialTheme.typography.headlineMedium)
                Text(if (type == "VIDEO") "Video Calling..." else "Voice Calling...", color = Color.White.copy(0.7f))
                Spacer(Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) { IconButton(onClick = onDismiss, modifier = Modifier.size(64.dp).background(Color.Red, CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(32.dp)) } }
            }
        }
    }
}

@Composable
fun NewChatDialog(profiles: List<UserProfile>, onSelectProfile: (String) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(20.dp)) {
                Text("Start New Conversation", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.height(400.dp)) {
                    items(profiles) { profile ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onSelectProfile(profile.id); onDismiss() }.padding(vertical = 12.dp, horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer)) { if (profile.profilePhotoUrl.isNotBlank()) { AsyncImage(profile.profilePhotoUrl, null, contentScale = ContentScale.Crop) } }
                            Spacer(Modifier.width(12.dp))
                            Column { Text(profile.name, fontWeight = FontWeight.Bold); Text(profile.profession, fontSize = 12.sp, color = Color.Gray) }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
                Spacer(Modifier.height(16.dp)); TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("CANCEL") }
            }
        }
    }
}

@Composable
fun ImagePreviewDialog(uri: Uri, caption: String, onCaptionChange: (String) -> Unit, onSend: () -> Unit, onCrop: () -> Unit = {}, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E), contentColor = Color.White)) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Send Image", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    IconButton(onClick = onCrop) { Icon(Icons.Default.Crop, "Crop", tint = Color.White, modifier = Modifier.size(28.dp)) }
                }
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black)) { AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit) }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = caption, onValueChange = onCaptionChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("Add a caption...", color = Color.Gray) }, shape = RoundedCornerShape(16.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, cursorColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.DarkGray))
                Spacer(Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = onSend, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("SEND", fontWeight = FontWeight.ExtraBold) }
                }
            }
        }
    }
}

@Composable
fun AudioPreviewDialog(uri: Uri, onSend: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E), contentColor = Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Send Audio", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Spacer(Modifier.height(20.dp))
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AudioFile, null, modifier = Modifier.size(40.dp), tint = Color.White) }
                Spacer(Modifier.height(16.dp)); Text("Audio file selected", fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("CANCEL", color = Color.White, fontWeight = FontWeight.Bold) }
                    Spacer(Modifier.width(12.dp))
                    Button(onClick = onSend, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)) { Text("SEND", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}