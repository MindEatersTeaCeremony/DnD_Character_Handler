package com.dndcharacterhandler.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.dndcharacterhandler.data.localization.LocalizedStrings
import com.dndcharacterhandler.domain.model.AppLanguage
import com.dndcharacterhandler.domain.model.AssetReferences
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.presentation.localization.LocalStrings
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.theme.DnDTheme
import com.dndcharacterhandler.presentation.theme.LocalDesignTokens

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
    val tokens = LocalDesignTokens.current.typography

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.68f)
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A161D), Color(0xFF0E0B11), Color(0xFF09070D)),
                    radius = 1300f
                )
            )
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 70.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = text("drawer_characters"),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = tokens.characterName.fontSizeSp.sp,
                        lineHeight = (tokens.characterName.lineHeightSp ?: tokens.characterName.fontSizeSp).sp
                    ),
                    color = Color(0xFFF7F2EA)
                )
                DrawerOrnamentDivider(modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
            }

            items(state.characters, key = { it.character.id }) { characterBundle ->
                DrawerCharacterCard(
                    characterBundle = characterBundle,
                    selected = state.selectedCharacterId == characterBundle.character.id,
                    onClick = { onSelectCharacter(characterBundle.character.id) }
                )
            }

            item {
                DrawerActionCard(
                    label = text("drawer_new_character"),
                    icon = Icons.Outlined.AddCircleOutline,
                    onClick = onCreateCharacter,
                    dashed = true,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                DrawerOrnamentDivider(modifier = Modifier.padding(top = 18.dp, bottom = 12.dp))
                DrawerActionCard(
                    label = text("drawer_export_character"),
                    icon = Icons.Outlined.FileUpload,
                    onClick = onExportCharacter
                )
            }

            item {
                DrawerActionCard(
                    label = text("drawer_delete_character"),
                    icon = Icons.Outlined.Delete,
                    iconTint = Color(0xFFC6A36C),
                    onClick = onDeleteCharacter
                )
            }

            item {
                DrawerOrnamentDivider(modifier = Modifier.padding(top = 18.dp, bottom = 12.dp))
                DrawerActionCard(
                    label = text("drawer_import_character"),
                    icon = Icons.Outlined.FileDownload,
                    onClick = onImportCharacter
                )
            }

            item {
                DrawerOrnamentDivider(modifier = Modifier.padding(top = 18.dp, bottom = 12.dp))
                Box {
                    DrawerLanguageCard(
                        label = text("drawer_language"),
                        value = text(state.language.localizationKey),
                        onClick = { languageExpanded = true }
                    )
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

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun DrawerCharacterCard(
    characterBundle: CharacterBundle,
    selected: Boolean,
    onClick: () -> Unit
) {
    val character = characterBundle.character
    val strings = LocalStrings.current
    val tokens = LocalDesignTokens.current.typography
    val characterName = character.name.ifBlank { text("placeholder_loading_character") }
    val classLabel = buildDrawerClassLabel(characterBundle, strings)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(124.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF17141B).copy(alpha = 0.72f),
        border = BorderStroke(1.dp, if (selected) Color(0xFFC6A36C) else Color(0x42FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DrawerSelectionDot(selected = selected)
            DrawerPortrait(
                portraitUri = character.portraitUri,
                characterName = characterName,
                modifier = Modifier.padding(start = 14.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 18.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = characterName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF7F2EA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = classLabel,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD2CAC2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = strings.format("drawer_level", character.level),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = tokens.subtitleToken.fontSizeSp.sp),
                    color = Color(0xFFC6A36C)
                )
            }
        }
    }
}

@Composable
private fun DrawerSelectionDot(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = if (selected) Color(0xFFFFD86B) else Color.Transparent,
                radius = 9.dp.toPx()
            )
            drawCircle(
                color = if (selected) Color(0xFFFFD86B) else Color(0xFFC2BBB3),
                radius = 10.dp.toPx(),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
private fun DrawerPortrait(
    portraitUri: String?,
    characterName: String,
    modifier: Modifier = Modifier
) {
    val portraitReference = portraitUri ?: AssetReferences.portraitPlaceholderPath("portrait_placeholder.png")

    Box(
        modifier = modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color(0x55A19892),
                radius = size.minDimension / 2f - 4.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0x42FFFFFF),
                radius = size.minDimension / 2f - 12.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Surface(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = Color(0xFF141118)
        ) {
            AppImage(
                imageRef = portraitReference,
                contentDescription = characterName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallback = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2D2730)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = characterName.take(1).ifBlank { "?" },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = LocalDesignTokens.current.typography.portraitInitial.fontSizeSp.sp
                            ),
                            color = Color(0xFFF7F2EA)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun DrawerActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color(0xFFF1ECE5),
    dashed: Boolean = false
) {
    val tokens = LocalDesignTokens.current.typography

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF17141B).copy(alpha = 0.48f),
        border = BorderStroke(1.dp, if (dashed) Color(0xFF706359) else Color(0x42FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 28.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = tokens.titleMedium.fontSizeSp.sp),
                color = Color(0xFFD2CAC2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DrawerLanguageCard(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val tokens = LocalDesignTokens.current.typography

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF17141B).copy(alpha = 0.48f),
        border = BorderStroke(1.dp, Color(0x42FFFFFF))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = null,
                tint = Color(0xFFF1ECE5),
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = label,
                modifier = Modifier
                    .padding(start = 28.dp)
                    .weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = tokens.titleMedium.fontSizeSp.sp),
                color = Color(0xFFD2CAC2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD2CAC2),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFC6A36C),
                modifier = Modifier.padding(start = 10.dp).size(30.dp)
            )
        }
    }
}

@Composable
private fun DrawerOrnamentDivider(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        val centerY = size.height / 2f
        val ornamentCenterX = size.width / 2f
        val gap = 16.dp.toPx()
        drawLine(
            color = Color(0x55A19892),
            start = Offset(0f, centerY),
            end = Offset(ornamentCenterX - gap, centerY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0x55A19892),
            start = Offset(ornamentCenterX + gap, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = Color(0xFFF7F2EA),
            radius = 2.dp.toPx(),
            center = Offset(ornamentCenterX, centerY)
        )
        drawCircle(
            color = Color(0x55A19892),
            radius = 7.dp.toPx(),
            center = Offset(ornamentCenterX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

private fun buildDrawerClassLabel(
    characterBundle: CharacterBundle,
    strings: com.dndcharacterhandler.data.localization.LocalizedStrings
): String {
    val character = characterBundle.character
    val race = character.race.ifBlank { strings["placeholder_race"] }
    val characterClass = character.characterClass.ifBlank { strings["placeholder_class"] }
    val subclass = character.subclass.ifBlank { null }
    val classLabel = if (subclass != null) "$subclass $characterClass" else characterClass
    return "$race • $classLabel"
}
