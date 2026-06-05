package com.dndcharacterhandler.domain.repository

interface CharacterFileRepository {
    suspend fun exportCharacter(characterId: Long, destinationUri: String): Result<String>
    suspend fun importCharacter(sourceUri: String): Result<Long>
}
