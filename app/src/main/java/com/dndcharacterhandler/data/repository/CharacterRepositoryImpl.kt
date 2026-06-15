package com.dndcharacterhandler.data.repository

import com.dndcharacterhandler.data.local.dao.CharacterDao
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CharacterRepositoryImpl(
    private val characterDao: CharacterDao
) : CharacterRepository {
    private val writeMutex = Mutex()

    override fun observeCharacters(): Flow<List<CharacterBundle>> =
        characterDao.observeCharacters().map { characters -> characters.map { it.toDomain() } }

    override fun observeCharacter(characterId: Long): Flow<CharacterBundle?> =
        characterDao.observeCharacter(characterId).map { it?.toDomain() }

    override suspend fun createCharacter(character: CharacterBundle): Long {
        return upsertCharacter(character.copy(character = character.character.copy(id = 0)))
    }

    override suspend fun upsertCharacter(character: CharacterBundle): Long =
        writeMutex.withLock {
            val characterEntity = character.character.toEntity()
            characterDao.replaceCharacterBundle(
                character = characterEntity,
                skills = character.skills.map { it.toEntity(characterEntity.id) },
                attacks = character.attacks.map { it.toEntity(characterEntity.id) },
                combatResources = character.combatResources.map { it.toEntity(characterEntity.id) },
                inventoryItems = character.inventoryItems.map { it.toEntity(characterEntity.id) },
                spells = character.spells.map { it.toEntity(characterEntity.id) },
                features = character.features.map { it.toEntity(characterEntity.id) },
                notes = character.notes.map { it.toEntity(characterEntity.id) }
            )
        }

    override suspend fun deleteCharacter(characterId: Long) {
        writeMutex.withLock {
            characterDao.deleteCharacter(characterId)
        }
    }
}
