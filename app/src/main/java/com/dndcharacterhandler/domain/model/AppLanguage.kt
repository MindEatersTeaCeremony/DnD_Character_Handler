package com.dndcharacterhandler.domain.model

enum class AppLanguage(val code: String, val localizationKey: String) {
    ENGLISH("en", "language_english"),
    RUSSIAN("ru", "language_russian"),
    GERMAN("de", "language_german"),
    FRENCH("fr", "language_french"),
    SPANISH("es", "language_spanish")
}
