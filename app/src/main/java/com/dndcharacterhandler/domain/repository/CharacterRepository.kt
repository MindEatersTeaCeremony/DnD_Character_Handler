package com.dndcharacterhandler.domain.repository

import com.dndcharacterhandler.domain.model.CharacterBundle
import kotlinx.coroutines.flow.Flow

interface CharacterRepository {
    fun observeCharacters(): Flow<List<CharacterBundle>>
    fun observeCharacter(characterId: Long): Flow<CharacterBundle?>
    suspend fun createCharacter(character: CharacterBundle): Long
    suspend fun upsertCharacter(character: CharacterBundle): Long
    suspend fun deleteCharacter(characterId: Long)
}

