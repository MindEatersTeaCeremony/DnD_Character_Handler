package com.dndcharacterhandler.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    @Update
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
}
