package com.dndcharacterhandler.domain.repository

interface CharacterFileRepository {
    suspend fun exportCharacter(characterId: Long): Result<String>
    suspend fun importCharacter(serializedCharacter: String): Result<Unit>
}

