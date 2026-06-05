package com.dndcharacterhandler.data.localization

import android.content.Context
import com.dndcharacterhandler.domain.model.AppLanguage
import org.json.JSONObject
import java.util.Locale

class LocalizationRepository(context: Context) {
    private val localizedValuesByLanguage: Map<AppLanguage, Map<String, String>>

    init {
        val json = context.assets.open("localization.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        localizedValuesByLanguage = AppLanguage.entries.associateWith { language ->
            val languageJson = root.getJSONObject(language.code)
            buildMap {
                val keys = languageJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    put(key, languageJson.getString(key))
                }
            }
        }
    }

    fun getStrings(language: AppLanguage): LocalizedStrings {
        return LocalizedStrings(
            language = language,
            values = localizedValuesByLanguage[language].orEmpty()
        )
    }
}

data class LocalizedStrings(
    val language: AppLanguage,
    private val values: Map<String, String>
) {
    operator fun get(key: String): String = values[key] ?: key

    fun format(key: String, vararg args: Any?): String {
        return String.format(Locale.ROOT, this[key], *args)
    }
}

