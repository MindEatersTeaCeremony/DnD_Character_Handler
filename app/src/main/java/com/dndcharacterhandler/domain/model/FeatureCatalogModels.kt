package com.dndcharacterhandler.domain.model

data class FeatureCatalogItem(
    val id: String,
    val name: String,
    val description: String,
    val level: Int? = null,
    val source: FeatureSource,
    val category: String = ""
) {
    fun toFeature(): Feature =
        Feature(
            name = name,
            description = description,
            level = level,
            source = source
        )
}
