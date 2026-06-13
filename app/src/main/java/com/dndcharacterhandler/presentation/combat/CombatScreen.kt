package com.dndcharacterhandler.presentation.combat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.dndcharacterhandler.domain.model.Attack
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.domain.model.InventoryArmorType
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.domain.model.SpellcastingAbility
import com.dndcharacterhandler.domain.repository.CharacterRepository
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.components.AppImage
import com.dndcharacterhandler.presentation.components.CharacterScreenHeader
import com.dndcharacterhandler.presentation.components.FloatingAddButton
import com.dndcharacterhandler.presentation.components.ScreenBackground
import com.dndcharacterhandler.presentation.components.ScreenTopActions
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.theme.LocalDesignTokens
import kotlinx.coroutines.launch
import kotlin.math.max

class CombatViewModel(
    private val characterRepository: CharacterRepository,
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder) {
    fun updateArmorClass(
        characterBundle: CharacterBundle,
        baseArmorClass: Int,
        armorClassMode: ArmorClassMode,
        manualArmorClass: Int?
    ) {
        val current = characterBundle.character
        val sanitizedBaseArmorClass = baseArmorClass.coerceAtLeast(1)
        val sanitizedArmorClass = when (armorClassMode) {
            ArmorClassMode.AUTOMATIC -> calculateArmorClass(
                baseArmorClass = sanitizedBaseArmorClass,
                dexterityScore = current.dexterity,
                inventoryItems = characterBundle.inventoryItems
            )
            ArmorClassMode.MANUAL -> manualArmorClass?.coerceAtLeast(1) ?: current.armorClass
        }
        if (
            sanitizedArmorClass == current.armorClass &&
            sanitizedBaseArmorClass == current.baseArmorClass &&
            armorClassMode == current.armorClassMode
        ) return

        saveBundle(
            characterBundle.copy(
                character = current.copy(
                    armorClass = sanitizedArmorClass,
                    baseArmorClass = sanitizedBaseArmorClass,
                    armorClassMode = armorClassMode,
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
    }

    fun updateSpellcastingAbility(characterBundle: CharacterBundle, ability: SpellcastingAbility) {
        val current = characterBundle.character
        if (current.spellcastingAbility == ability) return
        saveBundle(
            characterBundle.copy(
                character = current.copy(
                    spellcastingAbility = ability,
                    updatedAt = System.currentTimeMillis()
                )
            )
        )
    }

    fun updateAttack(characterBundle: CharacterBundle, attack: Attack) {
        val updatedAttacks = if (attack.id == 0L) {
            characterBundle.attacks + attack.copy(id = 0)
        } else {
            characterBundle.attacks.map { if (it.id == attack.id) attack else it }
        }
        saveBundle(characterBundle.copy(attacks = updatedAttacks))
    }

    fun deleteAttack(characterBundle: CharacterBundle, attack: Attack) {
        saveBundle(characterBundle.copy(attacks = characterBundle.attacks.filterNot { it.id == attack.id }))
    }

    fun updateCombatResourceUses(characterBundle: CharacterBundle, resourceId: Long, delta: Int) {
        val updatedResources = characterBundle.combatResources.map { resource ->
            if (resource.id != resourceId) {
                resource
            } else {
                resource.copy(
                    currentUses = (resource.currentUses + delta).coerceIn(0, resource.maximumUses.coerceAtLeast(0))
                )
            }
        }
        saveBundle(characterBundle.copy(combatResources = updatedResources))
    }

    fun updateCombatResource(characterBundle: CharacterBundle, resource: CombatResource) {
        val updatedResources = if (resource.id == 0L) {
            characterBundle.combatResources + resource.copy(id = 0)
        } else {
            characterBundle.combatResources.map { if (it.id == resource.id) resource else it }
        }
        saveBundle(characterBundle.copy(combatResources = updatedResources))
    }

    fun deleteCombatResource(characterBundle: CharacterBundle, resource: CombatResource) {
        saveBundle(characterBundle.copy(combatResources = characterBundle.combatResources.filterNot { it.id == resource.id }))
    }

    private fun saveBundle(characterBundle: CharacterBundle) {
        viewModelScope.launch {
            characterRepository.upsertCharacter(
                characterBundle.copy(
                    character = characterBundle.character.copy(updatedAt = System.currentTimeMillis())
                )
            )
        }
    }
}

@Composable
fun CombatScreen(
    viewModel: CombatViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CombatContent(
        characterBundle = state.character,
        onOpenDrawer = onOpenDrawer,
        onOpenSettings = onOpenSettings,
        onUpdateArmorClass = viewModel::updateArmorClass,
        onUpdateSpellcastingAbility = viewModel::updateSpellcastingAbility,
        onUpdateAttack = viewModel::updateAttack,
        onDeleteAttack = viewModel::deleteAttack,
        onUpdateCombatResourceUses = viewModel::updateCombatResourceUses,
        onUpdateCombatResource = viewModel::updateCombatResource,
        onDeleteCombatResource = viewModel::deleteCombatResource
    )
}

@Composable
internal fun CombatContent(
    characterBundle: CharacterBundle?,
    onOpenDrawer: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onUpdateArmorClass: (CharacterBundle, Int, ArmorClassMode, Int?) -> Unit = { _, _, _, _ -> },
    onUpdateSpellcastingAbility: (CharacterBundle, SpellcastingAbility) -> Unit = { _, _ -> },
    onUpdateAttack: (CharacterBundle, Attack) -> Unit = { _, _ -> },
    onDeleteAttack: (CharacterBundle, Attack) -> Unit = { _, _ -> },
    onUpdateCombatResourceUses: (CharacterBundle, Long, Int) -> Unit = { _, _, _ -> },
    onUpdateCombatResource: (CharacterBundle, CombatResource) -> Unit = { _, _ -> },
    onDeleteCombatResource: (CharacterBundle, CombatResource) -> Unit = { _, _ -> }
) {
    val character = characterBundle?.character
    var editingAttack by remember { mutableStateOf<Attack?>(null) }
    var editingCombatResource by remember { mutableStateOf<CombatResource?>(null) }
    var isArmorClassDialogOpen by remember { mutableStateOf(false) }
    var isSpellcastingAbilityDialogOpen by remember { mutableStateOf(false) }
    var armorClassBaseDraft by remember(character?.id, character?.baseArmorClass) { mutableStateOf(character?.baseArmorClass?.toString().orEmpty()) }
    var armorClassManualDraft by remember(character?.id, character?.armorClass, character?.armorClassMode) {
        mutableStateOf(if (character?.armorClassMode == ArmorClassMode.MANUAL) character.armorClass.toString() else "")
    }
    var armorClassModeDraft by remember(character?.id, character?.armorClassMode) {
        mutableStateOf(character?.armorClassMode ?: ArmorClassMode.AUTOMATIC)
    }

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
    val spellModifier = abilityModifier(scoreForSpellcastingAbility(character, character.spellcastingAbility))
    val proficiencyBonus = proficiencyBonusForLevel(character.level)
    val spellAttackBonus = signedNumber(proficiencyBonus + spellModifier)
    val spellSaveDc = (8 + proficiencyBonus + spellModifier).toString()
    val resourceRows = remember(resolvedBundle.combatResources) {
        resolvedBundle.combatResources.chunked(3)
    }

    ScreenBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    CharacterScreenHeader(
                        character = character,
                        onOpenDrawer = onOpenDrawer,
                        onOpenSettings = onOpenSettings
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CombatMiniStatCard(
                            modifier = Modifier.weight(1f),
                            value = character.armorClass.toString(),
                            label = text("overview_ac_full"),
                            icon = Icons.Outlined.Shield,
                            onClick = {
                                armorClassBaseDraft = character.baseArmorClass.toString()
                                armorClassManualDraft = if (character.armorClassMode == ArmorClassMode.MANUAL) {
                                    character.armorClass.toString()
                                } else {
                                    ""
                                }
                                armorClassModeDraft = character.armorClassMode
                                isArmorClassDialogOpen = true
                            }
                        )
                        CombatMiniStatCard(
                            modifier = Modifier.weight(1f),
                            value = spellAttackBonus,
                            label = text("combat_spell_bonus"),
                            icon = Icons.Outlined.FlashOn,
                            onClick = { isSpellcastingAbilityDialogOpen = true }
                        )
                        CombatMiniStatCard(
                            modifier = Modifier.weight(1f),
                            value = spellSaveDc,
                            label = text("combat_spell_dc"),
                            icon = Icons.Outlined.Bolt,
                            onClick = { isSpellcastingAbilityDialogOpen = true }
                        )
                    }
                }

                item {
                    CombatSectionTitle(text("combat_section_attacks_title"))
                }

                if (resolvedBundle.attacks.isEmpty()) {
                    item {
                        CombatEmptyCard(text("combat_empty_attacks"))
                    }
                } else {
                    items(resolvedBundle.attacks, key = { it.id }) { attack ->
                        AttackCard(
                            attack = attack,
                            onClick = { editingAttack = attack }
                        )
                    }
                }

                item {
                    CombatSectionTitle(text("combat_combat_resources_title"))
                }

                if (resolvedBundle.combatResources.isEmpty()) {
                    item {
                        CombatEmptyCard(text("combat_empty_resources"))
                    }
                } else {
                    items(resourceRows.size) { index ->
                        CombatResourceRow(
                            resources = resourceRows[index],
                            onEdit = { editingCombatResource = it },
                            onAdjust = { resourceId, delta ->
                                onUpdateCombatResourceUses(resolvedBundle, resourceId, delta)
                            }
                        )
                    }
                }
            }

            FloatingAddButton(
                onClick = { editingAttack = newDraftAttack() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 15.dp)
            )
        }
    }

    editingAttack?.let { attack ->
        AttackEditDialog(
            attack = attack,
            onDismiss = { editingAttack = null },
            onSave = { updated ->
                onUpdateAttack(resolvedBundle, updated)
                editingAttack = null
            },
            onDelete = if (attack.id != 0L) {
                {
                    onDeleteAttack(resolvedBundle, attack)
                    editingAttack = null
                }
            } else {
                null
            }
        )
    }

    if (isArmorClassDialogOpen) {
        val parsedBaseArmorClass = armorClassBaseDraft.toIntOrNull()?.coerceAtLeast(1) ?: 10
        val parsedManualArmorClass = armorClassManualDraft.toIntOrNull()?.coerceAtLeast(1)
        AlertDialog(
            onDismissRequest = { isArmorClassDialogOpen = false },
            title = { Text(text("overview_edit_ac_title")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = armorClassBaseDraft,
                        onValueChange = { value -> armorClassBaseDraft = value.filter(Char::isDigit) },
                        singleLine = true,
                        label = { Text(text("overview_ac_base")) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text(
                        text = text("overview_ac_mode"),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFF7F2EA)
                    )

                    ArmorClassModeOption(
                        title = text("overview_ac_mode_automatic"),
                        description = text("overview_ac_mode_automatic_hint"),
                        selected = armorClassModeDraft == ArmorClassMode.AUTOMATIC,
                        onClick = { armorClassModeDraft = ArmorClassMode.AUTOMATIC }
                    )
                    ArmorClassModeOption(
                        title = text("overview_ac_mode_manual"),
                        description = text("overview_ac_mode_manual_hint"),
                        selected = armorClassModeDraft == ArmorClassMode.MANUAL,
                        onClick = { armorClassModeDraft = ArmorClassMode.MANUAL }
                    )

                    if (armorClassModeDraft == ArmorClassMode.MANUAL) {
                        OutlinedTextField(
                            value = armorClassManualDraft,
                            onValueChange = { value -> armorClassManualDraft = value.filter(Char::isDigit) },
                            singleLine = true,
                            label = { Text(text("overview_ac_manual")) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateArmorClass(
                            resolvedBundle,
                            parsedBaseArmorClass,
                            armorClassModeDraft,
                            parsedManualArmorClass
                        )
                        isArmorClassDialogOpen = false
                    }
                ) {
                    Text(text("common_save"))
                }
            },
            dismissButton = {
                TextButton(onClick = { isArmorClassDialogOpen = false }) {
                    Text(text("common_cancel"))
                }
            }
        )
    }

    if (isSpellcastingAbilityDialogOpen) {
        AlertDialog(
            onDismissRequest = { isSpellcastingAbilityDialogOpen = false },
            title = { Text(text("combat_edit_spellcasting_ability")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    spellcastingAbilityOptions.forEach { ability ->
                        SpellcastingAbilityOption(
                            title = text(ability.labelKey),
                            selected = resolvedBundle.character.spellcastingAbility == ability.ability,
                            onClick = {
                                onUpdateSpellcastingAbility(resolvedBundle, ability.ability)
                                isSpellcastingAbilityDialogOpen = false
                            }
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { isSpellcastingAbilityDialogOpen = false }) {
                    Text(text("common_cancel"))
                }
            }
        )
    }

    editingCombatResource?.let { resource ->
        CombatResourceEditDialog(
            resource = resource,
            onDismiss = { editingCombatResource = null },
            onSave = { updated ->
                onUpdateCombatResource(resolvedBundle, updated)
                editingCombatResource = null
            },
            onDelete = if (resource.id != 0L) {
                {
                    onDeleteCombatResource(resolvedBundle, resource)
                    editingCombatResource = null
                }
            } else {
                null
            }
        )
    }
}

@Composable
private fun CombatMiniStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val tokens = LocalDesignTokens.current.typography
    Surface(
        modifier = modifier
            .height(92.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0x42FFFFFF)),
        color = Color(0xFF17141B).copy(alpha = 0.62f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = tokens.bodyLarge.fontSizeSp.sp),
                color = Color(0xFFBEB6AE),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFD8D1CA),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = tokens.miniStatValue.fontSizeSp.sp,
                        lineHeight = (tokens.miniStatValue.lineHeightSp ?: tokens.miniStatValue.fontSizeSp).sp
                    ),
                    color = Color(0xFFF7F2EA),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ArmorClassModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0x1FFFFFFF) else Color(0x0FFFFFFF),
        border = BorderStroke(1.dp, if (selected) Color(0x66FFF6EA) else Color(0x20FFFFFF))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFF7F2EA)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFD2CAC2)
            )
        }
    }
}

@Composable
private fun SpellcastingAbilityOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) Color(0x1FFFFFFF) else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) Color(0x66FFF6EA) else Color(0x20FFFFFF))
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFF7F2EA)
        )
    }
}

@Composable
private fun CombatSectionTitle(title: String) {
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
private fun AttackCard(
    attack: Attack,
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
            Surface(
                modifier = Modifier.size(82.dp),
                shape = RoundedCornerShape(10.dp),
                color = Color(0x1AFFFFFF),
                border = BorderStroke(1.dp, Color(0x20FFFFFF))
            ) {
                AppImage(
                    imageRef = attack.icon,
                    contentDescription = attack.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    fallback = {
                        Icon(
                            imageVector = attackFallbackIcon(attack),
                            contentDescription = null,
                            tint = Color(0xFFD2CAC2),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = attack.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFF7F2EA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                attack.range.takeIf { it.isNotBlank() }?.let { range ->
                    RangeTag(range)
                }
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(72.dp)
                    .background(Color(0x20FFFFFF))
            )

            Column(
                modifier = Modifier
                    .weight(0.9f)
                    .padding(start = 14.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = attack.attackBonusOrSaveDc,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF7F2EA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = attack.damage,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE9DBC8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = attack.damageType,
                    style = MaterialTheme.typography.bodyLarge,
                    color = damageTypeColor(attack.damageType),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RangeTag(value: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF2A2630),
        border = BorderStroke(1.dp, Color(0x18FFFFFF))
    ) {
        Text(
            text = value,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD2CAC2)
        )
    }
}

@Composable
private fun CombatEmptyCard(label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.62f),
        border = BorderStroke(1.dp, Color(0x36FFFFFF))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFD2CAC2)
        )
    }
}

@Composable
private fun CombatResourceRow(
    resources: List<CombatResource>,
    onEdit: (CombatResource) -> Unit,
    onAdjust: (Long, Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        resources.forEach { resource ->
            CombatResourceTile(
                resource = resource,
                modifier = Modifier.weight(1f),
                onEdit = onEdit,
                onAdjust = onAdjust
            )
        }
        repeat(max(0, 3 - resources.size)) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CombatResourceTile(
    resource: CombatResource,
    modifier: Modifier = Modifier,
    onEdit: (CombatResource) -> Unit,
    onAdjust: (Long, Int) -> Unit
) {
    Surface(
        modifier = modifier
            .height(92.dp)
            .clickable { onEdit(resource) },
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF17141B).copy(alpha = 0.62f),
        border = BorderStroke(1.dp, Color(0x36FFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = resource.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFF7F2EA),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StepperButton(
                    icon = Icons.Outlined.Remove,
                    enabled = resource.currentUses > 0,
                    onClick = { onAdjust(resource.id, -1) }
                )
                Text(
                    text = "${resource.currentUses}/${resource.maximumUses}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFF7F2EA),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
                StepperButton(
                    icon = Icons.Outlined.Add,
                    enabled = resource.currentUses < resource.maximumUses,
                    onClick = { onAdjust(resource.id, 1) }
                )
            }
        }
    }
}

@Composable
private fun CombatResourceEditDialog(
    resource: CombatResource,
    onDismiss: () -> Unit,
    onSave: (CombatResource) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(resource) { mutableStateOf(resource.name) }
    var currentUses by remember(resource) { mutableStateOf(resource.currentUses.toString()) }
    var maximumUses by remember(resource) { mutableStateOf(resource.maximumUses.toString()) }
    var restoresOnShortRest by remember(resource) { mutableStateOf(resource.restoresOnShortRest) }
    var restoresOnLongRest by remember(resource) { mutableStateOf(resource.restoresOnLongRest) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text("combat_edit_resource")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text("features_name")) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = currentUses,
                    onValueChange = { currentUses = it.filter(Char::isDigit) },
                    label = { Text(text("combat_resource_current")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = maximumUses,
                    onValueChange = { maximumUses = it.filter(Char::isDigit) },
                    label = { Text(text("combat_resource_maximum")) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                ResourceCheckboxRow(
                    checked = restoresOnShortRest,
                    label = text("combat_resource_short_rest"),
                    onCheckedChange = { restoresOnShortRest = it }
                )
                ResourceCheckboxRow(
                    checked = restoresOnLongRest,
                    label = text("combat_resource_long_rest"),
                    onCheckedChange = { restoresOnLongRest = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedMax = maximumUses.toIntOrNull()?.coerceAtLeast(0) ?: 0
                    val parsedCurrent = (currentUses.toIntOrNull() ?: 0).coerceIn(0, parsedMax)
                    onSave(
                        resource.copy(
                            name = name.trim(),
                            currentUses = parsedCurrent,
                            maximumUses = parsedMax,
                            restoresOnShortRest = restoresOnShortRest,
                            restoresOnLongRest = restoresOnLongRest
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
private fun ResourceCheckboxRow(
    checked: Boolean,
    label: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFF7F2EA)
        )
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (enabled) Color(0xFF1E1A22) else Color(0xFF17141B),
        border = BorderStroke(1.dp, if (enabled) Color(0x22FFFFFF) else Color(0x14FFFFFF))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) Color(0xFFF7F2EA) else Color(0x66D2CAC2),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun AttackEditDialog(
    attack: Attack,
    onDismiss: () -> Unit,
    onSave: (Attack) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember(attack) { mutableStateOf(attack.name) }
    var range by remember(attack) { mutableStateOf(attack.range) }
    var attackBonusOrSaveDc by remember(attack) { mutableStateOf(attack.attackBonusOrSaveDc) }
    var damage by remember(attack) { mutableStateOf(attack.damage) }
    var damageType by remember(attack) { mutableStateOf(attack.damageType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text(if (attack.id == 0L) "combat_add_attack" else "combat_edit_attack")) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text("features_name")) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = range,
                    onValueChange = { range = it },
                    label = { Text(text("combat_attack_range")) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = attackBonusOrSaveDc,
                    onValueChange = { attackBonusOrSaveDc = it },
                    label = { Text(text("combat_attack_bonus_or_dc")) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = damage,
                    onValueChange = { damage = it },
                    label = { Text(text("combat_attack_damage")) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = damageType,
                    onValueChange = { damageType = it },
                    label = { Text(text("combat_attack_damage_type")) },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        attack.copy(
                            name = name.trim(),
                            range = range.trim(),
                            attackBonusOrSaveDc = attackBonusOrSaveDc.trim(),
                            damage = damage.trim(),
                            damageType = damageType.trim()
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

private fun newDraftAttack(): Attack =
    Attack(
        id = 0,
        name = "",
        icon = "",
        range = "",
        attackBonusOrSaveDc = "",
        damage = "",
        damageType = ""
    )

private fun proficiencyBonusForLevel(level: Int): Int = 2 + ((level.coerceAtLeast(1) - 1) / 4)

private fun signedNumber(value: Int): String = if (value >= 0) "+$value" else value.toString()

private fun attackFallbackIcon(attack: Attack): ImageVector {
    val description = "${attack.name} ${attack.damageType} ${attack.range}".lowercase()
    return when {
        "fire" in description || "bolt" in description -> Icons.Outlined.Bolt
        "cold" in description -> Icons.Outlined.FlashOn
        else -> Icons.Outlined.Shield
    }
}

private fun damageTypeColor(value: String): Color {
    val key = value.lowercase()
    return when {
        "fire" in key -> Color(0xFFFF8A3D)
        "cold" in key -> Color(0xFF7BB7FF)
        "lightning" in key -> Color(0xFFCFB6FF)
        "poison" in key -> Color(0xFFA8D76F)
        else -> Color(0xFFD5C6B2)
    }
}

private fun abilityModifier(score: Int): Int = Math.floorDiv(score - 10, 2)

private fun scoreForSpellcastingAbility(
    character: com.dndcharacterhandler.domain.model.Character,
    ability: SpellcastingAbility
): Int =
    when (ability) {
        SpellcastingAbility.STRENGTH -> character.strength
        SpellcastingAbility.DEXTERITY -> character.dexterity
        SpellcastingAbility.CONSTITUTION -> character.constitution
        SpellcastingAbility.INTELLIGENCE -> character.intelligence
        SpellcastingAbility.WISDOM -> character.wisdom
        SpellcastingAbility.CHARISMA -> character.charisma
    }

private fun calculateArmorClass(
    baseArmorClass: Int,
    dexterityScore: Int,
    inventoryItems: List<InventoryItem>
): Int {
    val equippedArmor = inventoryItems.firstOrNull { item ->
        item.isEquipped && item.armorDetails?.armorType != InventoryArmorType.SHIELD
    }?.armorDetails
    val equippedShield = inventoryItems.firstOrNull { item ->
        item.isEquipped && item.armorDetails?.armorType == InventoryArmorType.SHIELD
    }?.armorDetails
    val dexterityModifier = abilityModifier(dexterityScore)

    val armorClass = if (equippedArmor == null) {
        baseArmorClass + dexterityModifier
    } else {
        val dexterityBonus = if (!equippedArmor.appliesDexterityBonus) {
            0
        } else {
            equippedArmor.maxDexterityBonus?.let { dexterityModifier.coerceAtMost(it) } ?: dexterityModifier
        }
        equippedArmor.armorClass + dexterityBonus
    }

    return armorClass + (equippedShield?.armorClass ?: 0)
}

private data class SpellcastingAbilityOptionItem(
    val ability: SpellcastingAbility,
    val labelKey: String
)

private val spellcastingAbilityOptions = listOf(
    SpellcastingAbilityOptionItem(SpellcastingAbility.STRENGTH, "ability_strength"),
    SpellcastingAbilityOptionItem(SpellcastingAbility.DEXTERITY, "ability_dexterity"),
    SpellcastingAbilityOptionItem(SpellcastingAbility.CONSTITUTION, "ability_constitution"),
    SpellcastingAbilityOptionItem(SpellcastingAbility.INTELLIGENCE, "ability_intelligence"),
    SpellcastingAbilityOptionItem(SpellcastingAbility.WISDOM, "ability_wisdom"),
    SpellcastingAbilityOptionItem(SpellcastingAbility.CHARISMA, "ability_charisma")
)
