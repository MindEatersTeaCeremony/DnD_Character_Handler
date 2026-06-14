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
import com.dndcharacterhandler.domain.model.ArmorClassMode
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.model.CombatResource
import com.dndcharacterhandler.domain.model.Feature
import com.dndcharacterhandler.domain.model.InventoryArmorDetails
import com.dndcharacterhandler.domain.model.InventoryItem
import com.dndcharacterhandler.domain.model.InventoryWeaponDamage
import com.dndcharacterhandler.domain.model.InventoryWeaponDetails
import com.dndcharacterhandler.domain.model.InventoryWeaponProperty
import com.dndcharacterhandler.domain.model.Note
import com.dndcharacterhandler.domain.model.Skill
import com.dndcharacterhandler.domain.model.Spell

fun CharacterWithDetails.toDomain(): CharacterBundle =
    CharacterBundle(
        character = character.toDomain(),
        skills = skills.map { Skill(it.id, it.name, it.isProficient, it.isExpertise, it.hasJackOfAllTrades) },
        attacks = attacks.map {
            Attack(
                id = it.id,
                name = it.name,
                icon = it.icon,
                isProficient = it.isProficient,
                calculationMode = it.calculationMode,
                ability = it.ability,
                normalRange = it.normalRange,
                longRange = it.longRange,
                damageDiceCount = it.damageDiceCount,
                damageDieType = it.damageDieType,
                alternateDamageDiceCount = it.alternateDamageDiceCount,
                alternateDamageDieType = it.alternateDamageDieType,
                alternateDamageType = it.alternateDamageType,
                magicalBonus = it.magicalBonus,
                applyAbilityModifierToDamage = it.applyAbilityModifierToDamage,
                range = it.range,
                attackBonusOrSaveDc = it.attackBonusOrSaveDc,
                damage = it.damage,
                damageType = it.damageType
            )
        },
        combatResources = combatResources.map {
            CombatResource(
                it.id,
                it.name,
                it.currentUses,
                it.maximumUses,
                it.restoresOnShortRest,
                it.restoresOnLongRest
            )
        },
        inventoryItems = inventoryItems.sortedBy { it.id }.map {
            InventoryItem(
                id = it.id,
                name = it.name,
                description = it.description,
                isMagical = it.isMagical,
                magicalBonus = it.magicalBonus,
                category = it.category,
                weight = it.weight,
                quantity = it.quantity,
                isEquipped = it.isEquipped,
                icon = it.icon,
                costQuantity = it.costQuantity,
                costUnit = it.costUnit,
                armorDetails = if (it.armorType != null && it.armorClass != null && it.appliesDexterityBonus != null && it.strengthMinimum != null && it.hasStealthDisadvantage != null) {
                    InventoryArmorDetails(
                        armorType = it.armorType,
                        armorClass = it.armorClass,
                        appliesDexterityBonus = it.appliesDexterityBonus,
                        maxDexterityBonus = it.maxDexterityBonus,
                        strengthMinimum = it.strengthMinimum,
                        hasStealthDisadvantage = it.hasStealthDisadvantage
                    )
                } else {
                    null
                },
                weaponDetails = if (
                    it.weaponClass != null &&
                    it.weaponRangeType != null &&
                    it.weaponPrimaryDamageDice != null &&
                    it.weaponPrimaryDamageType != null
                ) {
                    InventoryWeaponDetails(
                        weaponClass = it.weaponClass,
                        rangeType = it.weaponRangeType,
                        baseWeaponId = it.weaponBaseId,
                        normalRange = it.weaponNormalRange,
                        longRange = it.weaponLongRange,
                        damages = listOf(
                            InventoryWeaponDamage(
                                dice = it.weaponPrimaryDamageDice,
                                damageType = it.weaponPrimaryDamageType
                            )
                        ),
                        twoHandedDamage = if (it.weaponTwoHandedDamageDice != null && it.weaponTwoHandedDamageType != null) {
                            InventoryWeaponDamage(
                                dice = it.weaponTwoHandedDamageDice,
                                damageType = it.weaponTwoHandedDamageType
                            )
                        } else {
                            null
                        },
                        properties = it.weaponProperties
                            .orEmpty()
                            .split(',')
                            .mapNotNull { value ->
                                value.takeIf { it.isNotBlank() }?.let(InventoryWeaponProperty::valueOf)
                            }
                            .toSet()
                    )
                } else {
                    null
                }
            )
        },
        spells = spells.map { Spell(it.id, it.name, it.level, it.school, it.isPrepared, it.description) },
        features = features.map { Feature(it.id, it.name, it.description, it.level, it.source) },
        notes = notes.map { Note(it.id, it.title, it.createdDate, it.updatedDate, it.content, it.isPinned) }
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
        baseArmorClass = baseArmorClass,
        armorClassMode = armorClassMode,
        copperPieces = copperPieces,
        silverPieces = silverPieces,
        goldPieces = goldPieces,
        speed = speed,
        initiative = initiative,
        initiativeBonus = initiativeBonus,
        spellcastingAbility = spellcastingAbility,
        experience = experience,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        strengthSaveProficient = strengthSaveProficient,
        dexteritySaveProficient = dexteritySaveProficient,
        constitutionSaveProficient = constitutionSaveProficient,
        intelligenceSaveProficient = intelligenceSaveProficient,
        wisdomSaveProficient = wisdomSaveProficient,
        charismaSaveProficient = charismaSaveProficient,
        passivePerceptionBonus = passivePerceptionBonus,
        armorProficiencies = armorProficiencies,
        weaponProficiencies = weaponProficiencies,
        toolProficiencies = toolProficiencies,
        languageProficiencies = languageProficiencies,
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
        baseArmorClass = baseArmorClass,
        armorClassMode = armorClassMode,
        copperPieces = copperPieces,
        silverPieces = silverPieces,
        goldPieces = goldPieces,
        speed = speed,
        initiative = initiative,
        initiativeBonus = initiativeBonus,
        spellcastingAbility = spellcastingAbility,
        experience = experience,
        strength = strength,
        dexterity = dexterity,
        constitution = constitution,
        intelligence = intelligence,
        wisdom = wisdom,
        charisma = charisma,
        strengthSaveProficient = strengthSaveProficient,
        dexteritySaveProficient = dexteritySaveProficient,
        constitutionSaveProficient = constitutionSaveProficient,
        intelligenceSaveProficient = intelligenceSaveProficient,
        wisdomSaveProficient = wisdomSaveProficient,
        charismaSaveProficient = charismaSaveProficient,
        passivePerceptionBonus = passivePerceptionBonus,
        armorProficiencies = armorProficiencies,
        weaponProficiencies = weaponProficiencies,
        toolProficiencies = toolProficiencies,
        languageProficiencies = languageProficiencies,
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
    SkillEntity(
        id = id,
        characterOwnerId = characterId,
        name = name,
        isProficient = isProficient,
        isExpertise = isExpertise,
        hasJackOfAllTrades = hasJackOfAllTrades
    )

fun Attack.toEntity(characterId: Long): AttackEntity =
    AttackEntity(
        id = id,
        characterOwnerId = characterId,
        name = name,
        icon = icon,
        isProficient = isProficient,
        calculationMode = calculationMode,
        ability = ability,
        normalRange = normalRange,
        longRange = longRange,
        damageDiceCount = damageDiceCount,
        damageDieType = damageDieType,
        alternateDamageDiceCount = alternateDamageDiceCount,
        alternateDamageDieType = alternateDamageDieType,
        alternateDamageType = alternateDamageType,
        magicalBonus = magicalBonus,
        applyAbilityModifierToDamage = applyAbilityModifierToDamage,
        range = range,
        attackBonusOrSaveDc = attackBonusOrSaveDc,
        damage = damage,
        damageType = damageType
    )

fun CombatResource.toEntity(characterId: Long): CombatResourceEntity =
    CombatResourceEntity(
        id = id,
        characterOwnerId = characterId,
        name = name,
        currentUses = currentUses,
        maximumUses = maximumUses,
        restoresOnShortRest = restoresOnShortRest,
        restoresOnLongRest = restoresOnLongRest
    )

fun InventoryItem.toEntity(characterId: Long): InventoryItemEntity =
    InventoryItemEntity(
        id = id,
        characterOwnerId = characterId,
        name = name,
        description = description,
        isMagical = isMagical,
        magicalBonus = magicalBonus,
        category = category,
        weight = weight,
        quantity = quantity,
        isEquipped = isEquipped,
        icon = icon,
        costQuantity = costQuantity,
        costUnit = costUnit,
        armorType = armorDetails?.armorType,
        armorClass = armorDetails?.armorClass,
        appliesDexterityBonus = armorDetails?.appliesDexterityBonus,
        maxDexterityBonus = armorDetails?.maxDexterityBonus,
        strengthMinimum = armorDetails?.strengthMinimum,
        hasStealthDisadvantage = armorDetails?.hasStealthDisadvantage,
        weaponClass = weaponDetails?.weaponClass,
        weaponRangeType = weaponDetails?.rangeType,
        weaponBaseId = weaponDetails?.baseWeaponId,
        weaponNormalRange = weaponDetails?.normalRange,
        weaponLongRange = weaponDetails?.longRange,
        weaponPrimaryDamageDice = weaponDetails?.damages?.firstOrNull()?.dice,
        weaponPrimaryDamageType = weaponDetails?.damages?.firstOrNull()?.damageType,
        weaponTwoHandedDamageDice = weaponDetails?.twoHandedDamage?.dice,
        weaponTwoHandedDamageType = weaponDetails?.twoHandedDamage?.damageType,
        weaponProperties = weaponDetails?.properties
            ?.map(InventoryWeaponProperty::name)
            ?.sorted()
            ?.joinToString(",")
    )

fun Spell.toEntity(characterId: Long): SpellEntity =
    SpellEntity(id = id, characterOwnerId = characterId, name = name, level = level, school = school, isPrepared = isPrepared, description = description)

fun Feature.toEntity(characterId: Long): FeatureEntity =
    FeatureEntity(
        id = id,
        characterOwnerId = characterId,
        name = name,
        description = description,
        level = level,
        source = source
    )

fun Note.toEntity(characterId: Long): NoteEntity =
    NoteEntity(id = id, characterOwnerId = characterId, title = title, createdDate = createdDate, updatedDate = updatedDate, content = content, isPinned = isPinned)
