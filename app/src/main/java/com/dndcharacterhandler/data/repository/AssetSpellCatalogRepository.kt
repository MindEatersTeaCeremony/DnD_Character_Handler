package com.dndcharacterhandler.data.repository

import android.content.Context
import com.dndcharacterhandler.domain.model.SpellCatalogItem
import com.dndcharacterhandler.domain.repository.SpellCatalogRepository
import org.json.JSONArray
import org.json.JSONObject

class AssetSpellCatalogRepository(
    private val context: Context
) : SpellCatalogRepository {
    @Volatile
    private var cachedItems: List<SpellCatalogItem>? = null

    override suspend fun getItems(): List<SpellCatalogItem> {
        cachedItems?.let { return it }
        val items = readArray("5e-SRD-Spells.json")
            .mapNotNull(::parseSpell)
            .sortedBy { it.name }
        cachedItems = items
        return items
    }

    private fun readArray(assetName: String): JSONArray {
        val rawJson = context.assets.open(assetName).bufferedReader().use { it.readText() }
        return JSONArray(rawJson)
    }

    private fun JSONArray.mapNotNull(transform: (JSONObject) -> SpellCatalogItem?): List<SpellCatalogItem> =
        buildList(length()) {
            for (index in 0 until length()) {
                transform(getJSONObject(index))?.let(::add)
            }
        }

    private fun parseSpell(json: JSONObject): SpellCatalogItem? {
        val id = json.optString("index").ifBlank { return null }
        val name = json.optString("name").ifBlank { return null }
        val description = json.optJSONArray("desc").joinText()
        val higherLevel = json.optJSONArray("higher_level").joinText()
        val material = json.optString("material")
        val components = buildString {
            append(json.optJSONArray("components").joinValues())
            if (material.isNotBlank()) {
                if (isNotBlank()) append(" ")
                append("($material)")
            }
        }

        val damageJson = json.optJSONObject("damage")
        val damageScaling = damageJson?.optJSONObject("damage_at_slot_level")
            ?: damageJson?.optJSONObject("damage_at_character_level")
        val dcJson = json.optJSONObject("dc")
        val (primaryComponent, altComponent) = splitDamageComponents(damageScaling.baseValue())
        val healComponent = json.optJSONObject("heal_at_slot_level").baseValue()

        return SpellCatalogItem(
            id = "spell:$id",
            name = name,
            level = json.optInt("level"),
            school = json.optJSONObject("school")?.optString("name").orEmpty(),
            description = description,
            higherLevelDescription = higherLevel,
            range = json.optString("range"),
            castingTime = json.optString("casting_time"),
            duration = json.optString("duration"),
            components = components,
            material = material,
            materialCost = material.extractGpCost(),
            isRitual = json.optBoolean("ritual", false),
            requiresConcentration = json.optBoolean("concentration", false),
            attackType = json.optString("attack_type"),
            availableClasses = json.optJSONArray("classes").joinObjectNames(),
            damageType = damageJson?.optJSONObject("damage_type")?.optString("name").orEmpty(),
            damageBase = primaryComponent.diceOnly(),
            damageBonusValue = primaryComponent.bonusValue(),
            damageBonusIsModifier = primaryComponent.bonusIsModifier(),
            altDamageBase = altComponent.diceOnly(),
            altDamageBonusValue = altComponent.bonusValue(),
            altDamageBonusIsModifier = altComponent.bonusIsModifier(),
            damage = damageScaling.joinScaling(),
            saveAbility = dcJson?.optJSONObject("dc_type")?.optString("name").orEmpty(),
            saveEffect = dcJson?.optString("dc_success").orEmpty(),
            areaOfEffect = json.optJSONObject("area_of_effect").formatAreaOfEffect(),
            healBase = healComponent.diceOnly(),
            healBonusValue = healComponent.bonusValue(),
            healBonusIsModifier = healComponent.bonusIsModifier(),
            healing = json.optJSONObject("heal_at_slot_level").joinScaling()
        )
    }

    private fun JSONArray?.joinText(): String {
        if (this == null) return ""
        return buildList(length()) {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }.joinToString("\n\n")
    }

    private fun JSONArray?.joinValues(): String {
        if (this == null) return ""
        return buildList(length()) {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }.joinToString(", ")
    }

    private fun JSONArray?.joinObjectNames(): String {
        if (this == null) return ""
        return buildList(length()) {
            for (index in 0 until length()) {
                optJSONObject(index)?.optString("name")?.takeIf { it.isNotBlank() }?.let(::add)
            }
        }.joinToString(", ")
    }

    private fun JSONObject?.joinScaling(): String {
        if (this == null) return ""
        return keys().asSequence()
            .sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
            .mapNotNull { key -> optString(key).takeIf { it.isNotBlank() }?.let { "$key: $it" } }
            .joinToString("\n")
    }

    private fun JSONObject?.baseValue(): String {
        if (this == null) return ""
        val minKey = keys().asSequence()
            .minByOrNull { it.toIntOrNull() ?: Int.MAX_VALUE }
            ?: return ""
        return optString(minKey).orEmpty()
    }

    private fun splitDamageComponents(base: String): Pair<String, String> {
        val diceTerms = Regex("""\d+d\d+""").findAll(base).toList()
        if (diceTerms.size < 2) return base to ""
        val secondStart = diceTerms[1].range.first
        val primary = base.substring(0, secondStart).trim().trimEnd('+', ' ').trim()
        val alternate = base.substring(secondStart).trim()
        return primary to alternate
    }

    private fun String.diceOnly(): String =
        Regex("""\d+d\d+""", RegexOption.IGNORE_CASE).find(this)?.value.orEmpty()

    private fun String.bonusIsModifier(): Boolean = contains("MOD", ignoreCase = true)

    private fun String.bonusValue(): Int {
        if (bonusIsModifier()) return 0
        val withoutDice = replaceFirst(Regex("""\d+d\d+""", RegexOption.IGNORE_CASE), "")
        return Regex("""\d+""").find(withoutDice)?.value?.toIntOrNull() ?: 0
    }

    private fun JSONObject?.formatAreaOfEffect(): String {
        if (this == null) return ""
        val type = optString("type").takeIf { it.isNotBlank() } ?: return ""
        val size = optInt("size")
        return "$type, $size ft"
    }

    private fun String.extractGpCost(): String {
        val match = gpCostRegex.find(this) ?: return ""
        return match.groupValues[1].replace(",", "")
    }

    private companion object {
        private val gpCostRegex = Regex("""(\d[\d,]*)\s*gp""", RegexOption.IGNORE_CASE)
    }
}
