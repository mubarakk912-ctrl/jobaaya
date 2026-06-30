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
                placeholder = { Text("Search...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) },
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Near Me", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                        
                        IconButton(
                            onClick = { showFilters = !showFilters },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                    focusedContainerColor = Color.Black.copy(alpha = 0.05f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.05f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
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
                    Text(text = "Refine Network Search", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Experience (Min: $selectedExp Years)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
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
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Text(text = if (stars == 0.0f) "All" else "$stars★", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
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
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = if (active) 2.dp else 1.dp,
                        color = if (active) Color(0xFF01796F) else Color(0xFF01796F).copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = cat,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (active) Color(0xFF01796F) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
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
                            color = Color.Gray.copy(alpha = 0.5f)
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFF00120F)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Section: Avatar, Name, Profession
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF015F56)),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile.profilePhotoUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(profile.profilePhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = profile.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Khali Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF015F56))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = profile.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                        tint = if (profile.bookmarkStatus) Color(0xFFFFB300) else Color.White.copy(alpha = 0.6f),
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
                    color = Color.White
                )
                Text(
                    text = " (${profile.reviewCount})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(16.dp))

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = profile.fullAddress.split(",").last().trim(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                // Get Direction - Styled as a simple clickable line
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
                        text = "Get Direction",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00A38E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expertise & Custom Skills Button - Now Toggles details
            Box(
                modifier = Modifier
                    .background(Color(0xFF00120F), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFF015F56), RoundedCornerShape(8.dp))
                    .clickable { showDetailedSkills = !showDetailedSkills }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Expertise & Custom Skills",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (showDetailedSkills) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
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
                                    .background(Color(0xFF015F56).copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .border(1.dp, Color(0xFF015F56).copy(alpha = 0.4f), RoundedCornerShape(6.dp))
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015F56))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Chat", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF015F56))
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Call", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
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
            myProfile = UserProfile(
                name = "My Profile",
                profession = "Developer",
                skillsRaw = "Kotlin, Compose",
                mobileNumber = "1234567890",
                emailAddress = "me@example.com",
                fullAddress = "Home",
                latitude = 0.0,
                longitude = 0.0,
                yearsOfExperience = 5,
                languagesRaw = "English",
                aboutSection = "I am a developer",
                isMe = true
            ),
            profiles = listOf(
                UserProfile(
                    name = "Jane Smith",
                    profession = "Designer",
                    skillsRaw = "Figma, UI/UX",
                    mobileNumber = "0987654321",
                    emailAddress = "jane@example.com",
                    fullAddress = "Office",
                    latitude = 0.0,
                    longitude = 0.0,
                    yearsOfExperience = 3,
                    languagesRaw = "English, Spanish",
                    aboutSection = "I am a designer",
                    averageRating = 4.5f
                )
            ),
            searchQuery = "",
            selectedAvail = "All",
            selectedRating = 0.0f,
            selectedExp = 0,
            selectedDistance = 50.0f,
            onSearchQueryChange = {},
            onFilterAvailChange = {},
            onFilterRatingChange = {},
            onFilterExpChange = {},
            onFilterDistanceChange = {},
            onProfileClick = {},
            onChatClick = {},
            onBookmarkClick = {},
            categories = listOf("All", "Design", "Development")
        )
    }
}
