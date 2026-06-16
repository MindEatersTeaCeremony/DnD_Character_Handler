package com.dndcharacterhandler.data.repository

import androidx.room.withTransaction
import com.dndcharacterhandler.data.local.AppDatabase
import com.dndcharacterhandler.data.local.dao.CharacterDao
import com.dndcharacterhandler.data.local.entity.InventoryItemEntity
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.Attack
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.domain.model.Feature
import com.dndcharacterhandler.domain.model.InventoryArmorType
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
    private val database: AppDatabase,
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
            replaceCharacterBundle(
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
            upsertInventoryItemForCharacter(
                characterId = characterId,
                item = item.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteInventoryItem(characterId: Long, itemId: Long) {
        writeMutex.withLock {
            deleteInventoryItemForCharacter(
                characterId = characterId,
                itemId = itemId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun toggleInventoryItemEquipped(characterId: Long, itemId: Long) {
        writeMutex.withLock {
            toggleInventoryItemEquippedForCharacter(
                characterId = characterId,
                itemId = itemId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertSpell(characterId: Long, spell: Spell): Long =
        writeMutex.withLock {
            upsertSpellForCharacter(
                characterId = characterId,
                spell = spell.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteSpell(characterId: Long, spellId: Long) {
        writeMutex.withLock {
            deleteSpellForCharacter(
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
            updateArmorClassSettingsForCharacter(
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
            upsertAttackForCharacter(
                characterId = characterId,
                attack = attack.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteAttack(characterId: Long, attackId: Long) {
        writeMutex.withLock {
            deleteAttackForCharacter(
                characterId = characterId,
                attackId = attackId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertCombatResource(characterId: Long, resource: CombatResource): Long =
        writeMutex.withLock {
            upsertCombatResourceForCharacter(
                characterId = characterId,
                resource = resource.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteCombatResource(characterId: Long, resourceId: Long) {
        writeMutex.withLock {
            deleteCombatResourceForCharacter(
                characterId = characterId,
                resourceId = resourceId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun updateCombatResourceUses(characterId: Long, resourceId: Long, delta: Int) {
        writeMutex.withLock {
            updateCombatResourceUsesForCharacter(
                characterId = characterId,
                resourceId = resourceId,
                delta = delta,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertFeature(characterId: Long, feature: Feature): Long =
        writeMutex.withLock {
            upsertFeatureForCharacter(
                characterId = characterId,
                feature = feature.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteFeature(characterId: Long, featureId: Long) {
        writeMutex.withLock {
            deleteFeatureForCharacter(
                characterId = characterId,
                featureId = featureId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertNote(characterId: Long, note: Note): Long =
        writeMutex.withLock {
            upsertNoteForCharacter(
                characterId = characterId,
                note = note.toEntity(characterId),
                updatedAt = System.currentTimeMillis()
            )
        }

    override suspend fun deleteNote(characterId: Long, noteId: Long) {
        writeMutex.withLock {
            deleteNoteForCharacter(
                characterId = characterId,
                noteId = noteId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun upsertSkill(characterId: Long, skill: Skill) {
        writeMutex.withLock {
            upsertSkillForCharacter(
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

    private suspend fun replaceCharacterBundle(
        character: com.dndcharacterhandler.data.local.entity.CharacterEntity,
        skills: List<com.dndcharacterhandler.data.local.entity.SkillEntity>,
        attacks: List<com.dndcharacterhandler.data.local.entity.AttackEntity>,
        combatResources: List<com.dndcharacterhandler.data.local.entity.CombatResourceEntity>,
        inventoryItems: List<InventoryItemEntity>,
        spells: List<com.dndcharacterhandler.data.local.entity.SpellEntity>,
        features: List<com.dndcharacterhandler.data.local.entity.FeatureEntity>,
        notes: List<com.dndcharacterhandler.data.local.entity.NoteEntity>
    ): Long = database.withTransaction {
        val savedId = characterDao.insertCharacter(character)
        val characterId = if (character.id == 0L) savedId else character.id

        characterDao.deleteSkillsForCharacter(characterId)
        characterDao.deleteAttacksForCharacter(characterId)
        characterDao.deleteCombatResourcesForCharacter(characterId)
        characterDao.deleteInventoryItemsForCharacter(characterId)
        characterDao.deleteSpellsForCharacter(characterId)
        characterDao.deleteFeaturesForCharacter(characterId)
        characterDao.deleteNotesForCharacter(characterId)

        characterDao.insertSkills(skills.map { it.copy(characterOwnerId = characterId) })
        characterDao.insertAttacks(attacks.map { it.copy(characterOwnerId = characterId) })
        characterDao.insertCombatResources(combatResources.map { it.copy(characterOwnerId = characterId) })
        characterDao.insertInventoryItems(inventoryItems.map { it.copy(characterOwnerId = characterId) })
        characterDao.insertSpells(spells.map { it.copy(characterOwnerId = characterId) })
        characterDao.insertFeatures(features.map { it.copy(characterOwnerId = characterId) })
        characterDao.insertNotes(notes.map { it.copy(characterOwnerId = characterId) })

        characterId
    }

    private suspend fun upsertInventoryItemForCharacter(characterId: Long, item: InventoryItemEntity, updatedAt: Long): Long =
        database.withTransaction {
            val savedId = characterDao.upsertInventoryItem(item.copy(characterOwnerId = characterId))
            refreshInventoryDerivedCharacterState(characterId, updatedAt)
            savedId
        }

    private suspend fun deleteInventoryItemForCharacter(characterId: Long, itemId: Long, updatedAt: Long) {
        database.withTransaction {
            characterDao.deleteInventoryItemById(itemId)
            refreshInventoryDerivedCharacterState(characterId, updatedAt)
        }
    }

    private suspend fun toggleInventoryItemEquippedForCharacter(characterId: Long, itemId: Long, updatedAt: Long) {
        database.withTransaction {
            val targetItem = characterDao.getInventoryItemById(itemId) ?: return@withTransaction
            if (targetItem.characterOwnerId != characterId) return@withTransaction

            val shouldEquip = !targetItem.isEquipped
            val targetArmorType = targetItem.armorType
            val items = characterDao.getInventoryItemsForCharacter(characterId)

            items.forEach { currentItem ->
                val nextEquipped = when {
                    currentItem.id == itemId -> shouldEquip
                    !shouldEquip -> currentItem.isEquipped
                    targetArmorType == InventoryArmorType.SHIELD && currentItem.armorType == InventoryArmorType.SHIELD -> false
                    targetArmorType != null && targetArmorType != InventoryArmorType.SHIELD &&
                        currentItem.armorType != null && currentItem.armorType != InventoryArmorType.SHIELD -> false
                    else -> currentItem.isEquipped
                }

                if (nextEquipped != currentItem.isEquipped) {
                    characterDao.updateInventoryItemEquipped(currentItem.id, nextEquipped)
                }
            }

            refreshInventoryDerivedCharacterState(characterId, updatedAt)
        }
    }

    private suspend fun refreshInventoryDerivedCharacterState(characterId: Long, updatedAt: Long) {
        val character = characterDao.getCharacterEntity(characterId) ?: return
        if (character.armorClassMode == ArmorClassMode.AUTOMATIC) {
            val armorClass = calculateArmorClassForEntities(
                baseArmorClass = character.baseArmorClass,
                dexterityScore = character.dexterity,
                inventoryItems = characterDao.getInventoryItemsForCharacter(characterId)
            )
            characterDao.updateArmorClass(characterId, armorClass, updatedAt)
        } else {
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun upsertSpellForCharacter(
        characterId: Long,
        spell: com.dndcharacterhandler.data.local.entity.SpellEntity,
        updatedAt: Long
    ): Long = database.withTransaction {
        val savedId = characterDao.upsertSpellEntity(spell.copy(characterOwnerId = characterId))
        characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        savedId
    }

    private suspend fun deleteSpellForCharacter(characterId: Long, spellId: Long, updatedAt: Long) {
        database.withTransaction {
            characterDao.deleteSpellById(spellId)
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun updateArmorClassSettingsForCharacter(
        characterId: Long,
        baseArmorClass: Int,
        armorClassMode: ArmorClassMode,
        manualArmorClass: Int?,
        updatedAt: Long
    ) {
        database.withTransaction {
            val character = characterDao.getCharacterEntity(characterId) ?: return@withTransaction
            val sanitizedBaseArmorClass = baseArmorClass.coerceAtLeast(1)
            val sanitizedArmorClass = when (armorClassMode) {
                ArmorClassMode.AUTOMATIC -> calculateArmorClassForEntities(
                    baseArmorClass = sanitizedBaseArmorClass,
                    dexterityScore = character.dexterity,
                    inventoryItems = characterDao.getInventoryItemsForCharacter(characterId)
                )
                ArmorClassMode.MANUAL -> manualArmorClass?.coerceAtLeast(1) ?: character.armorClass
            }
            characterDao.updateArmorClassSettings(
                characterId = characterId,
                armorClass = sanitizedArmorClass,
                baseArmorClass = sanitizedBaseArmorClass,
                armorClassMode = armorClassMode,
                updatedAt = updatedAt
            )
        }
    }

    private suspend fun upsertAttackForCharacter(
        characterId: Long,
        attack: com.dndcharacterhandler.data.local.entity.AttackEntity,
        updatedAt: Long
    ): Long = database.withTransaction {
        val savedId = characterDao.upsertAttackEntity(attack.copy(characterOwnerId = characterId))
        characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        savedId
    }

    private suspend fun deleteAttackForCharacter(characterId: Long, attackId: Long, updatedAt: Long) {
        database.withTransaction {
            characterDao.deleteAttackById(attackId)
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun upsertCombatResourceForCharacter(
        characterId: Long,
        resource: com.dndcharacterhandler.data.local.entity.CombatResourceEntity,
        updatedAt: Long
    ): Long = database.withTransaction {
        val savedId = characterDao.upsertCombatResourceEntity(resource.copy(characterOwnerId = characterId))
        characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        savedId
    }

    private suspend fun deleteCombatResourceForCharacter(characterId: Long, resourceId: Long, updatedAt: Long) {
        database.withTransaction {
            characterDao.deleteCombatResourceById(resourceId)
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun updateCombatResourceUsesForCharacter(characterId: Long, resourceId: Long, delta: Int, updatedAt: Long) {
        database.withTransaction {
            val current = characterDao.getCombatResourceById(resourceId) ?: return@withTransaction
            if (current.characterOwnerId != characterId) return@withTransaction
            val nextUses = (current.currentUses + delta).coerceIn(0, current.maximumUses.coerceAtLeast(0))
            characterDao.updateCombatResourceCurrentUses(resourceId, nextUses)
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun upsertFeatureForCharacter(
        characterId: Long,
        feature: com.dndcharacterhandler.data.local.entity.FeatureEntity,
        updatedAt: Long
    ): Long = database.withTransaction {
        val savedId = characterDao.upsertFeatureEntity(feature.copy(characterOwnerId = characterId))
        characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        savedId
    }

    private suspend fun deleteFeatureForCharacter(characterId: Long, featureId: Long, updatedAt: Long) {
        database.withTransaction {
            characterDao.deleteFeatureById(featureId)
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun upsertNoteForCharacter(
        characterId: Long,
        note: com.dndcharacterhandler.data.local.entity.NoteEntity,
        updatedAt: Long
    ): Long = database.withTransaction {
        val savedId = characterDao.upsertNoteEntity(note.copy(characterOwnerId = characterId))
        characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        savedId
    }

    private suspend fun deleteNoteForCharacter(characterId: Long, noteId: Long, updatedAt: Long) {
        database.withTransaction {
            characterDao.deleteNoteById(noteId)
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }

    private suspend fun upsertSkillForCharacter(
        characterId: Long,
        skill: com.dndcharacterhandler.data.local.entity.SkillEntity,
        updatedAt: Long
    ) {
        database.withTransaction {
            val existing = characterDao.getSkillForCharacter(characterId, skill.name)
            characterDao.upsertSkillEntity(
                skill.copy(
                    id = existing?.id ?: skill.id,
                    characterOwnerId = characterId
                )
            )
            characterDao.updateCharacterUpdatedAt(characterId, updatedAt)
        }
    }
}

private fun calculateArmorClassForEntities(
    baseArmorClass: Int,
    dexterityScore: Int,
    inventoryItems: List<InventoryItemEntity>
): Int {
    val dexterityModifier = Math.floorDiv(dexterityScore - 10, 2)
    val equippedArmor = inventoryItems.firstOrNull {
        it.isEquipped && it.armorType != null && it.armorType != InventoryArmorType.SHIELD
    }
    val equippedShield = inventoryItems.firstOrNull {
        it.isEquipped && it.armorType == InventoryArmorType.SHIELD
    }

    val effectiveArmorClass = if (equippedArmor != null) {
        val armorClass = equippedArmor.armorClass ?: baseArmorClass
        armorClass + equippedArmor.appliedDexterityModifier(dexterityModifier)
    } else {
        baseArmorClass + dexterityModifier
    }

    val shieldBonus = equippedShield?.armorClass ?: 0
    return (effectiveArmorClass + shieldBonus).coerceAtLeast(1)
}

private fun InventoryItemEntity.appliedDexterityModifier(dexterityModifier: Int): Int {
    if (appliesDexterityBonus != true) return 0
    return maxDexterityBonus?.let { dexterityModifier.coerceAtMost(it) } ?: dexterityModifier
}
