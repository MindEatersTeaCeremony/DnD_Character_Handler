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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: CharacterEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkills(skills: List<SkillEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttacks(attacks: List<AttackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombatResources(resources: List<CombatResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpells(spells: List<SpellEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatures(features: List<FeatureEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Query("DELETE FROM skills WHERE characterOwnerId = :characterId")
    suspend fun deleteSkillsForCharacter(characterId: Long)

    @Query("DELETE FROM attacks WHERE characterOwnerId = :characterId")
    suspend fun deleteAttacksForCharacter(characterId: Long)

    @Query("DELETE FROM combat_resources WHERE characterOwnerId = :characterId")
    suspend fun deleteCombatResourcesForCharacter(characterId: Long)

    @Query("DELETE FROM inventory_items WHERE characterOwnerId = :characterId")
    suspend fun deleteInventoryItemsForCharacter(characterId: Long)

    @Query("DELETE FROM spells WHERE characterOwnerId = :characterId")
    suspend fun deleteSpellsForCharacter(characterId: Long)

    @Query("DELETE FROM features WHERE characterOwnerId = :characterId")
    suspend fun deleteFeaturesForCharacter(characterId: Long)

    @Query("DELETE FROM notes WHERE characterOwnerId = :characterId")
    suspend fun deleteNotesForCharacter(characterId: Long)

    @Query("DELETE FROM characters WHERE id = :characterId")
    suspend fun deleteCharacter(characterId: Long)
}

