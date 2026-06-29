package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.data.ProfessionsData

@Composable
fun ProfessionPicker(
    currentProfession: String,
    onProfessionChange: (String) -> Unit,
    currentSkills: String,
    onSkillsChange: (String) -> Unit,
    label: String = "Profession"
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf(currentProfession) }
    
    val filteredProfessions = remember(searchQuery) {
        if (searchQuery.isBlank()) {
            ProfessionsData.professions
        } else {
            val filtered = ProfessionsData.professions.filter {
                it.profession.contains(searchQuery, ignoreCase = true)
            }
            // Always show the full list if the query exactly matches one item (to allow switching)
            if (filtered.size == 1 && filtered[0].profession.equals(searchQuery, ignoreCase = true)) {
                ProfessionsData.professions
            } else {
                filtered
            }
        }
    }

    val suggestedKeywords = remember(currentProfession) {
        ProfessionsData.getSuggestionsForProfession(currentProfession)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                onProfessionChange(it)
                expanded = true
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        AnimatedVisibility(visible = expanded && filteredProfessions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .heightIn(max = 250.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredProfessions) { suggestion ->
                        Text(
                            text = suggestion.profession,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = suggestion.profession
                                    onProfessionChange(suggestion.profession)
                                    expanded = false
                                }
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        if (suggestedKeywords.isNotEmpty()) {
            Text(
                text = "Suggested Skills (क्लिक करें जोड़ने के लिए):",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(suggestedKeywords) { keyword ->
                    val isAlreadyAdded = currentSkills.split(",").map { it.trim() }.contains(keyword)
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isAlreadyAdded) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable {
                                if (!isAlreadyAdded) {
                                    val newSkills = if (currentSkills.isBlank()) keyword
                                    else "$currentSkills, $keyword"
                                    onSkillsChange(newSkills)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "+ $keyword",
                            fontSize = 11.sp,
                            fontWeight = if (isAlreadyAdded) FontWeight.Bold else FontWeight.Normal,
                            color = if (isAlreadyAdded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
