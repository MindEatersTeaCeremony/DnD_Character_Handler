package com.dndcharacterhandler.domain.model

enum class SpellCatalogSource { SRD_2014 }

data class SpellCatalogItem(
    val id: String,
    val name: String,
    val level: Int,
    val school: String,
    val description: String,
    val higherLevelDescription: String = "",
    val range: String = "",
    val castingTime: String = "",
    val duration: String = "",
    val components: String = "",
    val material: String = "",
    val isRitual: Boolean = false,
    val requiresConcentration: Boolean = false,
    val attackType: String = "",
    val availableClasses: String = "",
    val source: SpellCatalogSource = SpellCatalogSource.SRD_2014
) {
    fun toSpell(): Spell =
        Spell(
            catalogId = id,
            name = name,
            level = level,
            school = school,
            isPrepared = level == 0,
            description = description,
            higherLevelDescription = higherLevelDescription,
            range = range,
            castingTime = castingTime,
            duration = duration,
            components = components,
            material = material,
            isRitual = isRitual,
            requiresConcentration = requiresConcentration,
            attackType = attackType,
            availableClasses = availableClasses
        )
}
