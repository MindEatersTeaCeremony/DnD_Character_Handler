package com.dndcharacterhandler.data.repository

import com.dndcharacterhandler.data.local.dao.CharacterDao
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.Attack
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.domain.model.Feature
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.domain.model.Note
import com.dndcharacterhandler.domain.model.Skill
import com.dndcharacterhandler.domain.model.Spell
import com.dndcharacterhandler.domain.model.SpellcastingAbility
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

    override suspend fun updateCharacterDetails(character: Character) {
        writeMutex.withLock {
            characterDao.updateCharacterEntity(character.copy(updatedAt = System.currentTimeMillis()).toEntity())
        }
    }

    override suspend fun updateCurrency(characterId: Long, copperPieces: Int, silverPieces: Int, goldPieces: Int) {
        writeMutex.withLock {
            characterDao.updateCurrency(
                characterId = characterId,
                copperPieces = copperPieces.coerceAtLeast(0),
                silverPieces = silverPieces.coerceAtLeast(0),
                goldPieces = goldPieces.coerceAtLeast(0),
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertInventoryItem(characterId: Long, item: InventoryItem): Long =
        writeMutex.withLock {
            characterDao.upsertInventoryItemForCharacter(
                characterId = characterId,
                item = item.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteInventoryItem(characterId: Long, itemId: Long) {
        writeMutex.withLock {
            characterDao.deleteInventoryItemForCharacter(
                characterId = characterId,
                itemId = itemId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun toggleInventoryItemEquipped(characterId: Long, itemId: Long) {
        writeMutex.withLock {
            characterDao.toggleInventoryItemEquippedForCharacter(
                characterId = characterId,
                itemId = itemId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertSpell(characterId: Long, spell: Spell): Long =
        writeMutex.withLock {
            characterDao.upsertSpellForCharacter(
                characterId = characterId,
                spell = spell.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteSpell(characterId: Long, spellId: Long) {
        writeMutex.withLock {
            characterDao.deleteSpellForCharacter(
                characterId = characterId,
                spellId = spellId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateSpellSlots(
        characterId: Long,
        spellSlotMaximums: String,
        spellSlotRemaining: String,
        restoresOnShortRest: Boolean,
        restoresOnLongRest: Boolean
    ) {
        writeMutex.withLock {
            characterDao.updateSpellSlots(
                characterId = characterId,
                spellSlotMaximums = spellSlotMaximums,
                spellSlotRemaining = spellSlotRemaining,
                restoresOnShortRest = restoresOnShortRest,
                restoresOnLongRest = restoresOnLongRest,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateSpellSlotRemaining(characterId: Long, spellSlotRemaining: String) {
        writeMutex.withLock {
            characterDao.updateSpellSlotRemaining(
                characterId = characterId,
                spellSlotRemaining = spellSlotRemaining,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateSpellcastingAbility(characterId: Long, ability: SpellcastingAbility) {
        writeMutex.withLock {
            characterDao.updateSpellcastingAbility(
                characterId = characterId,
                ability = ability,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateArmorClassSettings(
        characterId: Long,
        baseArmorClass: Int,
        armorClassMode: ArmorClassMode,
        manualArmorClass: Int?
    ) {
        writeMutex.withLock {
            characterDao.updateArmorClassSettingsForCharacter(
                characterId = characterId,
                baseArmorClass = baseArmorClass,
                armorClassMode = armorClassMode,
                manualArmorClass = manualArmorClass,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertAttack(characterId: Long, attack: Attack): Long =
        writeMutex.withLock {
            characterDao.upsertAttackForCharacter(
                characterId = characterId,
                attack = attack.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteAttack(characterId: Long, attackId: Long) {
        writeMutex.withLock {
            characterDao.deleteAttackForCharacter(
                characterId = characterId,
                attackId = attackId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertCombatResource(characterId: Long, resource: CombatResource): Long =
        writeMutex.withLock {
            characterDao.upsertCombatResourceForCharacter(
                characterId = characterId,
                resource = resource.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteCombatResource(characterId: Long, resourceId: Long) {
        writeMutex.withLock {
            characterDao.deleteCombatResourceForCharacter(
                characterId = characterId,
                resourceId = resourceId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateCombatResourceUses(characterId: Long, resourceId: Long, delta: Int) {
        writeMutex.withLock {
            characterDao.updateCombatResourceUsesForCharacter(
                characterId = characterId,
                resourceId = resourceId,
                delta = delta,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertFeature(characterId: Long, feature: Feature): Long =
        writeMutex.withLock {
            characterDao.upsertFeatureForCharacter(
                characterId = characterId,
                feature = feature.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteFeature(characterId: Long, featureId: Long) {
        writeMutex.withLock {
            characterDao.deleteFeatureForCharacter(
                characterId = characterId,
                featureId = featureId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertNote(characterId: Long, note: Note): Long =
        writeMutex.withLock {
            characterDao.upsertNoteForCharacter(
                characterId = characterId,
                note = note.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteNote(characterId: Long, noteId: Long) {
        writeMutex.withLock {
            characterDao.deleteNoteForCharacter(
                characterId = characterId,
                noteId = noteId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertSkill(characterId: Long, skill: Skill) {
        writeMutex.withLock {
            characterDao.upsertSkillForCharacter(
                characterId = characterId,
                skill = skill.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun deleteCharacter(characterId: Long) {
        writeMutex.withLock {
            characterDao.deleteCharacter(characterId)
        }
    }
}
