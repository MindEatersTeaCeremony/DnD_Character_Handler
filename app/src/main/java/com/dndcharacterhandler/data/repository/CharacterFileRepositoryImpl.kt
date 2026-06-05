package com.dndcharacterhandler.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.dndcharacterhandler.data.local.dao.CharacterDao
import com.dndcharacterhandler.domain.model.CharacterBundle
import com.dndcharacterhandler.domain.repository.CharacterFileRepository
import com.dndcharacterhandler.domain.repository.CharacterRepository
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class CharacterFileRepositoryImpl(
    private val context: Context,
    private val characterDao: CharacterDao,
    private val characterRepository: CharacterRepository
) : CharacterFileRepository {

    override suspend fun exportCharacter(characterId: Long, destinationUri: String): Result<String> {
        return runCatching {
            val bundle = characterDao.getCharacter(characterId)?.toDomain()
                ?: error("Character not found.")
            val destination = Uri.parse(destinationUri)
            writeCharacterArchive(bundle, destination)
            destination.toString()
        }
    }

    override suspend fun importCharacter(sourceUri: String): Result<Long> {
        return runCatching {
            val importedArchive = readCharacterArchive(Uri.parse(sourceUri))
            characterRepository.createCharacter(
                importedArchive.characterBundle.copy(
                    character = importedArchive.characterBundle.character.copy(id = 0)
                )
            )
        }
    }

    private fun writeCharacterArchive(characterBundle: CharacterBundle, destinationUri: Uri) {
        val contentResolver = context.contentResolver
        val assetCollector = AssetCollector(contentResolver)
        val manifest = characterBundle.toArchiveManifest(
            exportedAt = System.currentTimeMillis(),
            mapAssetReference = assetCollector::registerAsset
        )

        contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOutputStream ->
                zipOutputStream.putNextEntry(ZipEntry("manifest.json"))
                zipOutputStream.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
                zipOutputStream.closeEntry()

                assetCollector.writeAssets(zipOutputStream)
            }
        } ?: error("Unable to open export destination.")
    }

    private fun readCharacterArchive(sourceUri: Uri): ImportedArchive {
        val importDirectory = File(context.filesDir, "imported_assets/${UUID.randomUUID()}").apply {
            mkdirs()
        }
        val extractedAssets = linkedMapOf<String, File>()
        var manifestJson: String? = null

        context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
            ZipInputStream(BufferedInputStream(inputStream)).use { zipInputStream ->
                var entry = zipInputStream.nextEntry
                while (entry != null) {
                    when {
                        entry.name == "manifest.json" -> {
                            manifestJson = zipInputStream.readBytes().toString(Charsets.UTF_8)
                        }

                        entry.name.startsWith("assets/") && !entry.isDirectory -> {
                            val targetFile = File(importDirectory, entry.name.removePrefix("assets/")).apply {
                                parentFile?.mkdirs()
                            }
                            FileOutputStream(targetFile).use { output ->
                                zipInputStream.copyTo(output)
                            }
                            extractedAssets[entry.name] = targetFile
                        }
                    }
                    zipInputStream.closeEntry()
                    entry = zipInputStream.nextEntry
                }
            }
        } ?: error("Unable to open import source.")

        val manifestText = manifestJson ?: error("Character archive is missing manifest.json.")
        val manifest = JSONObject(manifestText)
        return archiveManifestToCharacterBundle(manifest) { rawValue ->
            resolveImportedAssetReference(rawValue, extractedAssets)
        }
    }
}

private class AssetCollector(
    private val contentResolver: ContentResolver
) {
    private val assetsBySource = linkedMapOf<String, ArchiveAsset>()
    private val reservedEntryNames = linkedSetOf<String>()

    fun registerAsset(source: String?, preferredName: String): String? {
        if (source.isNullOrBlank()) return null
        val resolved = source.trim()
        if (!isBundledAssetCandidate(resolved)) return resolved

        val existing = assetsBySource[resolved]
        if (existing != null) return existing.entryName

        val extension = guessExtension(resolved)
        val entryName = reserveEntryName(slugify(preferredName), extension)
        assetsBySource[resolved] = ArchiveAsset(source = resolved, entryName = entryName)
        return entryName
    }

    fun writeAssets(zipOutputStream: ZipOutputStream) {
        assetsBySource.values.forEach { asset ->
            openInputStream(asset.source)?.use { inputStream ->
                zipOutputStream.putNextEntry(ZipEntry(asset.entryName))
                inputStream.copyTo(zipOutputStream)
                zipOutputStream.closeEntry()
            }
        }
    }

    private fun isBundledAssetCandidate(source: String): Boolean {
        val uri = source.toUri()
        return when {
            source.startsWith("content://") -> true
            source.startsWith("file://") -> true
            uri.scheme.isNullOrBlank() -> File(source).exists()
            else -> false
        }
    }

    private fun openInputStream(source: String) =
        when {
            source.startsWith("content://") || source.startsWith("file://") -> {
                contentResolver.openInputStream(Uri.parse(source))
            }

            File(source).exists() -> FileInputStream(source)
            else -> null
        }

    private fun guessExtension(source: String): String {
        val uri = Uri.parse(source)
        val mimeType = contentResolver.getType(uri)
        if (!mimeType.isNullOrBlank()) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.let { return it }
        }

        val path = uri.lastPathSegment ?: source
        val extension = path.substringAfterLast('.', missingDelimiterValue = "")
        return extension.ifBlank { "bin" }
    }

    private fun reserveEntryName(baseName: String, extension: String): String {
        var suffix = 0
        var candidate: String
        do {
            val fileName = if (suffix == 0) {
                "$baseName.$extension"
            } else {
                "${baseName}_$suffix.$extension"
            }
            candidate = "assets/$fileName"
            suffix += 1
        } while (!reservedEntryNames.add(candidate))
        return candidate
    }
}

private data class ArchiveAsset(
    val source: String,
    val entryName: String
)
