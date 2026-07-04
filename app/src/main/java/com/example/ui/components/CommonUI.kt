package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

@Composable
fun PhotoFitDialog(uri: Uri, onDismiss: () -> Unit, onConfirm: (Uri, Float, Float, Float) -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ADJUST PROFILE PHOTO", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Save, null, tint = Color.Transparent)
                    }
                }

                // Interaction Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset = Offset(
                                    x = offset.x + pan.x,
                                    y = offset.y + pan.y
                                )
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offset.x,
                                translationY = offset.y
                            ),
                        contentScale = ContentScale.Fit,
                        alpha = 0.3f
                    )
                    
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)))
                    
                    Box(
                        modifier = Modifier
                            .size(300.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .clip(CircleShape)
                            .background(Color.DarkGray)
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF121212))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Zoom & Drag to Fit", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("-", color = Color.White, fontSize = 20.sp)
                        Slider(
                            value = scale,
                            onValueChange = { scale = it },
                            valueRange = 1f..5f,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF00A38E)
                            )
                        )
                        Text("+", color = Color.White, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = { onConfirm(uri, scale, offset.x, offset.y) },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00A38E),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save Photo")
                        }
                    }
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}
