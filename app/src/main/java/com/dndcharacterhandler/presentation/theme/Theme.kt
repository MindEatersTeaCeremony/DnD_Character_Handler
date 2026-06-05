package com.dndcharacterhandler.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

private val DnDTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)

private val DnDShapes = Shapes()

@Composable
fun DnDTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalDnDSpacing provides DnDSpacing()) {
        MaterialTheme(
            colorScheme = DnDColors,
            typography = DnDTypography,
            shapes = DnDShapes,
            content = content
        )
    }
}
