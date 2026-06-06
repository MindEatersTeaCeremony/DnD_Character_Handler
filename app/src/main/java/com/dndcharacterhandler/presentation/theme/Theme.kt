package com.dndcharacterhandler.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Keep app/src/main/assets/design_tokens.json in sync when theme fonts, sizes, or colors change.
data class DnDSpacing(
    val xs: androidx.compose.ui.unit.Dp = 4.dp,
    val sm: androidx.compose.ui.unit.Dp = 8.dp,
    val md: androidx.compose.ui.unit.Dp = 16.dp,
    val lg: androidx.compose.ui.unit.Dp = 24.dp,
    val xl: androidx.compose.ui.unit.Dp = 32.dp
)

val LocalDnDSpacing = staticCompositionLocalOf { DnDSpacing() }

object DnDCardDefaults {
    @Composable
    fun elevatedCardColors(): CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
}

private val DnDColors = darkColorScheme(
    primary = Color(0xFFC6A36C),
    onPrimary = Color(0xFF22170C),
    primaryContainer = Color(0xFF49321A),
    onPrimaryContainer = Color(0xFFF3DDB8),
    secondary = Color(0xFF9E7B5A),
    onSecondary = Color(0xFF21150C),
    background = Color(0xFF120E18),
    onBackground = Color(0xFFF0E7DA),
    surface = Color(0xFF1A1521),
    onSurface = Color(0xFFF0E7DA),
    surfaceVariant = Color(0xFF2A2231),
    onSurfaceVariant = Color(0xFFCABFB3),
    outline = Color(0xFF706359),
    outlineVariant = Color(0xFF423830)
)

private val DnDShapes = Shapes()

@Composable
fun DnDTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val designTokens = remember(context) { loadDesignTokens(context) }

    CompositionLocalProvider(
        LocalDnDSpacing provides DnDSpacing(),
        LocalDesignTokens provides designTokens
    ) {
        MaterialTheme(
            colorScheme = DnDColors,
            typography = buildDnDTypography(designTokens.typography),
            shapes = DnDShapes,
            content = content
        )
    }
}

private fun buildDnDTypography(tokens: DesignTypographyTokens): Typography {
    return Typography(
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = tokens.headlineMedium.fontSizeSp.sp,
            lineHeight = (tokens.headlineMedium.lineHeightSp ?: tokens.headlineMedium.fontSizeSp).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = tokens.titleLarge.fontSizeSp.sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.SemiBold,
            fontSize = tokens.titleMedium.fontSizeSp.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = tokens.bodyLarge.fontSizeSp.sp,
            lineHeight = (tokens.bodyLarge.lineHeightSp ?: tokens.bodyLarge.fontSizeSp).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontSize = tokens.bodyMedium.fontSizeSp.sp,
            lineHeight = (tokens.bodyMedium.lineHeightSp ?: tokens.bodyMedium.fontSizeSp).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = tokens.labelMedium.fontSizeSp.sp
        )
    )
}
