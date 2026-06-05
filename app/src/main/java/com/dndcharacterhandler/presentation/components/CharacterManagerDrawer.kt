package com.dndcharacterhandler.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dndcharacterhandler.domain.model.AppLanguage
import com.dndcharacterhandler.presentation.localization.LocalStrings
import com.dndcharacterhandler.presentation.localization.text

@Composable
fun CharacterManagerDrawer(
    state: CharacterManagerUiState,
    onSelectCharacter: (Long) -> Unit,
    onCreateCharacter: () -> Unit,
    onExportCharacter: () -> Unit,
    onDeleteCharacter: () -> Unit,
    onImportCharacter: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var languageExpanded by remember { mutableStateOf(false) }
    val strings = LocalStrings.current

    Surface(
        modifier = Modifier.fillMaxHeight(),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.86f)
                .statusBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text("drawer_characters"), style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.characters, key = { it.character.id }) { character ->
                        val isSelected = state.selectedCharacterId == character.character.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { onSelectCharacter(character.character.id) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            )
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(
                                    character.character.name.ifBlank { text("placeholder_loading_character") },
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    strings.format(
                                        "drawer_character_summary",
                                        character.character.race.ifBlank { text("placeholder_race") },
                                        character.character.characterClass.ifBlank { text("placeholder_class") },
                                        character.character.subclass.ifBlank { text("placeholder_subclass") }
                                    )
                                )
                                Text(strings.format("drawer_level", character.character.level))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                DrawerAction(text("drawer_new_character"), onCreateCharacter)
                DrawerAction(text("drawer_export_character"), onExportCharacter)
                DrawerAction(text("drawer_delete_character"), onDeleteCharacter)
                DrawerAction(text("drawer_import_character"), onImportCharacter)
            }

            Column {
                SectionDivider()
                Box {
                    DrawerAction(strings.format("drawer_language_value", text(state.language.localizationKey))) {
                        languageExpanded = true
                    }
                    DropdownMenu(
                        expanded = languageExpanded,
                        onDismissRequest = { languageExpanded = false }
                    ) {
                        AppLanguage.entries.forEach { language ->
                            DropdownMenuItem(
                                text = { Text(text(language.localizationKey)) },
                                onClick = {
                                    languageExpanded = false
                                    onLanguageSelected(language)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerAction(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}
