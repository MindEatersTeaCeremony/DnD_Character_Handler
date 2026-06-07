package com.dndcharacterhandler.domain.model

data class Character(
    val id: Long = 0,
    val name: String,
    val race: String,
    val characterClass: String,
    val subclass: String,
    val level: Int,
    val portraitUri: String?,
    val currentHp: Int,
    val maxHp: Int,
    val temporaryHp: Int,
    val hitDieSides: Int,
    val spentHitDice: Int,
    val hasInspiration: Boolean,
    val armorClass: Int,
    val speed: Int,
    val initiative: Int,
    val experience: Int,
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val strengthSaveProficient: Boolean,
    val dexteritySaveProficient: Boolean,
    val constitutionSaveProficient: Boolean,
    val intelligenceSaveProficient: Boolean,
    val wisdomSaveProficient: Boolean,
    val charismaSaveProficient: Boolean,
    val passivePerceptionBonus: Int,
    val armorProficiencies: String,
    val weaponProficiencies: String,
    val toolProficiencies: String,
    val alignment: String,
    val background: String,
    val faith: String,
    val homeland: String,
    val age: String,
    val gender: String,
    val height: String,
    val weight: String,
    val eyes: String,
    val hair: String,
    val skin: String,
    val personalityTraits: String,
    val ideals: String,
    val bonds: String,
    val flaws: String,
    val biography: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class CharacterBundle(
    val character: Character,
    val skills: List<Skill>,
    val attacks: List<Attack>,
    val combatResources: List<CombatResource>,
    val inventoryItems: List<InventoryItem>,
    val spells: List<Spell>,
    val features: List<Feature>,
    val notes: List<Note>
)

data class Skill(
    val id: Long = 0,
    val name: String,
    val isProficient: Boolean,
    val isExpertise: Boolean = false,
    val hasJackOfAllTrades: Boolean = false
)
data class Attack(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val range: String,
    val attackBonusOrSaveDc: String,
    val damage: String,
    val damageType: String
)
data class CombatResource(
    val id: Long = 0,
    val name: String,
    val currentUses: Int,
    val maximumUses: Int
)
enum class InventoryCategory { WEAPON, ARMOR, CONSUMABLE, OTHER }
data class InventoryItem(
    val id: Long = 0,
    val name: String,
    val category: InventoryCategory,
    val weight: Double,
    val quantity: Int,
    val isEquipped: Boolean,
    val icon: String
)
data class Spell(
    val id: Long = 0,
    val name: String,
    val level: Int,
    val school: String,
    val isPrepared: Boolean,
    val description: String
)
data class Feature(
    val id: Long = 0,
    val name: String,
    val description: String,
    val resourceTracking: String?
)
data class Note(
    val id: Long = 0,
    val title: String,
    val createdDate: Long,
    val updatedDate: Long,
    val content: String
)

fun defaultCharacterBundle(now: Long = System.currentTimeMillis()): CharacterBundle {
    val skills = listOf(
        "skill_acrobatics", "skill_animal_handling", "skill_arcana", "skill_athletics", "skill_deception",
        "skill_history", "skill_insight", "skill_intimidation", "skill_investigation", "skill_medicine",
        "skill_nature", "skill_perception", "skill_performance", "skill_persuasion", "skill_religion",
        "skill_sleight_of_hand", "skill_stealth", "skill_survival"
    ).map { Skill(name = it, isProficient = false, isExpertise = false, hasJackOfAllTrades = false) }

    return CharacterBundle(
        character = Character(
            name = "",
            race = "",
            characterClass = "",
            subclass = "",
            level = 1,
            portraitUri = null,
            currentHp = 8,
            maxHp = 8,
            temporaryHp = 0,
            hitDieSides = 8,
            spentHitDice = 0,
            hasInspiration = false,
            armorClass = 10,
            speed = 30,
            initiative = 0,
            experience = 0,
            strength = 10,
            dexterity = 10,
            constitution = 10,
            intelligence = 10,
            wisdom = 10,
            charisma = 10,
            strengthSaveProficient = false,
            dexteritySaveProficient = false,
            constitutionSaveProficient = false,
            intelligenceSaveProficient = false,
            wisdomSaveProficient = false,
            charismaSaveProficient = false,
            passivePerceptionBonus = 0,
            armorProficiencies = "",
            weaponProficiencies = "",
            toolProficiencies = "",
            alignment = "",
            background = "",
            faith = "",
            homeland = "",
            age = "",
            gender = "",
            height = "",
            weight = "",
            eyes = "",
            hair = "",
            skin = "",
            personalityTraits = "",
            ideals = "",
            bonds = "",
            flaws = "",
            biography = "",
            createdAt = now,
            updatedAt = now
        ),
        skills = skills,
        attacks = emptyList(),
        combatResources = emptyList(),
        inventoryItems = emptyList(),
        spells = emptyList(),
        features = emptyList(),
        notes = emptyList()
    )
}
