package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.localization.JobaayaLocalization
import com.example.viewmodel.JobaayaViewModel
import com.example.data.model.UtilityNote

data class CurrencyRowItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val denominationStr: String,
    val countStr: String
)

@Composable
fun UtilitiesScreen(
    viewModel: JobaayaViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    var selectedTab by remember { mutableIntStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {},
            divider = {},
            modifier = Modifier.height(80.dp)
        ) {
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = JobaayaLocalization.translate("calculator", currentLang),
                        modifier = Modifier.padding(top = 32.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if(selectedTab == 1) Color.White else Color(0xFFCCCCCC),
                        maxLines = 1,
                        softWrap = false,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Visible
                    )
                }
            )
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = JobaayaLocalization.translate("notes", currentLang),
                        modifier = Modifier.padding(top = 32.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if(selectedTab == 0) Color.White else Color(0xFFCCCCCC),
                        maxLines = 2,
                        softWrap = true,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Visible
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = JobaayaLocalization.translate("currency_counter", currentLang),
                        modifier = Modifier.padding(top = 32.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if(selectedTab == 2) Color.White else Color(0xFFCCCCCC),
                        maxLines = 2,
                        softWrap = true,
                        textAlign = TextAlign.Center,
                        overflow = TextOverflow.Visible
                    )
                }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                1 -> CalculatorTabSection()
                0 -> NotesTabSection(viewModel = viewModel, currentLang = currentLang)
                2 -> CurrencyTabSection(currentLang = currentLang)
            }
        }
    }
}

fun getNoteTextStyle(styleLabel: String, fontColor: Long, textAlign: String = "Left", isBold: Boolean = false, isItalic: Boolean = false): TextStyle {
    val baseColor = Color(fontColor)
    val alignment = when(textAlign) {
        "Center" -> TextAlign.Center
        "Right" -> TextAlign.Right
        else -> TextAlign.Left
    }

    val baseStyle = when(styleLabel) {
        "Serif" -> TextStyle(fontFamily = FontFamily.Serif)
        "Cursive" -> TextStyle(fontFamily = FontFamily.Cursive)
        "Monospace" -> TextStyle(fontFamily = FontFamily.Monospace)
        "Sans-Serif" -> TextStyle(fontFamily = FontFamily.SansSerif)
        "Light" -> TextStyle(fontWeight = FontWeight.Light)
        "Condensed" -> TextStyle(fontFamily = FontFamily.Default)
        "Medium" -> TextStyle(fontWeight = FontWeight.Medium)
        "Bold Style" -> TextStyle(fontWeight = FontWeight.Bold)
        else -> TextStyle(fontFamily = FontFamily.Default)
    }

    return baseStyle.copy(
        color = baseColor,
        textAlign = alignment,
        fontWeight = if (isBold) FontWeight.Bold else (baseStyle.fontWeight ?: FontWeight.Normal),
        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
    )
}

val noteBgColors = listOf(
    0xFFFFFFFF, 0xFF000000, 0xFFFFF9C4, 0xFFB3E5FC, 0xFFC8E6C9,
    0xFFFFCCBC, 0xFFF8BBD0, 0xFFE1BEE7, 0xFFFFE0B2, 0xFFF5F5F5,
    0xFF1B5E20, 0xFF0D47A1, 0xFFB71C1C, 0xFF355E3B, 0xFF212121
)

val noteFontColors = listOf(
    0xFF000000, 0xFFFFFFFF, 0xFFD32F2F, 0xFF1976D2, 0xFF388E3C,
    0xFF424242, 0xFF1B4D3E, 0xFFF57C00, 0xFF5D4037, 0xFF1A237E,
    0xFF003300, 0xFF000033, 0xFF330000, 0xFF1B5E20, 0xFF263238
)

val noteFontStyles = listOf(
    "Normal", "Serif", "Cursive", "Monospace", "Bold Style",
    "Sans-Serif", "Light", "Condensed", "Medium"
)

@Composable
fun NotesTabSection(
    viewModel: JobaayaViewModel,
    currentLang: com.example.ui.localization.AppLanguage
) {
    val allNotes by viewModel.allNotes.collectAsState()
    var noteSearchQuery by remember { mutableStateOf("") }

    val filteredNotes = remember(allNotes, noteSearchQuery) {
        if (noteSearchQuery.isBlank()) allNotes
        else allNotes.filter { it.title.contains(noteSearchQuery, ignoreCase = true) || it.content.contains(noteSearchQuery, ignoreCase = true) }
    }

    var showNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<UtilityNote?>(null) }

    var noteTitle by remember { mutableStateOf("") }
    var noteContentValue by remember { mutableStateOf(TextFieldValue("")) }
    var noteBgColor by remember { mutableStateOf(0xFFFFFFFF) }
    var noteFontStyle by remember { mutableStateOf("Normal") }
    var noteFontColor by remember { mutableStateOf(0xFF000000) }
    var noteTextAlign by remember { mutableStateOf("Left") }
    var noteIsBold by remember { mutableStateOf(false) }
    var noteIsItalic by remember { mutableStateOf(false) }

    var noteIsLocked by remember { mutableStateOf(false) }
    var noteLockPin by remember { mutableStateOf("") }
    var noteReminderTimestamp by remember { mutableStateOf<Long?>(null) }

    var selectedNoteForView by remember { mutableStateOf<UtilityNote?>(null) }
    var noteToUnlock by remember { mutableStateOf<UtilityNote?>(null) }
    var unlockPinInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = noteSearchQuery,
            onValueChange = { noteSearchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Search your notes...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Column(
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
        ) {
            if (filteredNotes.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.AutoMirrored.Filled.NoteAdd, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.outline)
                    Text(if(noteSearchQuery.isBlank()) "No notes yet." else "No matching notes found.", color = MaterialTheme.colorScheme.outline)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(filteredNotes) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (note.isLocked) {
                                    noteToUnlock = note
                                    unlockPinInput = ""
                                } else {
                                    editingNote = note
                                    noteTitle = note.title
                                    noteContentValue = TextFieldValue(note.content, selection = TextRange(note.content.length))
                                    noteBgColor = note.backgroundColor
                                    noteFontStyle = note.fontStyle
                                    noteFontColor = note.fontColor
                                    noteTextAlign = note.textAlign
                                    noteIsBold = note.isBold
                                    noteIsItalic = note.isItalic
                                    noteIsLocked = note.isLocked
                                    noteLockPin = note.lockPin ?: ""
                                    noteReminderTimestamp = note.reminderTimestamp
                                    showNoteDialog = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(note.backgroundColor))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (note.isLocked) Icons.Default.Lock else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if(note.backgroundColor == 0xFF000000L || note.backgroundColor == 0xFF212121L) Color.White.copy(alpha=0.6f) else Color(note.fontColor).copy(alpha = 0.6f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (note.isLocked) "Locked Note" else note.title.ifBlank { "Untitled Note" },
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                                        color = if (note.backgroundColor == 0xFF000000L || note.backgroundColor == 0xFF212121L) Color.White else Color.Black
                                    )
                                    if (!note.isLocked) {
                                        Text(
                                            text = note.content,
                                            style = getNoteTextStyle(note.fontStyle, note.fontColor, note.textAlign, note.isBold, note.isItalic),
                                            maxLines = 1, overflow = TextOverflow.Ellipsis
                                        )
                                    } else {
                                        Text("Tap to unlock", style = MaterialTheme.typography.bodySmall, color = if (note.backgroundColor == 0xFF000000L || note.backgroundColor == 0xFF212121L) Color.White.copy(alpha=0.6f) else Color.Black.copy(alpha=0.6f))
                                    }
                                }
                                if (note.reminderTimestamp != null) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Reminder set", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { viewModel.deleteUtilityNote(note) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    editingNote = null
                    noteTitle = ""
                    noteContentValue = TextFieldValue("")
                    noteBgColor = 0xFFFFFFFF
                    noteFontStyle = "Normal"
                    noteFontColor = 0xFF000000
                    noteTextAlign = "Left"
                    noteIsBold = false
                    noteIsItalic = false
                    noteIsLocked = false
                    noteLockPin = ""
                    noteReminderTimestamp = null
                    showNoteDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00281F),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    JobaayaLocalization.translate("create_note", currentLang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    if (showNoteDialog) {
        Dialog(
            onDismissRequest = { showNoteDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.75f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(noteBgColor))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showNoteDialog = false }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black,
                                modifier = Modifier.padding(8.dp)
                            )
                            Text(
                                "Back",
                                fontWeight = FontWeight.Bold,
                                color = if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (noteTitle.isNotBlank() || noteContentValue.text.isNotBlank()) {
                                    viewModel.saveUtilityNote(
                                        noteTitle,
                                        noteContentValue.text,
                                        editingNote?.id ?: 0,
                                        noteBgColor,
                                        noteFontStyle,
                                        noteFontColor,
                                        noteTextAlign,
                                        noteIsBold,
                                        noteIsItalic,
                                        noteIsLocked,
                                        if(noteIsLocked) noteLockPin else null,
                                        noteReminderTimestamp
                                    )
                                    showNoteDialog = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black,
                                contentColor = if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.Black else Color.White
                            )
                        ) { Text("Save Note") }
                    }

                    TextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        placeholder = { Text("Title", color = (if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black).copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black,
                            unfocusedTextColor = if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black
                        ),
                        textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    )

                    TextField(
                        value = noteContentValue,
                        onValueChange = { newVal ->
                            var processedVal = newVal

                            if (newVal.text.length > noteContentValue.text.length && newVal.text.endsWith("\n")) {
                                val lines = noteContentValue.text.split("\n")
                                val lastLine = lines.lastOrNull() ?: ""
                                val prefix = when {
                                    lastLine.trimStart().startsWith("• ") -> "• "
                                    lastLine.trimStart().startsWith("☐ ") -> "☐ "
                                    lastLine.trimStart().startsWith("☑ ") -> "☐ "
                                    lastLine.trimStart().firstOrNull()?.isDigit() == true && lastLine.contains(". ") -> {
                                        val num = lastLine.trimStart().takeWhile { it.isDigit() }.toIntOrNull()
                                        if (num != null) "${num + 1}. " else ""
                                    }
                                    else -> ""
                                }
                                val newText = newVal.text + prefix
                                processedVal = TextFieldValue(newText, selection = TextRange(newText.length))
                            }

                            val cursor = processedVal.selection.start
                            if (cursor > 0 && cursor <= processedVal.text.length) {
                                val textBefore = processedVal.text.substring(0, cursor)
                                val lastChar = textBefore.last()
                                if (lastChar == '☐' || lastChar == '☑') {
                                    val newChar = if (lastChar == '☐') '☑' else '☐'
                                    val newText = processedVal.text.substring(0, cursor - 1) + newChar + processedVal.text.substring(cursor)
                                    processedVal = TextFieldValue(newText, selection = TextRange(cursor))
                                }
                            }

                            noteContentValue = processedVal
                        },
                        placeholder = { Text("Start typing...", color = Color(noteFontColor).copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color(noteFontColor), unfocusedTextColor = Color(noteFontColor)
                        ),
                        textStyle = getNoteTextStyle(noteFontStyle, noteFontColor, noteTextAlign, noteIsBold, noteIsItalic).copy(fontSize = 18.sp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = if(noteBgColor == 0xFF000000L) Color.White.copy(alpha=0.1f) else Color.Black.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                IconButton(onClick = {
                                    val newText = noteContentValue.text + "\n• "
                                    noteContentValue = TextFieldValue(newText, selection = TextRange(newText.length))
                                }) { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, "Dots", tint = Color(noteFontColor)) }
                                IconButton(onClick = {
                                    val lines = noteContentValue.text.split("\n")
                                    val newText = noteContentValue.text + "\n${lines.size}. "
                                    noteContentValue = TextFieldValue(newText, selection = TextRange(newText.length))
                                }) { Icon(Icons.Default.FormatListNumbered, "Numbers", tint = Color(noteFontColor)) }
                                IconButton(onClick = {
                                    val newText = noteContentValue.text + "\n☐ "
                                    noteContentValue = TextFieldValue(newText, selection = TextRange(newText.length))
                                }) { Icon(Icons.Default.CheckCircle, "Tickbox", tint = Color(noteFontColor)) }

                                VerticalDivider(modifier = Modifier.height(24.dp).align(Alignment.CenterVertically))

                                IconButton(onClick = { noteIsBold = !noteIsBold }) { Icon(Icons.Default.FormatBold, "Bold", tint = if(noteIsBold) Color.Blue else Color(noteFontColor)) }
                                IconButton(onClick = { noteIsItalic = !noteIsItalic }) { Icon(Icons.Default.FormatItalic, "Italic", tint = if(noteIsItalic) Color.Blue else Color(noteFontColor)) }

                                VerticalDivider(modifier = Modifier.height(24.dp).align(Alignment.CenterVertically))

                                IconButton(onClick = {
                                    if (noteIsLocked) {
                                        noteIsLocked = false
                                        noteLockPin = ""
                                    } else {
                                        noteIsLocked = true
                                    }
                                }) {
                                    Icon(
                                        if(noteIsLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                        "Lock",
                                        tint = if(noteIsLocked) Color.Red else Color(noteFontColor)
                                    )
                                }

                                IconButton(onClick = {
                                    if (noteReminderTimestamp == null) {
                                        noteReminderTimestamp = System.currentTimeMillis() + 60000
                                    } else {
                                        noteReminderTimestamp = null
                                    }
                                }) {
                                    Icon(
                                        Icons.Default.Notifications,
                                        "Reminder",
                                        tint = if(noteReminderTimestamp != null) Color.Blue else Color(noteFontColor)
                                    )
                                }
                            }

                            if (noteIsLocked) {
                                OutlinedTextField(
                                    value = noteLockPin,
                                    onValueChange = { if(it.length <= 4 && it.all { c -> c.isDigit() }) noteLockPin = it },
                                    label = { Text("Set 4-Digit PIN") },
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(noteFontColor),
                                        unfocusedTextColor = Color(noteFontColor)
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            if (noteReminderTimestamp != null) {
                                Text(
                                    "Reminder set for a few moments from now",
                                    fontSize = 10.sp,
                                    color = Color(noteFontColor).copy(alpha=0.7f),
                                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                IconButton(onClick = { noteTextAlign = "Left" }) { Icon(Icons.AutoMirrored.Filled.FormatAlignLeft, "Left", tint = if(noteTextAlign=="Left") Color.Blue else Color(noteFontColor)) }
                                IconButton(onClick = { noteTextAlign = "Center" }) { Icon(Icons.Default.FormatAlignCenter, "Center", tint = if(noteTextAlign=="Center") Color.Blue else Color(noteFontColor)) }
                                IconButton(onClick = { noteTextAlign = "Right" }) { Icon(Icons.AutoMirrored.Filled.FormatAlignRight, "Right", tint = if(noteTextAlign=="Right") Color.Blue else Color(noteFontColor)) }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(noteFontColor).copy(alpha=0.1f))

                            LazyRow(modifier = Modifier.fillMaxWidth()) {
                                items(noteFontStyles) { style ->
                                    Box(modifier = Modifier.padding(horizontal = 4.dp).clip(RoundedCornerShape(8.dp))
                                        .background(if(noteFontStyle == style) Color.Blue.copy(alpha=0.1f) else Color.Transparent)
                                        .clickable { noteFontStyle = style }.padding(6.dp)) {
                                        Text(style, fontSize = 11.sp, color = Color(noteFontColor), fontWeight = if(noteFontStyle == style) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                items(noteFontColors) { colorLong ->
                                    Box(modifier = Modifier.size(28.dp).padding(4.dp).clip(CircleShape).background(Color(colorLong))
                                        .border(if(noteFontColor == colorLong) 2.dp else 0.dp, if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black, CircleShape)
                                        .clickable { noteFontColor = colorLong })
                                }
                            }

                            LazyRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                items(noteBgColors) { colorLong ->
                                    Box(modifier = Modifier.size(28.dp).padding(4.dp).clip(CircleShape).background(Color(colorLong))
                                        .border(if(noteBgColor == colorLong) 2.dp else 0.dp, if(noteBgColor == 0xFF000000L || noteBgColor == 0xFF212121L) Color.White else Color.Black, CircleShape)
                                        .clickable { noteBgColor = colorLong })
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (noteToUnlock != null) {
        Dialog(onDismissRequest = { noteToUnlock = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Enter PIN to Unlock Note", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = unlockPinInput,
                        onValueChange = { input -> if(input.length <= 4) unlockPinInput = input },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.width(150.dp),
                        singleLine = true,
                        textStyle = TextStyle(textAlign = TextAlign.Center)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { noteToUnlock = null }) { Text("Cancel") }
                        Button(onClick = {
                            if (unlockPinInput == noteToUnlock?.lockPin) {
                                val note = noteToUnlock!!
                                editingNote = note
                                noteTitle = note.title
                                noteContentValue = TextFieldValue(note.content, selection = TextRange(note.content.length))
                                noteBgColor = note.backgroundColor
                                noteFontStyle = note.fontStyle
                                noteFontColor = note.fontColor
                                noteTextAlign = note.textAlign
                                noteIsBold = note.isBold
                                noteIsItalic = note.isItalic
                                noteIsLocked = note.isLocked
                                noteLockPin = note.lockPin ?: ""
                                noteReminderTimestamp = note.reminderTimestamp
                                noteToUnlock = null
                                showNoteDialog = true
                            } else {
                                unlockPinInput = ""
                            }
                        }) { Text("Unlock") }
                    }
                }
            }
        }
    }

    if (selectedNoteForView != null) {
        val note = selectedNoteForView!!
        Dialog(onDismissRequest = { selectedNoteForView = null }) {
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(note.backgroundColor))) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(note.title.ifBlank { "Note" }, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if(note.backgroundColor == 0xFF000000L) Color.White else Color.Black, textAlign = when(note.textAlign){"Center"->TextAlign.Center;"Right"->TextAlign.Right;else->TextAlign.Left}, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(note.content, style = getNoteTextStyle(note.fontStyle, note.fontColor, note.textAlign, note.isBold, note.isItalic).copy(fontSize = 18.sp), modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { selectedNoteForView = null }, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = if(note.backgroundColor == 0xFF000000L) Color.White else Color.Black)) { Text("Close", color = if(note.backgroundColor == 0xFF000000L) Color.Black else Color.White) }
                }
            }
        }
    }
}

@Composable
fun CalculatorTabSection() {
    var displayStr by remember { mutableStateOf("0") }
    var runningVal by remember { mutableStateOf(0.0) }
    var activeOp by remember { mutableStateOf("") }
    var isStartingNewVal by remember { mutableStateOf(true) }
    var historyList by remember { mutableStateOf(listOf<String>()) }
    var showHistoryDialog by remember { mutableStateOf(false) }

    val padKeys = listOf(
        listOf("C", "H", "%", "Back"),
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "x"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "=", "+")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.fillMaxHeight(0.12f))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            color = Color.Black,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.dp, Color(0xFF1B5E20))
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (activeOp.isNotBlank()) {
                            val runningStr = if (runningVal % 1 == 0.0) runningVal.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", runningVal)
                            "$runningStr ${if (activeOp == "*") "x" else (if(activeOp == "/") "÷" else activeOp)}"
                        } else "",
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (activeOp.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (activeOp == "*") "x" else (if(activeOp == "/") "÷" else activeOp),
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = displayStr,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 27.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

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
                        val isAction = key in listOf("C", "H", "=", "Back", "/", "x", "-", "+", "%")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(55.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    when {
                                        key == "C" -> Color(0xFFEF5350)
                                        key == "H" -> Color(0xFF455A64)
                                        isAction -> MaterialTheme.colorScheme.primary
                                        else -> Color(0xFF00281F)
                                    }
                                )
                                .clickable {
                                    when {
                                        key == "C" -> {
                                            displayStr = "0"
                                            runningVal = 0.0
                                            activeOp = ""
                                            isStartingNewVal = true
                                        }

                                        key == "H" -> {
                                            showHistoryDialog = true
                                        }

                                        key == "Back" -> {
                                            if (displayStr.length > 1) {
                                                displayStr = displayStr.dropLast(1)
                                            } else {
                                                displayStr = "0"
                                                isStartingNewVal = true
                                            }
                                        }

                                        key in listOf("+", "-", "x", "/") -> {
                                            val nextVal = displayStr.toDoubleOrNull() ?: 0.0
                                            if (activeOp.isNotBlank() && !isStartingNewVal) {
                                                val res = when (activeOp) {
                                                    "+" -> runningVal + nextVal
                                                    "-" -> runningVal - nextVal
                                                    "*" -> runningVal * nextVal
                                                    "/" -> if (nextVal != 0.0) runningVal / nextVal else 0.0
                                                    else -> nextVal
                                                }

                                                val opSym = if(activeOp == "*") "x" else (if(activeOp == "/") "÷" else activeOp)
                                                val resStr = if (res % 1 == 0.0) res.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", res)
                                                val runStr = if (runningVal % 1 == 0.0) runningVal.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", runningVal)
                                                historyList = (listOf("$runStr $opSym $nextVal = $resStr") + historyList).take(10)

                                                runningVal = res
                                                displayStr = resStr
                                            } else {
                                                runningVal = nextVal
                                            }
                                            activeOp = if (key == "x") "*" else key
                                            isStartingNewVal = true
                                        }

                                        key == "%" -> {
                                            val currentVal = displayStr.toDoubleOrNull() ?: 0.0
                                            val res = if (activeOp.isNotBlank()) {
                                                runningVal * (currentVal / 100.0)
                                            } else {
                                                currentVal / 100.0
                                            }
                                            val resStr = if (res % 1 == 0.0) res.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", res)
                                            displayStr = resStr
                                        }

                                        key == "=" -> {
                                            if (activeOp.isNotBlank()) {
                                                if (isStartingNewVal) {
                                                    // If = is pressed right after an operator, just commit current result
                                                    activeOp = ""
                                                } else {
                                                    val nextVal = displayStr.toDoubleOrNull() ?: 0.0
                                                    val opSymbol = if (activeOp == "*") "x" else (if (activeOp == "/") "÷" else activeOp)
                                                    val res = when (activeOp) {
                                                        "+" -> runningVal + nextVal
                                                        "-" -> runningVal - nextVal
                                                        "*" -> runningVal * nextVal
                                                        "/" -> if (nextVal != 0.0) runningVal / nextVal else 0.0
                                                        else -> nextVal
                                                    }
                                                    val resStr = if (res % 1 == 0.0) res.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", res)
                                                    val runStr = if (runningVal % 1 == 0.0) runningVal.toLong().toString() else String.format(java.util.Locale.getDefault(), "%.2f", runningVal)

                                                    val historyEntry = "$runStr $opSymbol $nextVal = $resStr"
                                                    historyList = (listOf(historyEntry) + historyList).take(10)

                                                    displayStr = resStr
                                                    runningVal = res
                                                    activeOp = ""
                                                    isStartingNewVal = true
                                                }
                                            }
                                        }

                                        else -> {
                                            if (isStartingNewVal) {
                                                displayStr = if (key == ".") "0." else key
                                                isStartingNewVal = false
                                            } else {
                                                if (key == "." && displayStr.contains(".")) {
                                                    // Do nothing
                                                } else {
                                                    displayStr = if (displayStr == "0") key else displayStr + key
                                                }
                                            }
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (key == "H") {
                                Icon(Icons.Default.History, contentDescription = "History", tint = Color.White, modifier = Modifier.size(18.dp))
                            } else {
                                Text(
                                    text = key,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontSize = if(isAction) 17.sp else 20.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showHistoryDialog) {
        Dialog(onDismissRequest = { showHistoryDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF00281F)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Calculation History",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (historyList.isEmpty()) {
                        Text("No history yet.", color = Color.White.copy(alpha = 0.6f))
                    } else {
                        LazyColumn(modifier = Modifier.height(300.dp)) {
                            items(historyList) { entry ->
                                val parts = entry.split(" = ")
                                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                                    if (parts.size >= 2) {
                                        Text(parts[0], color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                                        Text("= ${parts[1]}", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                    } else {
                                        Text(entry, color = Color.White)
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(top = 10.dp), color = Color.White.copy(alpha=0.15f))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showHistoryDialog = false },
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f))
                    ) {
                        Text("Close", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyTabSection(
    currentLang: com.example.ui.localization.AppLanguage
) {
    var currencyRows by remember {
        mutableStateOf(
            listOf(
                CurrencyRowItem(denominationStr = "500", countStr = ""),
                CurrencyRowItem(denominationStr = "200", countStr = ""),
                CurrencyRowItem(denominationStr = "100", countStr = ""),
                CurrencyRowItem(denominationStr = "50", countStr = ""),
                CurrencyRowItem(denominationStr = "20", countStr = ""),
                CurrencyRowItem(denominationStr = "10", countStr = ""),
                CurrencyRowItem(denominationStr = "5", countStr = ""),
                CurrencyRowItem(denominationStr = "2", countStr = "")
            )
        )
    }

    val totalAmount = currencyRows.sumOf { row ->
        val denom = row.denominationStr.toLongOrNull() ?: 0L
        val count = row.countStr.toLongOrNull() ?: 0L
        denom * count
    }

    val contentColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 42.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF00281F)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = JobaayaLocalization.translate("total_amount", currentLang),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
                val formattedTotal = java.text.NumberFormat.getIntegerInstance().format(totalAmount)
                Text(
                    text = formattedTotal,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(currencyRows, key = { it.id }) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = item.denominationStr,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() }) {
                                    currencyRows = currencyRows.map {
                                        if (it.id == item.id) it.copy(denominationStr = newVal) else it
                                    }
                                }
                            },
                            placeholder = { Text("Note", fontSize = 12.sp, color = contentColor.copy(alpha=0.5f)) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier
                                .width(70.dp)
                                .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = contentColor,
                                unfocusedTextColor = contentColor
                            ),
                            textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = contentColor, textAlign = TextAlign.Center)
                        )

                        Text(
                            text = "  x ",
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )

                        TextField(
                            value = item.countStr,
                            onValueChange = { newVal ->
                                if (newVal.all { it.isDigit() }) {
                                    currencyRows = currencyRows.map {
                                        if (it.id == item.id) it.copy(countStr = newVal) else it
                                    }
                                }
                            },
                            placeholder = { Text("0", fontSize = 14.sp, color = contentColor.copy(alpha=0.5f)) },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            modifier = Modifier
                                .width(85.dp)
                                .border(1.dp, contentColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = contentColor,
                                unfocusedTextColor = contentColor
                            ),
                            textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = contentColor, textAlign = TextAlign.Center)
                        )
                    }

                    val rowTotal = (item.denominationStr.toLongOrNull() ?: 0L) * (item.countStr.toLongOrNull() ?: 0L)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "= $rowTotal",
                            fontWeight = FontWeight.Black,
                            color = contentColor,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(end = 8.dp)
                        )

                        IconButton(
                            onClick = {
                                currencyRows = currencyRows.filter { it.id != item.id }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Row",
                                tint = Color.Red.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    currencyRows = currencyRows + CurrencyRowItem(denominationStr = "", countStr = "")
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF00281F))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Row",
                    tint = Color.White
                )
            }
        }

        Button(
            onClick = { currencyRows = currencyRows.map { it.copy(countStr = "") } },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00281F))
        ) {
            Text("Reset All Counter", color = Color.White)
        }
    }
}