package com.dndcharacterhandler.presentation.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.InventoryArmorDetails
import com.dndcharacterhandler.domain.model.InventoryArmorType
import com.dndcharacterhandler.domain.model.InventoryCatalogItem
import com.dndcharacterhandler.domain.model.InventoryCategory
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.domain.repository.CharacterRepository
import com.dndcharacterhandler.domain.repository.InventoryCatalogRepository
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.CharacterScreenHeader
import com.dndcharacterhandler.presentation.components.FloatingAddButton
import com.dndcharacterhandler.presentation.components.ScreenBackground
import com.dndcharacterhandler.presentation.components.ScreenTopActions
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.theme.LocalDesignTokens
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class InventoryViewModel(
    private val characterRepository: CharacterRepository,
    private val inventoryCatalogRepository: InventoryCatalogRepository,
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder) {
    private val _catalogUiState = MutableStateFlow(InventoryCatalogUiState())
    val catalogUiState: StateFlow<InventoryCatalogUiState> = _catalogUiState.asStateFlow()

    init {
        viewModelScope.launch {
            val items = inventoryCatalogRepository.getItems()
            _catalogUiState.value = InventoryCatalogUiState(items = items, isLoading = false)
        }
    }

    fun addCatalogItem(characterBundle: CharacterBundle, item: InventoryCatalogItem) {
        viewModelScope.launch {
            characterRepository.upsertCharacter(
                characterBundle.copy(
                    inventoryItems = characterBundle.inventoryItems + item.toInventoryItem(),
                    character = characterBundle.character.copy(updatedAt = System.currentTimeMillis())
                )
            )
        }
    }

    fun toggleItemEquipped(characterBundle: CharacterBundle, targetItem: InventoryItem) {
        viewModelScope.launch {
            val updatedItems = characterBundle.inventoryItems.toggleEquippedItem(targetItem)
            characterRepository.upsertCharacter(
                characterBundle.copy(
                    inventoryItems = updatedItems,
                    character = characterBundle.character.copy(
                        armorClass = if (characterBundle.character.armorClassMode == ArmorClassMode.AUTOMATIC) {
                            calculateArmorClass(
                                baseArmorClass = characterBundle.character.baseArmorClass,
                                dexterityScore = characterBundle.character.dexterity,
                                inventoryItems = updatedItems
                            )
                        } else {
                            characterBundle.character.armorClass
                        },
                        updatedAt = System.currentTimeMillis()
                    )
                )
            )
        }
    }
}

data class InventoryCatalogUiState(
    val items: List<InventoryCatalogItem> = emptyList(),
    val isLoading: Boolean = true
)

@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val catalogState by viewModel.catalogUiState.collectAsStateWithLifecycle()
    var isCatalogOpen by remember { mutableStateOf(false) }

    InventoryContent(
        characterBundle = state.character,
        onOpenDrawer = onOpenDrawer,
        onOpenSettings = onOpenSettings,
        onAddItem = { isCatalogOpen = true },
        onToggleEquipped = { characterBundle, item ->
            viewModel.toggleItemEquipped(characterBundle, item)
        }
    )

    val characterBundle = state.character
    if (isCatalogOpen && characterBundle != null) {
        InventoryCatalogDialog(
            catalogItems = catalogState.items,
            isLoading = catalogState.isLoading,
            onDismissRequest = { isCatalogOpen = false },
            onSelectItem = { item ->
                viewModel.addCatalogItem(characterBundle, item)
                isCatalogOpen = false
            }
        )
    }
}

@Composable
internal fun InventoryContent(
    characterBundle: CharacterBundle?,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onAddItem: () -> Unit = {},
    onToggleEquipped: (CharacterBundle, InventoryItem) -> Unit = { _, _ -> }
) {
    val character = characterBundle?.character
    var query by remember { mutableStateOf("") }

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

    val items = remember(characterBundle.inventoryItems, query) {
        val needle = query.trim()
        characterBundle.inventoryItems.filter { item ->
            needle.isBlank() ||
                item.name.contains(needle, ignoreCase = true) ||
                item.category.name.contains(needle, ignoreCase = true)
        }
    }
    val totalWeight = remember(characterBundle.inventoryItems) {
        characterBundle.inventoryItems.sumOf { it.weight * it.quantity }
    }
    val carryLimit = (character.strength.coerceAtLeast(1) * 15).toDouble()

    ScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    CharacterScreenHeader(
                        character = character,
                        onOpenDrawer = onOpenDrawer,
                        onOpenSettings = onOpenSettings
                    )
                }

                item {
                    CarryWeightBlock(
                        current = totalWeight,
                        maximum = carryLimit
                    )
                }

                item {
                    InventorySearchField(
                        value = query,
                        onValueChange = { query = it }
                    )
                }

                InventoryCategory.values().forEach { category ->
                    val categoryItems = items.filter { it.category == category }
                    if (categoryItems.isNotEmpty()) {
                        item {
                            InventorySectionTitle(title = category.title())
                        }
                        item {
                            InventorySectionCard(
                                items = categoryItems,
                                dexterityScore = character.dexterity,
                                onToggleEquipped = { item -> onToggleEquipped(characterBundle, item) }
                            )
                        }
                    }
                }

                if (items.isEmpty()) {
                    item {
                        EmptyInventoryMessage()
                    }
                }
            }

            FloatingAddButton(
                onClick = onAddItem,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 15.dp)
            )
        }
    }
}

@Composable
private fun CarryWeightBlock(
    current: Double,
    maximum: Double
) {
    val safeMaximum = maximum.coerceAtLeast(1.0)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text("inventory_carry_weight"),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF7F2EA)
            )
            Text(
                text = "${formatWeight(current)} / ${formatWeight(safeMaximum)} lb",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD2CAC2)
            )
        }
        LinearProgressIndicator(
            progress = { (current / safeMaximum).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = Color(0xFFD7D1CC),
            trackColor = Color(0x22FFFFFF)
        )
    }
}

@Composable
private fun InventorySearchField(
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
                    text = text("inventory_search_placeholder"),
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
private fun InventorySectionTitle(title: String) {
    val tokens = LocalDesignTokens.current.typography
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color(0x33FFFFFF),
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun InventoryCatalogDialog(
    catalogItems: List<InventoryCatalogItem>,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
    onSelectItem: (InventoryCatalogItem) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filteredItems = remember(catalogItems, query) {
        val needle = query.trim()
        catalogItems.filter { item ->
            needle.isBlank() ||
                item.name.contains(needle, ignoreCase = true) ||
                item.description.contains(needle, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = text("inventory_catalog_title"),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InventorySearchField(
                    value = query,
                    onValueChange = { query = it }
                )
                when {
                    isLoading -> {
                        Text(
                            text = text("inventory_catalog_loading"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD2CAC2)
                        )
                    }

                    filteredItems.isEmpty() -> {
                        Text(
                            text = text("inventory_catalog_empty"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD2CAC2)
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(360.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredItems, key = { it.id }) { item ->
                                InventoryCatalogRow(
                                    item = item,
                                    onAdd = { onSelectItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text("common_cancel"))
            }
        }
    )
}

@Composable
private fun InventoryCatalogRow(
    item: InventoryCatalogItem,
    onAdd: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.62f),
        border = BorderStroke(1.dp, Color(0x36FFFFFF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFF7F2EA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.detailLine?.let { detail ->
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC2BBB3),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description.replace('\n', ' '),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD2CAC2),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (item.weight > 0.0) {
                    Text(
                        text = "${formatWeight(item.weight)} lb",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFC2BBB3)
                    )
                }
                IconButton(onClick = onAdd) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = text("inventory_add_item"),
                        tint = Color(0xFFF7F2EA)
                    )
                }
            }
        }
    }
}

@Composable
private fun InventorySectionCard(
    items: List<InventoryItem>,
    dexterityScore: Int,
    onToggleEquipped: (InventoryItem) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.62f),
        border = BorderStroke(1.dp, Color(0x36FFFFFF))
    ) {
        Column {
            items.forEachIndexed { index, item ->
                InventoryItemRow(
                    item = item,
                    dexterityScore = dexterityScore,
                    onToggleEquipped = { onToggleEquipped(item) }
                )
                if (index != items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0x1FFFFFFF))
                    )
                }
            }
        }
    }
}

@Composable
private fun InventoryItemRow(
    item: InventoryItem,
    dexterityScore: Int,
    onToggleEquipped: () -> Unit
) {
    val propertyTags = remember(item, dexterityScore) { item.propertyTags(dexterityScore) }

    Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.Inventory2,
                contentDescription = null,
                tint = Color(0xFFD2CAC2),
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = item.name,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF7F2EA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatWeight(item.weight)} lb",
                modifier = Modifier.padding(start = 10.dp, end = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFC2BBB3),
                maxLines = 1
            )
            Text(
                text = "x${item.quantity}",
                modifier = Modifier.padding(horizontal = 12.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFD2CAC2)
            )
            EquippedDot(
                selected = item.isEquipped,
                onClick = onToggleEquipped
            )
        }

        if (propertyTags.isNotEmpty()) {
            InventoryPropertyTags(
                tags = propertyTags,
                modifier = Modifier.padding(start = 38.dp)
            )
        }
    }
}

@Composable
private fun EquippedDot(
    selected: Boolean,
    onClick: () -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(22.dp)
            .clickable(onClick = onClick)
    ) {
        drawCircle(
            color = if (selected) Color(0xFFF7F2EA) else Color.Transparent,
            radius = size.minDimension * 0.32f
        )
        drawCircle(
            color = Color(0xFFC2BBB3),
            radius = size.minDimension * 0.42f,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
private fun EmptyInventoryMessage() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.42f),
        border = BorderStroke(1.dp, Color(0x30FFFFFF))
    ) {
        Text(
            text = text("inventory_empty"),
            modifier = Modifier.padding(18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFD2CAC2)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InventoryPropertyTags(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = Color(0x22FFF6EA),
                border = BorderStroke(1.dp, Color(0x30FFFFFF))
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFE6DED3),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun InventoryCategory.title(): String =
    when (this) {
        InventoryCategory.WEAPON -> text("inventory_category_weapon")
        InventoryCategory.ARMOR -> text("inventory_category_armor")
        InventoryCategory.CONSUMABLE -> text("inventory_category_consumable")
        InventoryCategory.OTHER -> text("inventory_category_other")
    }

private fun formatWeight(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toInt().toString()
    } else {
        rounded.toString()
    }
}

private fun InventoryItem.propertyTags(dexterityScore: Int): List<String> {
    val armor = armorDetails ?: return emptyList()
    return buildList {
        add(armor.armorType.label())
        add("${armor.armorClass} AC")
        armor.currentDexterityTag(dexterityScore)?.let(::add)
        if (armor.strengthMinimum > 0) {
            add("Str ${armor.strengthMinimum}")
        }
        if (armor.hasStealthDisadvantage) {
            add("Stealth Dis")
        }
    }
}

private fun InventoryArmorType.label(): String =
    when (this) {
        InventoryArmorType.LIGHT -> "Light"
        InventoryArmorType.MEDIUM -> "Medium"
        InventoryArmorType.HEAVY -> "Heavy"
        InventoryArmorType.SHIELD -> "Shield"
    }

private fun InventoryArmorDetails.currentDexterityTag(dexterityScore: Int): String? {
    if (!appliesDexterityBonus) return null

    val rawModifier = abilityModifier(dexterityScore)
    val appliedModifier = maxDexterityBonus?.let { cap ->
        rawModifier.coerceAtMost(cap)
    } ?: rawModifier

    return "${appliedModifier.signedValue()} Dex"
}

private fun abilityModifier(score: Int): Int = Math.floorDiv(score - 10, 2)

private fun Int.signedValue(): String = if (this >= 0) "+$this" else toString()

private fun List<InventoryItem>.toggleEquippedItem(targetItem: InventoryItem): List<InventoryItem> {
    val targetArmorType = targetItem.armorDetails?.armorType
    val shouldEquip = !targetItem.isEquipped

    return map { currentItem ->
        val isTarget = currentItem.matchesInventoryItem(targetItem)
        val currentArmorType = currentItem.armorDetails?.armorType

        when {
            isTarget -> currentItem.copy(isEquipped = shouldEquip)
            !shouldEquip -> currentItem
            targetArmorType == InventoryArmorType.SHIELD && currentArmorType == InventoryArmorType.SHIELD ->
                currentItem.copy(isEquipped = false)
            targetArmorType != null && targetArmorType != InventoryArmorType.SHIELD &&
                currentArmorType != null && currentArmorType != InventoryArmorType.SHIELD ->
                currentItem.copy(isEquipped = false)
            else -> currentItem
        }
    }
}

private fun InventoryItem.matchesInventoryItem(other: InventoryItem): Boolean {
    return if (id != 0L && other.id != 0L) {
        id == other.id
    } else {
        this == other
    }
}

private fun calculateArmorClass(
    baseArmorClass: Int,
    dexterityScore: Int,
    inventoryItems: List<InventoryItem>
): Int {
    val dexterityModifier = abilityModifier(dexterityScore)
    val equippedArmor = inventoryItems.firstOrNull {
        it.isEquipped && it.armorDetails?.armorType != null && it.armorDetails.armorType != InventoryArmorType.SHIELD
    }?.armorDetails
    val equippedShield = inventoryItems.firstOrNull {
        it.isEquipped && it.armorDetails?.armorType == InventoryArmorType.SHIELD
    }?.armorDetails

    val effectiveArmorClass = if (equippedArmor != null) {
        equippedArmor.armorClass + equippedArmor.appliedDexterityModifier(dexterityModifier)
    } else {
        baseArmorClass + dexterityModifier
    }

    val shieldBonus = equippedShield?.armorClass ?: 0
    return (effectiveArmorClass + shieldBonus).coerceAtLeast(1)
}

private fun InventoryArmorDetails.appliedDexterityModifier(dexterityModifier: Int): Int {
    if (!appliesDexterityBonus) return 0
    return maxDexterityBonus?.let { dexterityModifier.coerceAtMost(it) } ?: dexterityModifier
}
