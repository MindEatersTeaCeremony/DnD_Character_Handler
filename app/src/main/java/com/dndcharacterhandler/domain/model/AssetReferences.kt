package com.dndcharacterhandler.domain.model

private val assetFileRegex = Regex("[^a-z0-9_./-]+")

enum class AssetIconGroup(val folder: String) {
    ITEMS("items"),
    SPELLS("spells"),
    ATTACKS("attacks"),
    FEATURES("features"),
    CATEGORIES("categories"),
    PORTRAITS("portraits/placeholders")
}

object AssetReferences {
    const val drawableUiPrefix = "ic_ui_"
    const val drawableDecorPrefix = "decor_"
    const val drawableGamePrefix = "ic_game_"
    const val drawableReferencePrefix = "drawable:"

    const val iconsRoot = "icons"
    const val portraitsRoot = "portraits"

    fun iconAssetPath(group: AssetIconGroup, fileName: String): String {
        return "$iconsRoot/${group.folder}/${sanitizeAssetFileName(fileName)}"
    }

    fun portraitPlaceholderPath(fileName: String): String {
        return "$portraitsRoot/placeholders/${sanitizeAssetFileName(fileName)}"
    }

    fun drawableName(prefix: String, rawName: String): String {
        val normalized = rawName
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .ifBlank { "asset" }
        return prefix + normalized
    }

    fun drawableReference(drawableName: String): String {
        return drawableReferencePrefix + drawableName
    }

    private fun sanitizeAssetFileName(fileName: String): String {
        return fileName
            .lowercase()
            .replace(assetFileRegex, "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .ifBlank { "asset" }
    }
}
