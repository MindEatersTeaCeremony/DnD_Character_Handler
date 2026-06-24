package com.dndcharacterhandler.data.repository

import android.content.Context
import com.dndcharacterhandler.domain.model.FeatureCatalogItem
import com.dndcharacterhandler.domain.model.FeatureSource
import com.dndcharacterhandler.domain.repository.FeatureCatalogRepository
import org.json.JSONArray
import org.json.JSONObject

class AssetFeatureCatalogRepository(
    private val context: Context
) : FeatureCatalogRepository {
    @Volatile
    private var cachedItems: List<FeatureCatalogItem>? = null

    override suspend fun getItems(): List<FeatureCatalogItem> {
        cachedItems?.let { return it }
        val classFeatures = readArray("5e-SRD-Features.json")
            .mapNotNull { parseFeature(it, FeatureSource.CLASS) }
        val racialTraits = readArray("5e-SRD-Traits.json")
            .mapNotNull { parseFeature(it, FeatureSource.RACE) }
        val originFeats = readArray("5e-SRD-Feats.json")
            .mapNotNull { parseOriginFeat(it) }
        val items = (classFeatures + racialTraits + originFeats).sortedBy { it.name }
        cachedItems = items
        return items
    }

    private fun readArray(assetName: String): JSONArray {
        val rawJson = runCatching {
            context.assets.open(assetName).bufferedReader().use { it.readText() }
        }.getOrNull() ?: return JSONArray()
        return runCatching { JSONArray(rawJson) }.getOrDefault(JSONArray())
    }

    private fun JSONArray.mapNotNull(transform: (JSONObject) -> FeatureCatalogItem?): List<FeatureCatalogItem> =
        buildList(length()) {
            for (index in 0 until length()) {
                transform(getJSONObject(index))?.let(::add)
            }
        }

    private fun parseFeature(json: JSONObject, source: FeatureSource): FeatureCatalogItem? {
        val index = json.optString("index").ifBlank { return null }
        val name = json.optString("name").ifBlank { return null }
        val description = json.optString("description").ifBlank {
            json.optJSONArray("desc").joinParagraphs()
        }
        val level = json.optJSONObject("level")?.optString("index")
            ?.let { Regex("""(\d+)$""").find(it)?.groupValues?.get(1)?.toIntOrNull() }
        val category = when (source) {
            FeatureSource.CLASS -> json.optJSONObject("class")?.optString("name").orEmpty()
            FeatureSource.RACE -> json.optJSONArray("species").joinNames()
            else -> ""
        }
        return FeatureCatalogItem(
            id = "${source.name}:$index",
            name = name,
            description = description,
            level = level,
            source = source,
            category = category
        )
    }

    private fun parseOriginFeat(json: JSONObject): FeatureCatalogItem? {
        if (!json.optString("type").equals("origin", ignoreCase = true)) return null
        val index = json.optString("index").ifBlank { return null }
        val name = json.optString("name").ifBlank { return null }
        val description = json.optString("description").ifBlank {
            json.optJSONArray("desc").joinParagraphs()
        }
        return FeatureCatalogItem(
            id = "FEAT:$index",
            name = name,
            description = description,
            level = null,
            source = FeatureSource.BACKGROUND,
            category = "Origin Feat"
        )
    }

    private fun JSONArray?.joinParagraphs(): String {
        if (this == null) return ""
        return buildList(length()) {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }.joinToString("\n\n")
    }

    private fun JSONArray?.joinNames(): String {
        if (this == null) return ""
        return buildList(length()) {
            for (index in 0 until length()) {
                optJSONObject(index)?.optString("name")?.takeIf { it.isNotBlank() }?.let(::add)
            }
        }.joinToString(", ")
    }
}
