package com.dndcharacterhandler.data.repository

import android.content.Context
import com.dndcharacterhandler.data.local.dao.CharacterDao
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.repository.CharacterFileRepository

class CharacterFileRepositoryImpl(
    private val context: Context,
    private val characterDao: CharacterDao
) : CharacterFileRepository {
    override suspend fun exportCharacter(characterId: Long): Result<String> {
        val exists = characterDao.getCharacter(characterId) != null
        return if (exists) {
            Result.success("export://character/$characterId")
        } else {
            Result.failure(IllegalArgumentException("Character not found"))
        }
    }

    override suspend fun importCharacter(serializedCharacter: String): Result<Unit> {
        return Result.failure(
            UnsupportedOperationException(
                "Import wiring is scaffolded but the file parser will be implemented later."
            )
        )
    }
}

