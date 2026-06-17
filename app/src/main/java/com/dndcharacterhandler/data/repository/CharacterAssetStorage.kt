package com.dndcharacterhandler.data.repository

import java.io.File

/**
 * Single source of truth for where a character's binary assets (portrait, imported icons)
 * live on internal storage. Centralized so that import, the portrait picker and character
 * deletion all agree on the same locations and orphaned files can be reliably cleaned up.
 */
object CharacterAssetStorage {

    private const val PORTRAITS_ROOT = "character_portraits"
    private const val IMPORT_STAGING_ROOT = "imported_assets"

    /** Permanent per-character directory holding the portrait and any imported icons. */
    fun characterAssetsDir(filesDir: File, characterId: Long): File =
        File(filesDir, "$PORTRAITS_ROOT/$characterId")

    /** Temporary directory an archive is extracted into before its assets are relocated. */
    fun importStagingDir(filesDir: File, token: String): File =
        File(filesDir, "$IMPORT_STAGING_ROOT/$token")

    /** Removes every file owned by a character. Best effort; safe to call for unknown ids. */
    fun deleteCharacterAssets(filesDir: File, characterId: Long) {
        runCatching { characterAssetsDir(filesDir, characterId).deleteRecursively() }
    }

    /**
     * Deletes asset directories whose owning character no longer exists. Does not touch the
     * legacy [IMPORT_STAGING_ROOT] tree, since pre-existing imports may still reference it.
     */
    fun purgeOrphanedAssets(filesDir: File, validCharacterIds: Set<Long>) {
        val root = File(filesDir, PORTRAITS_ROOT)
        val directories = root.listFiles() ?: return
        directories.forEach { directory ->
            val ownerId = directory.name.toLongOrNull()
            if (ownerId == null || ownerId !in validCharacterIds) {
                runCatching { directory.deleteRecursively() }
            }
        }
    }
}
