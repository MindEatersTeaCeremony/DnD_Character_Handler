package com.dndcharacterhandler.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dndcharacterhandler.data.local.entity.AttackEntity
import com.dndcharacterhandler.data.local.entity.CharacterEntity
import com.dndcharacterhandler.data.local.entity.CharacterWithDetails
import com.dndcharacterhandler.data.local.entity.CombatResourceEntity
import com.dndcharacterhandler.data.local.entity.FeatureEntity
import com.dndcharacterhandler.data.local.entity.InventoryItemEntity
import com.dndcharacterhandler.data.local.entity.NoteEntity
import com.dndcharacterhandler.data.local.entity.SkillEntity
import com.dndcharacterhandler.data.local.entity.SpellEntity
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.InventoryArmorType
import com.dndcharacterhandler.domain.model.SpellcastingAbility
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    @Transaction
    @Query("SELECT * FROM characters ORDER BY updatedAt DESC")
    fun observeCharacters(): Flow<List<CharacterWithDetails>>

    @Transaction
    @Query("SELECT * FROM characters WHERE id = :characterId LIMIT 1")
    fun observeCharacter(characterId: Long): Flow<CharacterWithDetails?>

    @Transaction
    @Query("SELECT * FROM characters WHERE id = :characterId LIMIT 1")
    suspend fun getCharacter(characterId: Long): CharacterWithDetails?

    @Query("SELECT * FROM characters WHERE id = :characterId LIMIT 1")
    suspend fun getCharacterEntity(characterId: Long): CharacterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @androidx.room.Update
    suspend fun updateCharacterEntity(character: CharacterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSkillEntity(skill: SkillEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttacks(attacks: List<AttackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAttackEntity(attack: AttackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombatResources(resources: List<CombatResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCombatResourceEntity(resource: CombatResourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventoryItem(item: InventoryItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpells(spells: List<SpellEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpellEntity(spell: SpellEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(features: List<FeatureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFeatureEntity(feature: FeatureEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNoteEntity(note: NoteEntity): Long

    @Transaction
    suspend fun replaceCharacterBundle(
        character: CharacterEntity,
        skills: List<SkillEntity>,
        attacks: List<AttackEntity>,
        combatResources: List<CombatResourceEntity>,
        inventoryItems: List<InventoryItemEntity>,
        spells: List<SpellEntity>,
        features: List<FeatureEntity>,
        notes: List<NoteEntity>
    ): Long {
        val savedId = insertCharacter(character)
        val characterId = if (character.id == 0L) savedId else character.id

        deleteSkillsForCharacter(characterId)
        deleteAttacksForCharacter(characterId)
        deleteCombatResourcesForCharacter(characterId)
        deleteInventoryItemsForCharacter(characterId)
        deleteSpellsForCharacter(characterId)
        deleteFeaturesForCharacter(characterId)
        deleteNotesForCharacter(characterId)

        insertSkills(skills.map { it.copy(characterOwnerId = characterId) })
        insertAttacks(attacks.map { it.copy(characterOwnerId = characterId) })
        insertCombatResources(combatResources.map { it.copy(characterOwnerId = characterId) })
        insertInventoryItems(inventoryItems.map { it.copy(characterOwnerId = characterId) })
        insertSpells(spells.map { it.copy(characterOwnerId = characterId) })
        insertFeatures(features.map { it.copy(characterOwnerId = characterId) })
        insertNotes(notes.map { it.copy(characterOwnerId = characterId) })

        return characterId
    }

    @Query("DELETE FROM skills WHERE characterOwnerId = :characterId")
    suspend fun deleteSkillsForCharacter(characterId: Long)

    @Query("SELECT * FROM skills WHERE characterOwnerId = :characterId AND name = :skillName LIMIT 1")
    suspend fun getSkillForCharacter(characterId: Long, skillName: String): SkillEntity?

    @Query("DELETE FROM attacks WHERE characterOwnerId = :characterId")
    suspend fun deleteAttacksForCharacter(characterId: Long)

    @Query("DELETE FROM attacks WHERE id = :attackId")
    suspend fun deleteAttackById(attackId: Long)

    @Query("DELETE FROM combat_resources WHERE characterOwnerId = :characterId")
    suspend fun deleteCombatResourcesForCharacter(characterId: Long)

    @Query("DELETE FROM combat_resources WHERE id = :resourceId")
    suspend fun deleteCombatResourceById(resourceId: Long)

    @Query("SELECT * FROM combat_resources WHERE id = :resourceId LIMIT 1")
    suspend fun getCombatResourceById(resourceId: Long): CombatResourceEntity?

    @Query("DELETE FROM inventory_items WHERE characterOwnerId = :characterId")
    suspend fun deleteInventoryItemsForCharacter(characterId: Long)

    @Query("SELECT * FROM inventory_items WHERE characterOwnerId = :characterId")
    suspend fun getInventoryItemsForCharacter(characterId: Long): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE id = :itemId LIMIT 1")
    suspend fun getInventoryItemById(itemId: Long): InventoryItemEntity?

    @Query("DELETE FROM inventory_items WHERE id = :itemId")
    suspend fun deleteInventoryItemById(itemId: Long)

    @Query("UPDATE inventory_items SET isEquipped = :isEquipped WHERE id = :itemId")
    suspend fun updateInventoryItemEquipped(itemId: Long, isEquipped: Boolean)

    @Query(
        """
        UPDATE characters
        SET copperPieces = :copperPieces,
            silverPieces = :silverPieces,
            goldPieces = :goldPieces,
            updatedAt = :updatedAt
        WHERE id = :characterId
        """
    )
    suspend fun updateCurrency(
        characterId: Long,
        copperPieces: Int,
        silverPieces: Int,
        goldPieces: Int,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE characters
        SET armorClass = :armorClass,
            updatedAt = :updatedAt
        WHERE id = :characterId
        """
    )
    suspend fun updateArmorClass(characterId: Long, armorClass: Int, updatedAt: Long)

    @Query("UPDATE characters SET updatedAt = :updatedAt WHERE id = :characterId")
    suspend fun updateCharacterUpdatedAt(characterId: Long, updatedAt: Long)

    @Query(
        """
        UPDATE characters
        SET armorClass = :armorClass,
            baseArmorClass = :baseArmorClass,
            armorClassMode = :armorClassMode,
            updatedAt = :updatedAt
        WHERE id = :characterId
        """
    )
    suspend fun updateArmorClassSettings(
        characterId: Long,
        armorClass: Int,
        baseArmorClass: Int,
        armorClassMode: ArmorClassMode,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE combat_resources
        SET currentUses = :currentUses
        WHERE id = :resourceId
        """
    )
    suspend fun updateCombatResourceCurrentUses(resourceId: Long, currentUses: Int)

    @Query("DELETE FROM spells WHERE characterOwnerId = :characterId")
    suspend fun deleteSpellsForCharacter(characterId: Long)

    @Query("DELETE FROM spells WHERE id = :spellId")
    suspend fun deleteSpellById(spellId: Long)

    @Query(
        """
        UPDATE characters
        SET spellSlotMaximums = :spellSlotMaximums,
            spellSlotRemaining = :spellSlotRemaining,
            spellSlotsRestoreOnShortRest = :restoresOnShortRest,
            spellSlotsRestoreOnLongRest = :restoresOnLongRest,
            updatedAt = :updatedAt
        WHERE id = :characterId
        """
    )
    suspend fun updateSpellSlots(
        characterId: Long,
        spellSlotMaximums: String,
        spellSlotRemaining: String,
        restoresOnShortRest: Boolean,
        restoresOnLongRest: Boolean,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE characters
        SET spellSlotRemaining = :spellSlotRemaining,
            updatedAt = :updatedAt
        WHERE id = :characterId
        """
    )
    suspend fun updateSpellSlotRemaining(characterId: Long, spellSlotRemaining: String, updatedAt: Long)

    @Query(
        """
        UPDATE characters
        SET spellcastingAbility = :ability,
            updatedAt = :updatedAt
        WHERE id = :characterId
        """
    )
    suspend fun updateSpellcastingAbility(characterId: Long, ability: SpellcastingAbility, updatedAt: Long)

    @Query("DELETE FROM features WHERE characterOwnerId = :characterId")
    suspend fun deleteFeaturesForCharacter(characterId: Long)

    @Query("DELETE FROM features WHERE id = :featureId")
    suspend fun deleteFeatureById(featureId: Long)

    @Query("DELETE FROM notes WHERE characterOwnerId = :characterId")
    suspend fun deleteNotesForCharacter(characterId: Long)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun deleteCharacter(characterId: Long)

    @Transaction
    suspend fun upsertInventoryItemForCharacter(characterId: Long, item: InventoryItemEntity, updatedAt: Long): Long {
        val savedId = upsertInventoryItem(item.copy(characterOwnerId = characterId))
        refreshInventoryDerivedCharacterState(characterId, updatedAt)
        return savedId
    }

    @Transaction
    suspend fun deleteInventoryItemForCharacter(characterId: Long, itemId: Long, updatedAt: Long) {
        deleteInventoryItemById(itemId)
        refreshInventoryDerivedCharacterState(characterId, updatedAt)
    }

    @Transaction
    suspend fun toggleInventoryItemEquippedForCharacter(characterId: Long, itemId: Long, updatedAt: Long) {
        val targetItem = getInventoryItemById(itemId) ?: return
        if (targetItem.characterOwnerId != characterId) return

        val shouldEquip = !targetItem.isEquipped
        val targetArmorType = targetItem.armorType
        val items = getInventoryItemsForCharacter(characterId)

        items.forEach { currentItem ->
            val nextEquipped = when {
                currentItem.id == itemId -> shouldEquip
                !shouldEquip -> currentItem.isEquipped
                targetArmorType == InventoryArmorType.SHIELD && currentItem.armorType == InventoryArmorType.SHIELD ->
                    false
                targetArmorType != null && targetArmorType != InventoryArmorType.SHIELD &&
                    currentItem.armorType != null && currentItem.armorType != InventoryArmorType.SHIELD ->
                    false
                else -> currentItem.isEquipped
            }

            if (nextEquipped != currentItem.isEquipped) {
                updateInventoryItemEquipped(currentItem.id, nextEquipped)
            }
        }

        refreshInventoryDerivedCharacterState(characterId, updatedAt)
    }

    @Transaction
    suspend fun refreshInventoryDerivedCharacterState(characterId: Long, updatedAt: Long) {
        val character = getCharacterEntity(characterId) ?: return
        if (character.armorClassMode == ArmorClassMode.AUTOMATIC) {
            val armorClass = calculateArmorClass(
                baseArmorClass = character.baseArmorClass,
                dexterityScore = character.dexterity,
                inventoryItems = getInventoryItemsForCharacter(characterId)
            )
            updateArmorClass(characterId, armorClass, updatedAt)
        } else {
            updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    @Transaction
    suspend fun upsertSpellForCharacter(characterId: Long, spell: SpellEntity, updatedAt: Long): Long {
        val savedId = upsertSpellEntity(spell.copy(characterOwnerId = characterId))
        updateCharacterUpdatedAt(characterId, updatedAt)
        return savedId
    }

    @Transaction
    suspend fun deleteSpellForCharacter(characterId: Long, spellId: Long, updatedAt: Long) {
        deleteSpellById(spellId)
        updateCharacterUpdatedAt(characterId, updatedAt)
    }

    @Transaction
    suspend fun updateArmorClassSettingsForCharacter(
        characterId: Long,
        baseArmorClass: Int,
        armorClassMode: ArmorClassMode,
        manualArmorClass: Int?,
        updatedAt: Long
    ) {
        val character = getCharacterEntity(characterId) ?: return
        val sanitizedBaseArmorClass = baseArmorClass.coerceAtLeast(1)
        val sanitizedArmorClass = when (armorClassMode) {
            ArmorClassMode.AUTOMATIC -> calculateArmorClass(
                baseArmorClass = sanitizedBaseArmorClass,
                dexterityScore = character.dexterity,
                inventoryItems = getInventoryItemsForCharacter(characterId)
            )
            ArmorClassMode.MANUAL -> manualArmorClass?.coerceAtLeast(1) ?: character.armorClass
        }
        updateArmorClassSettings(
            characterId = characterId,
            armorClass = sanitizedArmorClass,
            baseArmorClass = sanitizedBaseArmorClass,
            armorClassMode = armorClassMode,
            updatedAt = updatedAt
        )
    }

    @Transaction
    suspend fun upsertAttackForCharacter(characterId: Long, attack: AttackEntity, updatedAt: Long): Long {
        val savedId = upsertAttackEntity(attack.copy(characterOwnerId = characterId))
        updateCharacterUpdatedAt(characterId, updatedAt)
        return savedId
    }

    @Transaction
    suspend fun deleteAttackForCharacter(characterId: Long, attackId: Long, updatedAt: Long) {
        deleteAttackById(attackId)
        updateCharacterUpdatedAt(characterId, updatedAt)
    }

    @Transaction
    suspend fun upsertCombatResourceForCharacter(
        characterId: Long,
        resource: CombatResourceEntity,
        updatedAt: Long
    ): Long {
        val savedId = upsertCombatResourceEntity(resource.copy(characterOwnerId = characterId))
        updateCharacterUpdatedAt(characterId, updatedAt)
        return savedId
    }

    @Transaction
    suspend fun deleteCombatResourceForCharacter(characterId: Long, resourceId: Long, updatedAt: Long) {
        deleteCombatResourceById(resourceId)
        updateCharacterUpdatedAt(characterId, updatedAt)
    }

    @Transaction
    suspend fun updateCombatResourceUsesForCharacter(characterId: Long, resourceId: Long, delta: Int, updatedAt: Long) {
        val current = getCombatResourceById(resourceId) ?: return
        if (current.characterOwnerId != characterId) return
        val nextUses = (current.currentUses + delta).coerceIn(0, current.maximumUses.coerceAtLeast(0))
        updateCombatResourceCurrentUses(resourceId, nextUses)
        updateCharacterUpdatedAt(characterId, updatedAt)
    }

    @Transaction
    suspend fun upsertFeatureForCharacter(characterId: Long, feature: FeatureEntity, updatedAt: Long): Long {
        val savedId = upsertFeatureEntity(feature.copy(characterOwnerId = characterId))
        updateCharacterUpdatedAt(characterId, updatedAt)
        return savedId
    }

    @Transaction
    suspend fun deleteFeatureForCharacter(characterId: Long, featureId: Long, updatedAt: Long) {
        deleteFeatureById(featureId)
        updateCharacterUpdatedAt(characterId, updatedAt)
    }

    @Transaction
    suspend fun upsertNoteForCharacter(characterId: Long, note: NoteEntity, updatedAt: Long): Long {
        val savedId = upsertNoteEntity(note.copy(characterOwnerId = characterId))
        updateCharacterUpdatedAt(characterId, updatedAt)
        return savedId
    }

    @Transaction
    suspend fun deleteNoteForCharacter(characterId: Long, noteId: Long, updatedAt: Long) {
        deleteNoteById(noteId)
        updateCharacterUpdatedAt(characterId, updatedAt)
    }

    @Transaction
    suspend fun upsertSkillForCharacter(characterId: Long, skill: SkillEntity, updatedAt: Long) {
        val existing = getSkillForCharacter(characterId, skill.name)
        upsertSkillEntity(
            skill.copy(
                id = existing?.id ?: skill.id,
                characterOwnerId = characterId
            )
        )
        updateCharacterUpdatedAt(characterId, updatedAt)
    }
}

private fun calculateArmorClass(
    baseArmorClass: Int,
    dexterityScore: Int,
    inventoryItems: List<InventoryItemEntity>
): Int {
    val dexterityModifier = abilityModifier(dexterityScore)
    val equippedArmor = inventoryItems.firstOrNull {
        it.isEquipped && it.armorType != null && it.armorType != InventoryArmorType.SHIELD
    }
    val equippedShield = inventoryItems.firstOrNull {
        it.isEquipped && it.armorType == InventoryArmorType.SHIELD
    }

    val effectiveArmorClass = if (equippedArmor != null) {
        val armorClass = equippedArmor.armorClass ?: baseArmorClass
        armorClass + equippedArmor.appliedDexterityModifier(dexterityModifier)
    } else {
        baseArmorClass + dexterityModifier
    }

    val shieldBonus = equippedShield?.armorClass ?: 0
    return (effectiveArmorClass + shieldBonus).coerceAtLeast(1)
}

private fun InventoryItemEntity.appliedDexterityModifier(dexterityModifier: Int): Int {
    if (appliesDexterityBonus != true) return 0
    return maxDexterityBonus?.let { dexterityModifier.coerceAtMost(it) } ?: dexterityModifier
}

private fun abilityModifier(score: Int): Int = Math.floorDiv(score - 10, 2)
