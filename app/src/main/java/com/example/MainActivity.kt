package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.localization.JobaayaLocalization
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MapScreen
import com.example.ui.screens.ProfileDetailScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.UtilitiesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.JobaayaViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val rootViewModel: JobaayaViewModel = viewModel()
        
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
    val inboxList by viewModel.chatInboxList.collectAsState()
    val notificationsList by viewModel.notifications.collectAsState()

    var activeViewRoute by remember { mutableStateOf("home") } // "home", "map", "chats", "utilities", "admin", "settings", "detail"
    var detailedUserIdRoute by remember { mutableStateOf("") }

    var showNotificationDrawer by remember { mutableStateOf(false) }

    val totalInboxUnreadCount = remember(inboxList) {
        inboxList.sumOf { it.unreadCount }
    }

    Scaffold(
        bottomBar = {
            // Standard Material 3 bottom navigation pill bar respecting gesture pill safety spacing inset
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 8.dp
            ) {
                // Explore Tab
                NavigationBarItem(
                    selected = activeViewRoute == "home" || (activeViewRoute == "detail" && detailedUserIdRoute.startsWith("prof_")),
                    onClick = { activeViewRoute = "home" },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Browse Network") },
                    label = { Text("Explore", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // Near Me Map Tab
                NavigationBarItem(
                    selected = activeViewRoute == "map",
                    onClick = { activeViewRoute = "map" },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Near Me") },
                    label = { Text(JobaayaLocalization.translate("near_me", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // Chats inbox Tab
                NavigationBarItem(
                    selected = activeViewRoute == "chats",
                    onClick = { activeViewRoute = "chats" },
                    icon = {
                        Box {
                            Icon(Icons.Default.Chat, contentDescription = "Chats")
                            if (totalInboxUnreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.Red, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$totalInboxUnreadCount",
                                        color = Color.White,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    },
                    label = { Text(JobaayaLocalization.translate("chats", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // Utilities Tab
                NavigationBarItem(
                    selected = activeViewRoute == "utilities",
                    onClick = { activeViewRoute = "utilities" },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Utilities Tools") },
                    label = { Text("Tools", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // Admin Dashboard console Tab
                NavigationBarItem(
                    selected = activeViewRoute == "admin",
                    onClick = { activeViewRoute = "admin" },
                    icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "Admin Console") },
                    label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )

                // Settings Tab
                NavigationBarItem(
                    selected = activeViewRoute == "settings",
                    onClick = { activeViewRoute = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configurations") },
                    label = { Text(JobaayaLocalization.translate("settings", currentLang), fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }
        },
        topBar = {
            // Unified top bar showing brand and alerts drawer toggler
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 40.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("J", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "JOBAAYA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Alerts icon with dynamic notifications count indicator
                IconButton(onClick = { showNotificationDrawer = !showNotificationDrawer }) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alerts",
                            tint = if (showNotificationDrawer) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        if (notificationsList.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color.Red, CircleShape)
                            )
                        }
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
            // Central route layout coordinator
            when (activeViewRoute) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onProfileClick = { id ->
                        detailedUserIdRoute = id
                        activeViewRoute = "detail"
                    },
                    onStartChat = { id ->
                        viewModel.selectActiveChat(id)
                        activeViewRoute = "chats"
                    }
                )

                "map" -> MapScreen(
                    viewModel = viewModel,
                    onProfileClick = { id ->
                        detailedUserIdRoute = id
                        activeViewRoute = "detail"
                    }
                )

                "chats" -> ChatScreen(
                    viewModel = viewModel
                )

                "utilities" -> UtilitiesScreen(
                    viewModel = viewModel,
                    onProfileClick = { id ->
                        detailedUserIdRoute = id
                        activeViewRoute = "detail"
                    }
                )

                "admin" -> AdminScreen(
                    viewModel = viewModel
                )

                "settings" -> SettingsScreen(
                    viewModel = viewModel
                )

                "detail" -> ProfileDetailScreen(
                    viewModel = viewModel,
                    profileId = detailedUserIdRoute,
                    onBack = { activeViewRoute = "home" },
                    onStartChat = { id ->
                        viewModel.selectActiveChat(id)
                        activeViewRoute = "chats"
                    }
                )
            }

            // Slide out Alerts / Notifications dynamic overlay drawer
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
                                text = "Platform Activity Logs",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { showNotificationDrawer = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Alerts")
                            }
                        }

                        HorizontalDivider()

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .height(220.dp)
                                .padding(vertical = 10.dp)
                        ) {
                            items(notificationsList) { alert ->
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text(text = alert.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = alert.text, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
