package com.dndcharacterhandler.data.repository

import com.dndcharacterhandler.data.local.dao.CharacterDao
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CharacterRepositoryImpl(
    private val characterDao: CharacterDao
) : CharacterRepository {
    override fun observeCharacters(): Flow<List<CharacterBundle>> =
        characterDao.observeCharacters().map { characters -> characters.map { it.toDomain() } }

    override fun observeCharacter(characterId: Long): Flow<CharacterBundle?> =
        characterDao.observeCharacter(characterId).map { it?.toDomain() }

    override suspend fun createCharacter(character: CharacterBundle): Long {
        return upsertCharacter(character.copy(character = character.character.copy(id = 0)))
    }

    override suspend fun upsertCharacter(character: CharacterBundle): Long {
        val savedId = characterDao.insertCharacter(character.character.toEntity())
        val characterId = if (character.character.id == 0L) savedId else character.character.id

        characterDao.deleteSkillsForCharacter(characterId)
        characterDao.deleteAttacksForCharacter(characterId)
        characterDao.deleteCombatResourcesForCharacter(characterId)
        characterDao.deleteInventoryItemsForCharacter(characterId)
        characterDao.deleteSpellsForCharacter(characterId)
        characterDao.deleteFeaturesForCharacter(characterId)
        characterDao.deleteNotesForCharacter(characterId)

        characterDao.insertSkills(character.skills.map { it.toEntity(characterId) })
        characterDao.insertAttacks(character.attacks.map { it.toEntity(characterId) })
        characterDao.insertCombatResources(character.combatResources.map { it.toEntity(characterId) })
        characterDao.insertInventoryItems(character.inventoryItems.map { it.toEntity(characterId) })
        characterDao.insertSpells(character.spells.map { it.toEntity(characterId) })
        characterDao.insertFeatures(character.features.map { it.toEntity(characterId) })
        characterDao.insertNotes(character.notes.map { it.toEntity(characterId) })
        return characterId
    }

    override suspend fun deleteCharacter(characterId: Long) {
        characterDao.deleteCharacter(characterId)
    }
}
