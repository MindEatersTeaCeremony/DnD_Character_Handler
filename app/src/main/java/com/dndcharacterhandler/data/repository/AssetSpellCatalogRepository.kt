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
            isRitual = json.optBoolean("ritual", false),
            requiresConcentration = json.optBoolean("concentration", false),
            attackType = json.optString("attack_type"),
            availableClasses = json.optJSONArray("classes").joinObjectNames()
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
}
