package com.dndcharacterhandler.presentation.features

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.Feature
import com.dndcharacterhandler.domain.model.FeatureSource
import com.dndcharacterhandler.domain.repository.CharacterRepository
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.CharacterScreenHeader
import com.dndcharacterhandler.presentation.components.FloatingAddButton
import com.dndcharacterhandler.presentation.components.ScreenBackground
import com.dndcharacterhandler.presentation.components.ScreenTopActions
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.theme.LocalDesignTokens
import kotlinx.coroutines.launch

class FeaturesViewModel(
    private val characterRepository: CharacterRepository,
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder) {
    fun updateFeature(characterBundle: CharacterBundle, feature: Feature) {
        if (feature.id == 0L) {
            createFeature(characterBundle, feature)
        } else {
            saveFeatures(
                characterBundle = characterBundle,
                features = characterBundle.features.map { if (it.id == feature.id) feature else it }
            )
        }
    }

    fun deleteFeature(characterBundle: CharacterBundle, feature: Feature) {
        saveFeatures(
            characterBundle = characterBundle,
            features = characterBundle.features.filterNot { it.id == feature.id }
        )
    }

    private fun createFeature(characterBundle: CharacterBundle, feature: Feature) {
        saveFeatures(
            characterBundle = characterBundle,
            features = characterBundle.features + feature.copy(id = 0)
        )
    }

    private fun saveFeatures(characterBundle: CharacterBundle, features: List<Feature>) {
        viewModelScope.launch {
            characterRepository.upsertCharacter(
                characterBundle.copy(
                    character = characterBundle.character.copy(updatedAt = System.currentTimeMillis()),
                    features = features
                )
            )
        }
    }
}

@Composable
fun FeaturesScreen(
    viewModel: FeaturesViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FeaturesContent(
        characterBundle = state.character,
        onOpenDrawer = onOpenDrawer,
        onOpenSettings = onOpenSettings,
        onUpdateFeature = viewModel::updateFeature,
        onDeleteFeature = viewModel::deleteFeature
    )
}

@Composable
internal fun FeaturesContent(
    characterBundle: CharacterBundle?,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onUpdateFeature: (CharacterBundle, Feature) -> Unit = { _, _ -> },
    onDeleteFeature: (CharacterBundle, Feature) -> Unit = { _, _ -> }
) {
    val character = characterBundle?.character
    var query by remember { mutableStateOf("") }
    var editingFeature by remember { mutableStateOf<Feature?>(null) }

    if (character == null) {
        ScreenBackground {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, end = 24.dp, top = 4.dp)
            ) {
                ScreenTopActions(
                    onOpenDrawer = onOpenDrawer,
                    onOpenSettings = onOpenSettings,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                Text(
                    text = text("placeholder_loading_character"),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD7D1CC)
                )
            }
        }
        return
    }

    val resolvedBundle = characterBundle
    val visibleFeatures = remember(resolvedBundle.features, query) {
        val needle = query.trim()
        resolvedBundle.features.filter { feature ->
            needle.isBlank() ||
                feature.name.contains(needle, ignoreCase = true) ||
                feature.description.contains(needle, ignoreCase = true)
        }
    }
    val groupedFeatures = remember(visibleFeatures) {
        featureSourceOrder.mapNotNull { source ->
            visibleFeatures
                .filter { it.source == source }
                .takeIf { it.isNotEmpty() }
                ?.let { source to it }
        }
    }

    ScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    ScreenTopActions(
                        onOpenDrawer = onOpenDrawer,
                        onOpenSettings = onOpenSettings
                    )
                }

                item {
                    CharacterScreenHeader(
                        character = character,
                        onOpenDrawer = onOpenDrawer,
                        onOpenSettings = onOpenSettings,
                        showTopActions = false
                    )
                }

                item {
                    FeaturesSearchField(
                        value = query,
                        onValueChange = { query = it }
                    )
                }

                groupedFeatures.forEach { (source, features) ->
                    item(key = "section_${source.name}") {
                        FeaturesSectionTitle(featureSourceLabel(source))
                    }

                    items(features, key = { it.id }) { feature ->
                        FeatureCard(
                            feature = feature,
                            onClick = { editingFeature = feature }
                        )
                    }
                }
            }

            FloatingAddButton(
                onClick = { editingFeature = newDraftFeature() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 15.dp)
            )
        }
    }

    editingFeature?.let { feature ->
        FeatureEditDialog(
            feature = feature,
            onDismiss = { editingFeature = null },
            onSave = { updated ->
                onUpdateFeature(resolvedBundle, updated)
                editingFeature = null
            },
            onDelete = if (feature.id != 0L) {
                {
                    onDeleteFeature(resolvedBundle, feature)
                    editingFeature = null
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun FeaturesSearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.62f),
        border = BorderStroke(1.dp, Color(0x36FFFFFF))
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = Color(0xFFD2CAC2),
                    modifier = Modifier.size(28.dp)
                )
            },
            placeholder = {
                Text(
                    text = text("features_search_placeholder"),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFD2CAC2).copy(alpha = 0.72f)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = Color(0xFFFFF6EA)
            )
        )
    }
}

@Composable
private fun FeaturesSectionTitle(title: String) {
    val tokens = LocalDesignTokens.current.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = tokens.headlineMedium.fontSizeSp.sp),
            color = Color(0xFFF7F2EA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
                .height(1.dp)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = androidx.compose.ui.geometry.Offset(0f, size.height / 2f),
                    end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun FeatureCard(
    feature: Feature,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.62f),
        border = BorderStroke(1.dp, Color(0x36FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoStories,
                contentDescription = null,
                tint = Color(0xFFD2CAC2),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(22.dp)
            )
            Text(
                text = feature.name.ifBlank { text("features_untitled") },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF7F2EA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFD2CAC2),
                modifier = Modifier.padding(start = 10.dp)
            )
        }
    }
}

@Composable
private fun FeatureEditDialog(
    feature: Feature,
    onDismiss: () -> Unit,
    onSave: (Feature) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(feature) { mutableStateOf(feature.name) }
    var description by remember(feature) { mutableStateOf(feature.description) }
    var level by remember(feature) { mutableStateOf(feature.level?.toString().orEmpty()) }
    var source by remember(feature) { mutableStateOf(feature.source) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text("features_edit_feature")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text("features_name")) },
                    singleLine = true
                )
                FeatureSourceField(
                    value = source,
                    onValueChange = { source = it }
                )
                OutlinedTextField(
                    value = level,
                    onValueChange = { value -> level = value.filter(Char::isDigit) },
                    label = { Text(text("features_level")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text("features_description")) },
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        feature.copy(
                            name = name.trim(),
                            description = description.trim(),
                            level = level.toIntOrNull(),
                            source = source
                        )
                    )
                }
            ) {
                Text(text("common_save"))
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text(text("inventory_delete_action"))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(text("common_cancel"))
                }
            }
        }
    )
}

@Composable
private fun FeatureSourceField(
    value: FeatureSource,
    onValueChange: (FeatureSource) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = text("features_source"),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFD2CAC2),
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF17141B).copy(alpha = 0.62f),
                border = BorderStroke(1.dp, Color(0x36FFFFFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = featureSourceLabel(value),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFF7F2EA),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                        tint = Color(0xFFD2CAC2)
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.widthIn(min = 220.dp)
            ) {
                featureSourceOrder.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(featureSourceLabel(option)) },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun featureSourceLabel(source: FeatureSource): String =
    when (source) {
        FeatureSource.RACE -> text("features_source_race")
        FeatureSource.BACKGROUND -> text("features_source_background")
        FeatureSource.CLASS -> text("features_source_class")
        FeatureSource.OTHER -> text("features_source_other")
    }

private fun newDraftFeature(): Feature =
    Feature(
        id = 0,
        name = "",
        description = "",
        level = null,
        source = FeatureSource.OTHER
    )

private val featureSourceOrder = listOf(
    FeatureSource.RACE,
    FeatureSource.BACKGROUND,
    FeatureSource.CLASS,
    FeatureSource.OTHER
)
