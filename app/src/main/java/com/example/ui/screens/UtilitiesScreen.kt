package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel

@Composable
fun UtilitiesScreen(
    viewModel: JobaayaViewModel,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val profiles by viewModel.filteredProfiles.collectAsState()

    val bookmarkedProfiles = remember(profiles) {
        profiles.filter { it.bookmarkStatus }
    }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Notes, 1: Calculator, 2: Bookmarks/Favorites

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Headers using Material 3 TabRow
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(JobaayaLocalization.translate("notes", currentLang), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(JobaayaLocalization.translate("calculator", currentLang), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(JobaayaLocalization.translate("bookmarks", currentLang), fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Content Routing
        when (selectedTab) {
            0 -> NotesTabSection(viewModel = viewModel, currentLang = currentLang)
            1 -> CalculatorTabSection(currentLang = currentLang)
            2 -> BookmarkedProfilesTabSection(bookmarkedList = bookmarkedProfiles, onProfileClick = onProfileClick)
        }
    }
}

// 1. NOTES SECTIONS SUB-COMPONENTS
@Composable
fun NotesTabSection(
    viewModel: JobaayaViewModel,
    currentLang: com.example.ui.localization.AppLanguage
) {
    val notes by viewModel.allNotes.collectAsState()

    var showNewNoteComposer by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Note creation form header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sticky Notepad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { showNewNoteComposer = !showNewNoteComposer },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(JobaayaLocalization.translate("create_note", currentLang))
            }
        }

        AnimatedVisibility(visible = showNewNoteComposer) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Add Quick Sticky note",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Note details...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                                    viewModel.saveUtilityNote(noteTitle, noteContent)
                                    noteTitle = ""
                                    noteContent = ""
                                    showNewNoteComposer = false
                                }
                            }
                        ) {
                            Text("Save Note")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (notes.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.NoteAdd,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No notes created yet. Jot down rates, booking requests, or numbers!",
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(notes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = note.title.ifBlank { "Untitled Note" },
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.deleteUtilityNote(note) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Note",
                                    tint = Color.Red.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. CALCULATOR SECTION SUB-COMPONENTS
@Composable
fun CalculatorTabSection(
    currentLang: com.example.ui.localization.AppLanguage
) {
    var displayStr by remember { mutableStateOf("0") }
    var runningVal by remember { mutableStateOf(0.0) }
    var activeOp by remember { mutableStateOf("") }
    var isStartingNewVal by remember { mutableStateOf(true) }

    val padKeys = listOf(
        listOf("C", "Taxes", "%", "/"),
        listOf("7", "8", "9", "*"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", ".", "=", "Rate")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = JobaayaLocalization.translate("calc_title", currentLang),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = JobaayaLocalization.translate("calc_hint", currentLang),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Screen Display Calculator Layout
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = Color.Black,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (activeOp.isNotBlank()) "$runningVal $activeOp" else "Quote Mode",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = displayStr,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tactile Calc Grid keypad implementation
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            padKeys.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowKeys.forEach { key ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    when (key) {
                                        "C" -> Color(0xFFEF5350)
                                        "=", "Rate", "Taxes" -> MaterialTheme.colorScheme.primary
                                        "/", "*", "-", "+", "%" -> MaterialTheme.colorScheme.secondaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                )
                                .clickable {
                                    // Handle logic of arithmetic triggers
                                    when {
                                        key == "C" -> {
                                            displayStr = "0"
                                            runningVal = 0.0
                                            activeOp = ""
                                            isStartingNewVal = true
                                        }

                                        key in listOf("+", "-", "*", "/", "%") -> {
                                            runningVal = displayStr.toDoubleOrNull() ?: 0.0
                                            activeOp = key
                                            isStartingNewVal = true
                                        }

                                        key == "=" -> {
                                            if (activeOp.isNotBlank()) {
                                                val nextVal = displayStr.toDoubleOrNull() ?: 0.0
                                                val res = when (activeOp) {
                                                    "+" -> runningVal + nextVal
                                                    "-" -> runningVal - nextVal
                                                    "*" -> runningVal * nextVal
                                                    "/" -> if (nextVal != 0.0) runningVal / nextVal else 0.0
                                                    "%" -> runningVal * (nextVal / 100.0)
                                                    else -> nextVal
                                                }
                                                displayStr = if (res % 1 == 0.0) res
                                                    .toInt()
                                                    .toString() else String.format("%.2f", res)
                                                activeOp = ""
                                                isStartingNewVal = true
                                            }
                                        }

                                        key == "Taxes" -> {
                                            // Quick add GST / Service Tax rate metric (18%)
                                            val currentVal = displayStr.toDoubleOrNull() ?: 0.0
                                            val nextVal = currentVal * 1.18
                                            displayStr = String.format("%.2f", nextVal)
                                            isStartingNewVal = true
                                        }

                                        key == "Rate" -> {
                                            // Apply premium builder diagnostic calculation
                                            val currentVal = displayStr.toDoubleOrNull() ?: 0.0
                                            val nextVal = currentVal * 1.35 // typical service margin markup added
                                            displayStr = String.format("%.2f", nextVal)
                                            isStartingNewVal = true
                                        }

                                        else -> {
                                            if (isStartingNewVal) {
                                                displayStr = if (key == ".") "0." else key
                                                isStartingNewVal = false
                                            } else {
                                                if (key == "." && displayStr.contains(".")) {
                                                    // ignore duplicate decimal
                                                } else {
                                                    displayStr = if (displayStr == "0") key else displayStr + key
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = key,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (key in listOf("C", "=", "Rate", "Taxes")) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// 3. BOOKMARKS SECTION SUB-COMPONENTS
@Composable
fun BookmarkedProfilesTabSection(
    bookmarkedList: List<UserProfile>,
    onProfileClick: (String) -> Unit
) {
    if (bookmarkedList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                modifier = Modifier.size(54.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Favorite Listings Bookmarks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Bookmarked professional profiles will appear here for immediate access offline.",
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(bookmarkedList) { profile ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onProfileClick(profile.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.profession,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFB300))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", profile.averageRating),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
