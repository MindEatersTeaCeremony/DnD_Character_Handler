package com.dndcharacterhandler.presentation.theme

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import org.json.JSONObject

data class TextSizeToken(
    val fontSizeSp: Float,
    val lineHeightSp: Float? = null,
    val alpha: Float? = null
)

data class DesignTypographyTokens(
    val headlineMedium: TextSizeToken,
    val titleLarge: TextSizeToken,
    val titleMedium: TextSizeToken,
    val bodyLarge: TextSizeToken,
    val bodyMedium: TextSizeToken,
    val labelMedium: TextSizeToken,
    val characterName: TextSizeToken,
    val portraitInitial: TextSizeToken,
    val actionButtonLabel: TextSizeToken,
    val xpLabel: TextSizeToken,
    val hpCurrent: TextSizeToken,
    val hpTemporary: TextSizeToken,
    val hpMaximum: TextSizeToken,
    val hpLabel: TextSizeToken,
    val miniStatValue: TextSizeToken,
    val miniStatLabel: TextSizeToken,
    val subtitleToken: TextSizeToken,
    val shortRestDiceCount: TextSizeToken,
    val shortRestDieToken: TextSizeToken,
    val shortRestCounterButton: TextSizeToken,
    val shortRestCounterValue: TextSizeToken
)

data class DesignTokens(
    val typography: DesignTypographyTokens
)

val DefaultDesignTokens = DesignTokens(
    typography = DesignTypographyTokens(
        headlineMedium = TextSizeToken(fontSizeSp = 28f, lineHeightSp = 32f),
        titleLarge = TextSizeToken(fontSizeSp = 22f),
        titleMedium = TextSizeToken(fontSizeSp = 18f),
        bodyLarge = TextSizeToken(fontSizeSp = 16f, lineHeightSp = 22f),
        bodyMedium = TextSizeToken(fontSizeSp = 14f, lineHeightSp = 20f),
        labelMedium = TextSizeToken(fontSizeSp = 12f),
        characterName = TextSizeToken(fontSizeSp = 32f, lineHeightSp = 35f),
        portraitInitial = TextSizeToken(fontSizeSp = 40f),
        actionButtonLabel = TextSizeToken(fontSizeSp = 12f, lineHeightSp = 13f),
        xpLabel = TextSizeToken(fontSizeSp = 16f),
        hpCurrent = TextSizeToken(fontSizeSp = 64f, lineHeightSp = 68f),
        hpTemporary = TextSizeToken(fontSizeSp = 40f, lineHeightSp = 44f),
        hpMaximum = TextSizeToken(fontSizeSp = 40f, lineHeightSp = 44f, alpha = 0.62f),
        hpLabel = TextSizeToken(fontSizeSp = 16f),
        miniStatValue = TextSizeToken(fontSizeSp = 28f, lineHeightSp = 30f),
        miniStatLabel = TextSizeToken(fontSizeSp = 12f),
        subtitleToken = TextSizeToken(fontSizeSp = 16f),
        shortRestDiceCount = TextSizeToken(fontSizeSp = 32f),
        shortRestDieToken = TextSizeToken(fontSizeSp = 18f),
        shortRestCounterButton = TextSizeToken(fontSizeSp = 28f),
        shortRestCounterValue = TextSizeToken(fontSizeSp = 40f)
    )
)

val LocalDesignTokens = staticCompositionLocalOf { DefaultDesignTokens }

fun loadDesignTokens(context: Context): DesignTokens {
    return runCatching {
        val root = JSONObject(
            context.assets.open("design_tokens.json")
                .bufferedReader()
                .use { it.readText() }
        )
        val typography = root.optJSONObject("typography") ?: JSONObject()
        val materialTheme = typography.optJSONObject("materialTheme") ?: JSONObject()
        val overview = typography.optJSONObject("overviewOverrides") ?: JSONObject()
        val defaults = DefaultDesignTokens.typography

        DesignTokens(
            typography = DesignTypographyTokens(
                headlineMedium = materialTheme.textToken("headlineMedium", defaults.headlineMedium),
                titleLarge = materialTheme.textToken("titleLarge", defaults.titleLarge),
                titleMedium = materialTheme.textToken("titleMedium", defaults.titleMedium),
                bodyLarge = materialTheme.textToken("bodyLarge", defaults.bodyLarge),
                bodyMedium = materialTheme.textToken("bodyMedium", defaults.bodyMedium),
                labelMedium = materialTheme.textToken("labelMedium", defaults.labelMedium),
                characterName = overview.textToken("characterName", defaults.characterName),
                portraitInitial = overview.textToken("portraitInitial", defaults.portraitInitial),
                actionButtonLabel = overview.textToken("actionButtonLabel", defaults.actionButtonLabel),
                xpLabel = overview.textToken("xpLabel", defaults.xpLabel),
                hpCurrent = overview.textToken("hpCurrent", defaults.hpCurrent),
                hpTemporary = overview.textToken("hpTemporary", defaults.hpTemporary),
                hpMaximum = overview.textToken("hpMaximum", defaults.hpMaximum),
                hpLabel = overview.textToken("hpLabel", defaults.hpLabel),
                miniStatValue = overview.textToken("miniStatValue", defaults.miniStatValue),
                miniStatLabel = overview.textToken("miniStatLabel", defaults.miniStatLabel),
                subtitleToken = overview.textToken("subtitleToken", defaults.subtitleToken),
                shortRestDiceCount = overview.textToken("shortRestDiceCount", defaults.shortRestDiceCount),
                shortRestDieToken = overview.textToken("shortRestDieToken", defaults.shortRestDieToken),
                shortRestCounterButton = overview.textToken("shortRestCounterButton", defaults.shortRestCounterButton),
                shortRestCounterValue = overview.textToken("shortRestCounterValue", defaults.shortRestCounterValue)
            )
        )
    }.getOrDefault(DefaultDesignTokens)
}

private fun JSONObject.textToken(
    key: String,
    fallback: TextSizeToken
): TextSizeToken {
    val value = optJSONObject(key) ?: return fallback
    val hasLineHeight = value.has("lineHeightSp")
    val hasAlpha = value.has("alpha")
    return TextSizeToken(
        fontSizeSp = value.optDouble("fontSizeSp", fallback.fontSizeSp.toDouble()).toFloat(),
        lineHeightSp = if (hasLineHeight) {
            value.optDouble("lineHeightSp").toFloat()
        } else {
            fallback.lineHeightSp
        },
        alpha = if (hasAlpha) {
            value.optDouble("alpha").toFloat()
        } else {
            fallback.alpha
        }
    )
}
