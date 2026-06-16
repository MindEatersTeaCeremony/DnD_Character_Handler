package com.dndcharacterhandler.domain.repository

import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.Attack
import com.dndcharacterhandler.domain.model.Spell
import com.dndcharacterhandler.domain.model.SpellcastingAbility
import com.dndcharacterhandler.domain.model.Feature
import com.dndcharacterhandler.domain.model.Note
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.Skill
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeCharacters(): Flow<List<CharacterBundle>>
    fun observeCharacter(characterId: Long): Flow<CharacterBundle?>
    suspend fun createCharacter(character: CharacterBundle): Long
    suspend fun upsertCharacter(character: CharacterBundle): Long
    suspend fun updateCharacterDetails(character: Character)
    suspend fun updateCurrency(characterId: Long, copperPieces: Int, silverPieces: Int, goldPieces: Int)
    suspend fun upsertInventoryItem(characterId: Long, item: InventoryItem): Long
    suspend fun deleteInventoryItem(characterId: Long, itemId: Long)
    suspend fun toggleInventoryItemEquipped(characterId: Long, itemId: Long)
    suspend fun upsertSpell(characterId: Long, spell: Spell): Long
    suspend fun deleteSpell(characterId: Long, spellId: Long)
    suspend fun updateSpellSlots(
        characterId: Long,
        spellSlotMaximums: String,
        spellSlotRemaining: String,
        restoresOnShortRest: Boolean,
        restoresOnLongRest: Boolean
    )
    suspend fun updateSpellSlotRemaining(characterId: Long, spellSlotRemaining: String)
    suspend fun updateSpellcastingAbility(characterId: Long, ability: SpellcastingAbility)
    suspend fun updateArmorClassSettings(
        characterId: Long,
        baseArmorClass: Int,
        armorClassMode: ArmorClassMode,
        manualArmorClass: Int?
    )
    suspend fun upsertAttack(characterId: Long, attack: Attack): Long
    suspend fun deleteAttack(characterId: Long, attackId: Long)
    suspend fun upsertCombatResource(characterId: Long, resource: CombatResource): Long
    suspend fun deleteCombatResource(characterId: Long, resourceId: Long)
    suspend fun updateCombatResourceUses(characterId: Long, resourceId: Long, delta: Int)
    suspend fun upsertFeature(characterId: Long, feature: Feature): Long
    suspend fun deleteFeature(characterId: Long, featureId: Long)
    suspend fun upsertNote(characterId: Long, note: Note): Long
    suspend fun deleteNote(characterId: Long, noteId: Long)
    suspend fun upsertSkill(characterId: Long, skill: Skill)
    suspend fun deleteCharacter(characterId: Long)
}
