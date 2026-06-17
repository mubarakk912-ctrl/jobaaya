package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.WorkStatus
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel

@Composable
fun MapScreen(
    viewModel: JobaayaViewModel,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()

    var selectedMapProfile by remember { mutableStateOf<UserProfile?>(null) }
    var locationPrivacyActive by remember { mutableStateOf(myProfile?.isLocationPublic ?: true) }

    // Coordinates bounding box for New Delhi (Center ~ 28.59, 77.16)
    // Map dimensions mapping
    val mapCenterLat = 28.59
    val mapCenterLon = 77.16
    val scaleLat = 0.5f // degrees range height
    val scaleLon = 0.5f // degrees range width

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // MAP CONTROLS HEADER Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = JobaayaLocalization.translate("near_me", currentLang),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Interactive GPS coordinates of active service providers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(12.dp))

                // GPS Privacy controls togglers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (locationPrivacyActive) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = JobaayaLocalization.translate("gps_locate", currentLang),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Switch(
                        checked = locationPrivacyActive,
                        onCheckedChange = {
                            locationPrivacyActive = it
                            myProfile?.let { me ->
                                viewModel.updateMyProfessionalProfile(me.copy(isLocationPublic = it))
                            }
                            Toast.makeText(
                                context,
                                if (it) "Your public GPS coordinates are visible." else "Location privacy enabled. You are now incognito.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }

        // DESIGN MAP CANVAS WITH VECTOR DRAWINGS REPRESENTATIONS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                .background(Color(0xFFE3F2FD)) // Beautiful Soft Blue representing river/waterways grid layout
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                // 1. Draw Grid Lines (City block structures)
                val strokeGrad = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                for (i in 1..6) {
                    val x = (canvasW / 7) * i
                    drawLine(Color(0xFFBBDEFB), Offset(x, 0f), Offset(x, canvasH), strokeWidth = 1f)
                    val y = (canvasH / 7) * i
                    drawLine(Color(0xFFBBDEFB), Offset(0f, y), Offset(canvasW, y), strokeWidth = 1f)
                }

                // 2. Draw mock Ring Roads / Highway visual structures
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    center = Offset(canvasW / 2, canvasH / 2),
                    radius = canvasW / 3.5f,
                    style = Stroke(width = 8f)
                )

                // 3. Draw Yamuna River blue curve visual model
                val riverOffset1 = Offset(canvasW * 0.8f, 0f)
                val riverOffset2 = Offset(canvasW * 0.6f, canvasH)
                drawLine(
                    color = Color(0xFF90CAF9),
                    start = riverOffset1,
                    end = riverOffset2,
                    strokeWidth = 24f // custom thick stream line
                )

                // 4. Draw Radar scan range rings centered at Me
                val sonarRingRadius = canvasW / 5f
                drawCircle(
                    color = Color(0xFF1E88E5).copy(alpha = 0.15f),
                    center = Offset(canvasW / 2, canvasH / 2),
                    radius = sonarRingRadius,
                    style = Stroke(width = 3f)
                )

                // 5. If a profile is highlighted, draw direct flight routing vectors!
                selectedMapProfile?.let { prof ->
                    val meLat = myProfile?.latitude ?: 28.7159
                    val meLon = myProfile?.longitude ?: 77.1006
                    val pairMe = translateLatLonToXY(meLat, meLon, mapCenterLat, mapCenterLon, scaleLat, scaleLon, canvasW, canvasH)
                    val pairTarget = translateLatLonToXY(prof.latitude, prof.longitude, mapCenterLat, mapCenterLon, scaleLat, scaleLon, canvasW, canvasH)

                    drawLine(
                        color = Color(0xFF2E7D32),
                        start = pairMe,
                        end = pairTarget,
                        strokeWidth = 4f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f))
                    )
                }
            }

            // Draw ME indicator at center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp)
                    .background(Color.White, CircleShape)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Position",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Draw other dynamic target markers
            profiles.forEach { profile ->
                if (profile.isLocationPublic && !profile.isMe) {
                    // Coordinates mapping trigonometry simulation
                    var xPct = ((profile.longitude - (mapCenterLon - scaleLon / 2)) / scaleLon).coerceIn(0.1, 0.9)
                    var yPct = (1.0 - ((profile.latitude - (mapCenterLat - scaleLat / 2)) / scaleLat)).coerceIn(0.1, 0.9)

                    val activeSelected = selectedMapProfile?.id == profile.id

                    Box(
                        modifier = Modifier
                            .align(
                                BiasAlignment(
                                    horizontalBias = ((xPct * 2) - 1).toFloat(),
                                    verticalBias = ((yPct * 2) - 1).toFloat()
                                )
                            )
                            .size(if (activeSelected) 40.dp else 44.dp)
                            .clickable { selectedMapProfile = profile }
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = profile.name,
                                tint = if (activeSelected) Color(0xFFE53935) else Color(0xFF1E88E5),
                                modifier = Modifier.size(28.dp)
                            )
                            Card(
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.75f)),
                                modifier = Modifier.padding(top = 1.dp)
                            ) {
                                Text(
                                    text = profile.name.split(" ").first(),
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Map Footer info showing the list of nearby users or selected user detail row
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (selectedMapProfile != null) {
                val sProf = selectedMapProfile!!
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.background),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = sProf.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = sProf.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = sProf.profession,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Close highlighted drawer action
                        IconButton(onClick = { onProfileClick(sProf.id) }) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = "Get details",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            } else {
                // Horizontal quick list scroll of nearby professionals
                Column {
                    Text(
                        text = "People in Your Proximity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(profiles) { prof ->
                            if (!prof.isMe) {
                                Surface(
                                    modifier = Modifier
                                        .clickable { selectedMapProfile = prof }
                                        .width(180.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.secondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = prof.name.take(1).uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                text = prof.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = prof.profession,
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.primary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
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

// Function helper inside file scope
private fun translateLatLonToXY(
    lat: Double,
    lon: Double,
    centerLat: Double,
    centerLon: Double,
    scaleLat: Float,
    scaleLon: Float,
    canvasW: Float,
    canvasH: Float
): Offset {
    val xPct = ((lon - (centerLon - scaleLon / 2)) / scaleLon).coerceIn(0.1, 0.9)
    val yPct = (1.0 - ((lat - (centerLat - scaleLat / 2)) / scaleLat)).coerceIn(0.1, 0.9)
    return Offset((xPct * canvasW).toFloat(), (yPct * canvasH).toFloat())
}

// Custom bias alignment converter classes to place overlay items simply
private class BiasAlignment(
    private val horizontalBias: Float,
    private val verticalBias: Float
) : androidx.compose.ui.Alignment {
    override fun align(
        size: androidx.compose.ui.unit.IntSize,
        space: androidx.compose.ui.unit.IntSize,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection
    ): androidx.compose.ui.unit.IntOffset {
        val centerX = (space.width - size.width) / 2
        val centerY = (space.height - size.height) / 2
        val x = centerX + (centerX * horizontalBias).toInt()
        val y = centerY + (centerY * verticalBias).toInt()
        return androidx.compose.ui.unit.IntOffset(x, y)
    }
}
