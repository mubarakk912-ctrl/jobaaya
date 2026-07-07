package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UserProfile
import com.example.data.model.WorkStatus
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.JobaayaLocalization
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JobaayaViewModel

@Composable
fun HomeScreen(
    viewModel: JobaayaViewModel,
    onProfileClick: (String) -> Unit,
    onStartChat: (String) -> Unit,
    modifier: Modifier = Modifier,
    onNearMeClick: () -> Unit = {}
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.availableCategories.collectAsState()

    val context = LocalContext.current

    val selectedAvail by viewModel.filterAvailability.collectAsState()
    val selectedRating by viewModel.filterRating.collectAsState()
    val selectedExp by viewModel.filterExperience.collectAsState()
    val selectedDistance by viewModel.filterDistanceKm.collectAsState()

    HomeContent(
        currentLang = currentLang,
        myProfile = myProfile,
        profiles = profiles,
        searchQuery = searchQuery,
        selectedAvail = selectedAvail,
        selectedRating = selectedRating,
        selectedExp = selectedExp,
        selectedDistance = selectedDistance,
        onSearchQueryChange = { viewModel.setSearchQuery(it) },
        onFilterAvailChange = { viewModel.setFilterAvailability(it) },
        onFilterRatingChange = { viewModel.setFilterRating(it) },
        onFilterExpChange = { viewModel.setFilterExperience(it) },
        onFilterDistanceChange = { viewModel.setFilterDistance(it) },
        onProfileClick = onProfileClick,
        onChatClick = onStartChat,
        onBookmarkClick = { viewModel.toggleBookmarkProfile(it) },
        categories = categories,
        modifier = modifier,
        onNearMeClick = onNearMeClick
    )
}

@Composable
fun HomeContent(
    currentLang: AppLanguage,
    myProfile: UserProfile?,
    profiles: List<UserProfile>,
    searchQuery: String,
    selectedAvail: String,
    selectedRating: Float,
    selectedExp: Int,
    selectedDistance: Float,
    onSearchQueryChange: (String) -> Unit,
    onFilterAvailChange: (String) -> Unit,
    onFilterRatingChange: (Float) -> Unit,
    onFilterExpChange: (Int) -> Unit,
    onFilterDistanceChange: (Float) -> Unit,
    onProfileClick: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onBookmarkClick: (String) -> Unit,
    categories: List<String>,
    modifier: Modifier = Modifier,
    onNearMeClick: () -> Unit = {}
) {
    var showFilters by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        
        Spacer(modifier = Modifier.height(2.dp))

        // Very Compact Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(JobaayaLocalization.translate("search_placeholder", currentLang), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Near Me Button - Integrated and Compact
                        Surface(
                            onClick = onNearMeClick,
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0B3A51), // Deep Teal
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(JobaayaLocalization.translate("near_me", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (showFilters) Color(0xFF0B3A51) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .testTag("search_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0B3A51),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = JobaayaLocalization.translate("refine_search", currentLang), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("${JobaayaLocalization.translate("experience", currentLang)} (Min: $selectedExp Years)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Slider(value = selectedExp.toFloat(), onValueChange = { onFilterExpChange(it.toInt()) }, valueRange = 0f..20f, steps = 20)

                    Text("Max Distance Filters: ${selectedDistance.toInt()} km", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                    Slider(value = selectedDistance, onValueChange = onFilterDistanceChange, valueRange = 5f..150f)

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Minimum Star Rating: ${if (selectedRating == 0f) "All" else "$selectedRating★"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                        Row {
                            listOf(0.0f, 4.0f, 4.5f).forEach { stars ->
                                val active = selectedRating == stars
                                Surface(
                                    modifier = Modifier.padding(horizontal = 4.dp).clickable { onFilterRatingChange(stars) },
                                    color = if (active) Color(0xFF0B3A51) else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Text(text = if (stars == 0.0f) JobaayaLocalization.translate("all", currentLang) else "$stars★", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        val categoriesScroll = rememberScrollState()
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(categoriesScroll).padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val active = (cat == "All" && searchQuery.isBlank()) || searchQuery.equals(cat, ignoreCase = true)
                Surface(
                    modifier = Modifier.clickable { onSearchQueryChange(if (cat == "All") "" else cat) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (active) Color(0xFF0B3A51).copy(alpha = 0.1f) else Color.Transparent,
                    border = BorderStroke(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) Color(0xFF0B3A51) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (cat == "All") JobaayaLocalization.translate("all", currentLang) else cat,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (active) Color(0xFF0B3A51) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (profiles.isEmpty()) {
            Column(modifier = Modifier.weight(1f).fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = JobaayaLocalization.translate("no_results", currentLang), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 16.dp))
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(profiles) { profile ->
                    Column {
                        ProfileListItem(
                            profile = profile,
                            currentLang = currentLang,
                            onClick = { onProfileClick(profile.id) },
                            onChatClick = { onChatClick(profile.id) },
                            onBookmarkClick = { onBookmarkClick(profile.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileListItem(
    profile: UserProfile,
    currentLang: AppLanguage,
    onClick: () -> Unit,
    onChatClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    val context = LocalContext.current
    var showDetailedSkills by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Section: Avatar, Name, Profession
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.profilePhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(if (profile.isMe) java.io.File(profile.profilePhotoUrl) else profile.profilePhotoUrl)
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                                .build(),
                            contentDescription = profile.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (profile.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Profile",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        text = profile.profession,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF00A38E),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Professional Save Button (Shortlist)
                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (profile.bookmarkStatus) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Shortlist",
                        tint = if (profile.bookmarkStatus) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Middle Section: Rating, Location, Get Direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", profile.averageRating),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = " (${profile.reviewCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = profile.fullAddress.split(",").last().trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.weight(1f))

                // Get Direction
                Row(
                    modifier = Modifier.clickable {
                        try {
                            val gmmIntentUri = Uri.parse("google.navigation:q=${profile.latitude},${profile.longitude}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        } catch (e: Exception) { }
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF00A38E)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = JobaayaLocalization.translate("directions", currentLang),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00A38E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expertise & Custom Skills Button
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF015F56).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { showDetailedSkills = !showDetailedSkills }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = JobaayaLocalization.translate("skills", currentLang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (showDetailedSkills) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Skills Details
            AnimatedVisibility(visible = showDetailedSkills) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        profile.skills.forEach { skill ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF00A38E).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF00A38E).copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = skill,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00A38E)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Bottom Buttons: Chat, Call
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3A51))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = JobaayaLocalization.translate("chats", currentLang), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        try {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${profile.mobileNumber}")
                            }
                            context.startActivity(dialIntent)
                        } catch (e: Exception) { }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3A51))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = JobaayaLocalization.translate("call", currentLang), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyApplicationTheme {
        HomeContent(
            currentLang = AppLanguage.ENGLISH,
            myProfile = null,
            profiles = listOf(
                UserProfile(
                    id = "1",
                    name = "John Doe",
                    profession = "Electrician",
                    skillsRaw = "Wiring, Repairs, Installation",
                    mobileNumber = "1234567890",
                    emailAddress = "john@example.com",
                    fullAddress = "123 Main St, City",
                    latitude = 0.0,
                    longitude = 0.0,
                    yearsOfExperience = 5,
                    languagesRaw = "English, Spanish",
                    aboutSection = "Expert electrician",
                    isVerified = true,
                    averageRating = 4.8f,
                    reviewCount = 12
                ),
                UserProfile(
                    id = "2",
                    name = "Jane Smith",
                    profession = "Plumber",
                    skillsRaw = "Piping, Leakage, Drainage",
                    mobileNumber = "0987654321",
                    emailAddress = "jane@example.com",
                    fullAddress = "456 Oak Ave, Town",
                    latitude = 0.0,
                    longitude = 0.0,
                    yearsOfExperience = 8,
                    languagesRaw = "English",
                    aboutSection = "Professional plumber",
                    isVerified = false,
                    averageRating = 4.5f,
                    reviewCount = 25
                )
            ),
            searchQuery = "",
            selectedAvail = "Available",
            selectedRating = 0f,
            selectedExp = 0,
            selectedDistance = 50f,
            onSearchQueryChange = {},
            onFilterAvailChange = {},
            onFilterRatingChange = {},
            onFilterExpChange = {},
            onFilterDistanceChange = {},
            onProfileClick = {},
            onChatClick = {},
            onBookmarkClick = {},
            categories = listOf("All", "Electrician", "Plumber", "Cleaner")
        )
    }
}

