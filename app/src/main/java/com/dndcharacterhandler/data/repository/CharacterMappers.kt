package com.dndcharacterhandler.data.repository

import com.dndcharacterhandler.data.local.entity.AttackEntity
import com.dndcharacterhandler.data.local.entity.CharacterEntity
import com.dndcharacterhandler.data.local.entity.CharacterWithDetails
import com.dndcharacterhandler.data.local.entity.CombatResourceEntity
import com.dndcharacterhandler.data.local.entity.FeatureEntity
import com.dndcharacterhandler.data.local.entity.InventoryItemEntity
import com.dndcharacterhandler.data.local.entity.NoteEntity
import com.dndcharacterhandler.data.local.entity.SkillEntity
import com.dndcharacterhandler.data.local.entity.SpellEntity
import com.dndcharacterhandler.domain.model.Attack
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.domain.model.Feature
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.domain.model.Note
import com.dndcharacterhandler.domain.model.Skill
import com.dndcharacterhandler.domain.model.Spell

fun CharacterWithDetails.toDomain(): CharacterBundle =
    CharacterBundle(
        character = character.toDomain(),
        skills = skills.map { Skill(it.id, it.name, it.isProficient) },
        attacks = attacks.map { Attack(it.id, it.name, it.icon, it.range, it.attackBonusOrSaveDc, it.damage, it.damageType) },
        combatResources = combatResources.map { CombatResource(it.id, it.name, it.currentUses, it.maximumUses) },
        inventoryItems = inventoryItems.map {
            InventoryItem(it.id, it.name, it.category, it.weight, it.quantity, it.isEquipped, it.icon)
        },
        spells = spells.map { Spell(it.id, it.name, it.level, it.school, it.isPrepared, it.description) },
        features = features.map { Feature(it.id, it.name, it.description, it.resourceTracking) },
        notes = notes.map { Note(it.id, it.title, it.createdDate, it.updatedDate, it.content) }
    )

fun CharacterEntity.toDomain(): Character =
    Character(
        id = id,
        name = name,
        race = race,
        characterClass = characterClass,
        subclass = subclass,
        level = level,
        portraitUri = portraitUri,
        currentHp = currentHp,
        maxHp = maxHp,
        temporaryHp = temporaryHp,
        hitDieSides = hitDieSides,
        spentHitDice = spentHitDice,
        hasInspiration = hasInspiration,
        armorClass = armorClass,
        speed = speed,
        initiative = initiative,
        experience = experience,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        alignment = alignment,
        background = background,
        faith = faith,
        homeland = homeland,
        age = age,
        gender = gender,
        height = height,
        weight = weight,
        eyes = eyes,
        hair = hair,
        skin = skin,
        personalityTraits = personalityTraits,
        ideals = ideals,
        bonds = bonds,
        flaws = flaws,
        biography = biography,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Character.toEntity(): CharacterEntity =
    CharacterEntity(
        id = id,
        name = name,
        race = race,
        characterClass = characterClass,
        subclass = subclass,
        level = level,
        portraitUri = portraitUri,
        currentHp = currentHp,
        maxHp = maxHp,
        temporaryHp = temporaryHp,
        hitDieSides = hitDieSides,
        spentHitDice = spentHitDice,
        hasInspiration = hasInspiration,
        armorClass = armorClass,
        speed = speed,
        initiative = initiative,
        experience = experience,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        alignment = alignment,
        background = background,
        faith = faith,
        homeland = homeland,
        age = age,
        gender = gender,
        height = height,
        weight = weight,
        eyes = eyes,
        hair = hair,
        skin = skin,
        personalityTraits = personalityTraits,
        ideals = ideals,
        bonds = bonds,
        flaws = flaws,
        biography = biography,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun Skill.toEntity(characterId: Long): SkillEntity =
    SkillEntity(id = id, characterOwnerId = characterId, name = name, isProficient = isProficient)

fun Attack.toEntity(characterId: Long): AttackEntity =
    AttackEntity(id = id, characterOwnerId = characterId, name = name, icon = icon, range = range, attackBonusOrSaveDc = attackBonusOrSaveDc, damage = damage, damageType = damageType)

fun CombatResource.toEntity(characterId: Long): CombatResourceEntity =
    CombatResourceEntity(id = id, characterOwnerId = characterId, name = name, currentUses = currentUses, maximumUses = maximumUses)

fun InventoryItem.toEntity(characterId: Long): InventoryItemEntity =
    InventoryItemEntity(id = id, characterOwnerId = characterId, name = name, category = category, weight = weight, quantity = quantity, isEquipped = isEquipped, icon = icon)

fun Spell.toEntity(characterId: Long): SpellEntity =
    SpellEntity(id = id, characterOwnerId = characterId, name = name, level = level, school = school, isPrepared = isPrepared, description = description)

fun Feature.toEntity(characterId: Long): FeatureEntity =
    FeatureEntity(id = id, characterOwnerId = characterId, name = name, description = description, resourceTracking = resourceTracking)

fun Note.toEntity(characterId: Long): NoteEntity =
    NoteEntity(id = id, characterOwnerId = characterId, title = title, createdDate = createdDate, updatedDate = updatedDate, content = content)
