package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.ContactUsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.ProfileDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UtilitiesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JobaayaViewModel
import com.example.ui.localization.JobaayaLocalization
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        enableEdgeToEdge()
        setContent {
            val rootViewModel: JobaayaViewModel = viewModel()
            MyApplicationTheme(darkTheme = true) {
                val isLoggedIn by rootViewModel.isLoggedIn.collectAsState()

                if (!isLoggedIn) {
                    AuthScreen(viewModel = rootViewModel)
                } else {
                    MainPlatformContainer(viewModel = rootViewModel)
                }
            }
        }
    }
}

@Composable
fun MainPlatformContainer(
    viewModel: JobaayaViewModel
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val inboxList by viewModel.chatInboxList.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()

    var activeViewRoute by remember { mutableStateOf("home") } // "home", "map", "chats", "utilities", "admin", "settings", "detail", "contact_us"
    var previousViewRoute by remember { mutableStateOf("home") }
    var detailedUserIdRoute by remember { mutableStateOf("") }
    var showNotificationDrawer by remember { mutableStateOf(false) }

    // Helper function to navigate and track history
    val navigateTo: (String) -> Unit = { route ->
        if (activeViewRoute != route) {
            previousViewRoute = activeViewRoute
            activeViewRoute = route
        }
    }

    // Back button handling logic
    BackHandler(enabled = activeViewRoute != "home" || showNotificationDrawer) {
        if (showNotificationDrawer) {
            showNotificationDrawer = false
        } else if (activeViewRoute == "detail") {
            activeViewRoute = previousViewRoute
            if (previousViewRoute == "detail") activeViewRoute = "home"
        } else {
            activeViewRoute = "home"
        }
    }

    val totalInboxUnreadCount = remember(inboxList) {
        inboxList.sumOf { it.unreadCount }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .height(58.dp),
                tonalElevation = 0.dp,
                containerColor = Color(0xFF0B3A51),
                contentColor = Color.White
            ) {
                // Home Tab
                NavigationBarItem(
                    selected = activeViewRoute == "home" || (activeViewRoute == "detail" && detailedUserIdRoute.startsWith("prof_")),
                    onClick = { navigateTo("home") },
                    icon = { Icon(Icons.Default.Home, contentDescription = JobaayaLocalization.translate("home", currentLang), modifier = Modifier.size(21.dp)) },
                    label = { Text(JobaayaLocalization.translate("home", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )

                // Chats inbox Tab
                NavigationBarItem(
                    selected = activeViewRoute == "chats" ,
                    onClick = { navigateTo("chats") },
                    icon = {
                        Box {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = JobaayaLocalization.translate("chats", currentLang), modifier = Modifier.size(21.dp))
                            if (totalInboxUnreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$totalInboxUnreadCount",
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    label = { Text(JobaayaLocalization.translate("chats", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )

                // Utilities Tab
                NavigationBarItem(
                    selected = activeViewRoute == "utilities",
                    onClick = { navigateTo("utilities") },
                    icon = { Icon(Icons.Default.Build, contentDescription = JobaayaLocalization.translate("tools", currentLang), modifier = Modifier.size(21.dp)) },
                    label = { Text(JobaayaLocalization.translate("tools", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
            }
        },
        topBar = {
            // Unified top bar with Deep Teal background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B3A51))
                    .padding(top = 27.dp, bottom = 5.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // --- नया बदलाव: टॉप लेफ्ट कॉर्नर पर प्रोफाइल सर्कल फोटो, जो क्लिक करने पर सीधे प्रिव्यू (Detail Screen) दिखाएगी ---
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable {
                            myProfile?.let {
                                detailedUserIdRoute = it.id
                                navigateTo("detail")
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (myProfile?.profilePhotoUrl?.isNotEmpty() == true) {
                        AsyncImage(
                            model = ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                                .data(java.io.File(myProfile!!.profilePhotoUrl))
                                .crossfade(true)
                                .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                                .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                                .build(),
                            contentDescription = "Profile Preview Logo",
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                // CENTER: App Brand Logo
                Box(
                    modifier = Modifier
                        .width(91.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.kuku),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(width = 82.dp, height = 29.dp)
                    )
                }

                // Right side: Icons
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAdmin = myProfile?.mobileNumber == "+919630981234" || myProfile?.emailAddress == "mubarakk912@gmail.com"

                    if (isAdmin) {
                        IconButton(onClick = { navigateTo("admin") }, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.SupervisorAccount,
                                contentDescription = "Admin Console",
                                tint = if (activeViewRoute == "admin") MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(onClick = {
                        showNotificationDrawer = !showNotificationDrawer
                        if (showNotificationDrawer) {
                            viewModel.markNotificationsAsRead()
                        }
                    }, modifier = Modifier.size(36.dp)) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alerts",
                                tint = if (showNotificationDrawer) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            val unreadCount = notificationsList.count { !it.isRead }
                            if (unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unreadCount",
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = { navigateTo("settings") }, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = if (activeViewRoute == "settings") MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeViewRoute) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onProfileClick = { id: String ->
                        detailedUserIdRoute = id
                        navigateTo("detail")
                    },
                    onStartChat = { id ->
                        viewModel.selectActiveChat(id)
                        navigateTo("chats")
                    },
                    onNearMeClick = { navigateTo("map") }
                )

                "map" -> MapScreen(
                    viewModel = viewModel,
                    onProfileClick = { id: String ->
                        detailedUserIdRoute = id
                        navigateTo("detail")
                    }
                )

                "chats" -> ChatScreen(
                    viewModel = viewModel,
                    onNavigateToProfile = { id ->
                        detailedUserIdRoute = id
                        navigateTo("detail")
                    }
                )

                "utilities" -> UtilitiesScreen(
                    viewModel = viewModel
                )

                "admin" -> {
                    val isAdmin = myProfile?.mobileNumber == "+919630981234" || myProfile?.emailAddress == "mubarakk912@gmail.com"
                    if (isAdmin) {
                        AdminScreen(
                            viewModel = viewModel,
                            onProfileClick = { id: String ->
                                detailedUserIdRoute = id
                                navigateTo("detail")
                            }
                        )
                    } else {
                        HomeScreen(
                            viewModel = viewModel,
                            onProfileClick = { id: String ->
                                detailedUserIdRoute = id
                                navigateTo("detail")
                            },
                            onStartChat = { id ->
                                viewModel.selectActiveChat(id)
                                navigateTo("chats")
                            }
                        )
                    }
                }

                "settings" -> SettingsScreen(
                    viewModel = viewModel,
                    onPreviewClick = { id ->
                        detailedUserIdRoute = id
                        navigateTo("detail")
                    },
                    onContactUsClick = { navigateTo("contact_us") }
                )

                "contact_us" -> ContactUsScreen(
                    onBack = { navigateTo("settings") }
                )

                "detail" -> ProfileDetailScreen(
                    viewModel = viewModel,
                    profileId = detailedUserIdRoute,
                    onBack = { navigateTo("home") },
                    onStartChat = { id ->
                        viewModel.selectActiveChat(id)
                        navigateTo("chats")
                    }
                )
            }

            // Notification drawer block
            AnimatedVisibility(
                visible = showNotificationDrawer,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 50.dp),
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = JobaayaLocalization.translate("activity_logs", currentLang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                if (notificationsList.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.clearAllNotifications() }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clear All", tint = Color.Red, modifier = Modifier.size(20.dp))
                                    }
                                }
                                IconButton(onClick = { showNotificationDrawer = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Alerts")
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (notificationsList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Notifications, null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
                                    Text(JobaayaLocalization.translate("no_activity_logs", currentLang), color = Color.Gray, fontSize = 12.sp)
                                }
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .heightIn(max = 350.dp)
                                    .padding(vertical = 4.dp)
                            ) {
                                items(notificationsList) { alert ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(if (alert.isRead) Color.Transparent else MaterialTheme.colorScheme.primary)
                                                )
                                                Spacer(Modifier.width(8.dp))
                                                Text(
                                                    text = alert.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            val timeString = try {
                                                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                                sdf.format(java.util.Date(alert.timestamp))
                                            } catch (_: Exception) { "" }

                                            Text(
                                                text = timeString,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        Text(
                                            text = alert.content,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
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