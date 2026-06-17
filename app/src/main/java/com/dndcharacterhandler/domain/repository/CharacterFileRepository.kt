package com.dndcharacterhandler.domain.repository

interface CharacterFileRepository {
    suspend fun exportCharacter(characterId: Long, destinationUri: String): Result<String>
    suspend fun importCharacter(sourceUri: String): Result<Long>

    /** Removes asset files left behind by characters that no longer exist. Best effort. */
    suspend fun purgeOrphanedAssets()
}
