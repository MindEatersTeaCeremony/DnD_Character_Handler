package com.dndcharacterhandler.presentation.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.dndcharacterhandler.data.localization.LocalizedStrings
import com.dndcharacterhandler.domain.model.AppLanguage

val LocalStrings = staticCompositionLocalOf {
    LocalizedStrings(
        language = AppLanguage.ENGLISH,
        values = emptyMap()
    )
}

@Composable
@ReadOnlyComposable
fun text(key: String): String = LocalStrings.current[key]
