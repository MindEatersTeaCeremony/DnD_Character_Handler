package com.dndcharacterhandler.presentation.overview

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.DirectionsRun
import androidx.compose.material.icons.outlined.FlashOn
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dndcharacterhandler.data.localization.LocalizedStrings
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.domain.model.AppLanguage
import com.dndcharacterhandler.domain.usecase.GetCharacterBundleUseCase
import com.dndcharacterhandler.presentation.BaseCharacterViewModel
import com.dndcharacterhandler.presentation.SelectedCharacterHolder
import com.dndcharacterhandler.presentation.localization.LocalStrings
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.theme.DnDTheme
import java.io.File
import java.io.FileInputStream
import java.text.NumberFormat

class OverviewViewModel(
    getCharacterBundleUseCase: GetCharacterBundleUseCase,
    selectedCharacterHolder: SelectedCharacterHolder
) : BaseCharacterViewModel(getCharacterBundleUseCase, selectedCharacterHolder)

private data class OverviewAction(
    val labelKey: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

private data class OverviewStat(
    val labelKey: String,
    val value: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun OverviewScreen(
    viewModel: OverviewViewModel,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    OverviewContent(
        character = state.character?.character,
        onOpenDrawer = onOpenDrawer,
        onOpenSettings = onOpenSettings
    )
}

@Composable
private fun OverviewContent(
    character: Character?,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val strings = LocalStrings.current
    val displayName = character?.name?.ifBlank { text("overview_name_placeholder") }
        ?: text("overview_name_placeholder")
    val subtitle = remember(character, strings) { buildOverviewSubtitle(character, strings) }
    val hpValue = "${character?.currentHp ?: 0} / ${character?.maxHp ?: 0}"
    val xpInfo = remember(character) { buildXpInfo(character) }

    val actions = listOf(
        OverviewAction("overview_short_rest", Icons.Outlined.LocalCafe),
        OverviewAction("overview_long_rest", Icons.Outlined.Bedtime),
        OverviewAction("overview_inspiration", Icons.Outlined.AutoAwesome)
    )

    val miniStats = listOf(
        OverviewStat("overview_ac", (character?.armorClass ?: 0).toString(), Icons.Outlined.Shield),
        OverviewStat("overview_initiative", signed(character?.initiative), Icons.Outlined.FlashOn),
        OverviewStat("overview_speed", "${character?.speed ?: 0} ft", Icons.Outlined.DirectionsRun)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF1A161D), Color(0xFF0E0B11), Color(0xFF09070D)),
                    radius = 1600f
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                OverviewHeaderRow(
                    onOpenDrawer = onOpenDrawer,
                    onOpenSettings = onOpenSettings
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PortraitFrame(
                        portraitUri = character?.portraitUri,
                        characterName = displayName
                    )
                    Text(
                        text = displayName,
                        modifier = Modifier.offset(y = (-36).dp),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            lineHeight = 35.sp
                        ),
                        color = Color(0xFFF7F2EA),
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                    Text(
                        text = subtitle,
                        modifier = Modifier
                            .offset(y = (-34).dp)
                            .padding(top = 2.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                        color = Color(0xFFAAA29A),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    actions.forEach { action ->
                        OverviewActionButton(
                            modifier = Modifier.weight(1f),
                            label = text(action.labelKey),
                            icon = action.icon
                        )
                    }
                }
            }

            item {
                Box(modifier = Modifier.offset(y = (-28).dp)) {
                    OverviewXpBlock(xpInfo = xpInfo)
                }
            }

            item {
                Box(modifier = Modifier.offset(y = (-30).dp)) {
                    OverviewHpCard(
                        hpValue = hpValue,
                        hpLabel = text("overview_hp")
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-34).dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    miniStats.forEach { stat ->
                        OverviewMiniStatCard(
                            modifier = Modifier.weight(1f),
                            value = stat.value,
                            label = text(stat.labelKey),
                            icon = stat.icon
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewHeaderRow(
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(onClick = onOpenDrawer) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = text("drawer_open_character_manager"),
                tint = Color(0xFFF3EEE6),
                modifier = Modifier.size(28.dp)
            )
        }
        HeaderIconButton(onClick = onOpenSettings) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = text("overview_settings"),
                tint = Color(0xFFF3EEE6),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun PortraitFrame(
    portraitUri: String?,
    characterName: String
) {
    val isPreview = LocalInspectionMode.current
    val context = LocalContext.current
    val bitmap = remember(portraitUri, isPreview) {
        if (isPreview) {
            null
        } else {
        portraitUri?.let { rawUri ->
            runCatching {
                when {
                    rawUri.startsWith("content://") || rawUri.startsWith("file://") -> {
                        context.contentResolver.openInputStream(Uri.parse(rawUri)).use(BitmapFactory::decodeStream)
                    }

                    File(rawUri).exists() -> {
                        FileInputStream(rawUri).use(BitmapFactory::decodeStream)
                    }

                    else -> null
                }
            }.getOrNull()
        }
        }
    }

    Box(
        modifier = Modifier
            .offset(y = (-47).dp)
            .padding(bottom = 0.dp)
            .size(238.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = center
            val outer = size.minDimension / 2f - 8.dp.toPx()
            val middle = outer - 12.dp.toPx()
            val inner = outer - 26.dp.toPx()

            drawCircle(
                color = Color(0x20FFFFFF),
                radius = outer + 8.dp.toPx(),
                center = c,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0x80C7C1BB),
                radius = outer,
                center = c,
                style = Stroke(width = 3.dp.toPx())
            )
            drawCircle(
                color = Color(0x42FFFFFF),
                radius = middle,
                center = c,
                style = Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Color(0xFFE9E2D9),
                radius = inner,
                center = c,
                style = Stroke(width = 2.dp.toPx())
            )

            val ornamentRadius = outer + 2.dp.toPx()
            listOf(
                Offset(c.x, c.y - ornamentRadius),
                Offset(c.x + ornamentRadius, c.y),
                Offset(c.x, c.y + ornamentRadius),
                Offset(c.x - ornamentRadius, c.y)
            ).forEachIndexed { index, offset ->
                val half = if (index % 2 == 0) 10.dp.toPx() else 12.dp.toPx()
                val diamond = Path().apply {
                    moveTo(offset.x, offset.y - half)
                    lineTo(offset.x + half, offset.y)
                    lineTo(offset.x, offset.y + half)
                    lineTo(offset.x - half, offset.y)
                    close()
                }
                drawPath(diamond, color = Color(0x14000000))
                drawPath(diamond, color = Color(0x55A19892), style = Stroke(width = 1.dp.toPx()))
                drawCircle(
                    color = Color(0xFF2D2730),
                    radius = 5.dp.toPx(),
                    center = offset
                )
            }
        }

        Surface(
            modifier = Modifier
                .size(188.dp)
                .clip(CircleShape),
            shape = CircleShape,
            color = Color(0xFF141118)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = characterName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF3B3840), Color(0xFF18151C), Color(0xFF0F0C12))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = characterName.take(1).ifBlank { "?" },
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 40.sp),
                        color = Color(0xFFF7F2EA)
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewActionButton(
        modifier: Modifier = Modifier,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        modifier = modifier.height(66.dp),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color(0x50FFFFFF)),
        color = Color(0xFF1A171D)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFF1ECE5),
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = label,
                modifier = Modifier.padding(start = 7.dp),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 11.5.sp,
                    lineHeight = 13.sp
                ),
                color = Color(0xFFF1ECE5),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OverviewXpBlock(xpInfo: XpProgressInfo) {
    val formatter = remember { NumberFormat.getIntegerInstance() }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "${text("overview_xp")} ${formatter.format(xpInfo.currentXp)} / ${formatter.format(xpInfo.nextLevelXp)}",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            color = Color(0xFFECE4DB)
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
        ) {
            val stroke = 11.dp.toPx()
            drawLine(
                color = Color(0x30FFFFFF),
                start = Offset(stroke / 2, center.y),
                end = Offset(size.width - stroke / 2, center.y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFD7D1CC),
                start = Offset(stroke / 2, center.y),
                end = Offset((size.width - stroke) * xpInfo.progress + stroke / 2, center.y),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun OverviewHpCard(
    hpValue: String,
    hpLabel: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFF17141B),
        border = BorderStroke(1.dp, Color(0x44FFFFFF))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp)
        ) {
            FrameCorner(modifier = Modifier.align(Alignment.TopStart))
            FrameCorner(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp),
                mirrored = true
            )
            FrameCorner(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(y = 6.dp),
                upsideDown = true
            )
            FrameCorner(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 6.dp, y = 6.dp),
                mirrored = true,
                upsideDown = true
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = hpValue,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 64.sp,
                        lineHeight = 68.sp
                    ),
                    color = Color(0xFFF7F2EA),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = hpLabel,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                    color = Color(0xFFC2BBB3)
                )
            }
        }
    }
}

@Composable
private fun OverviewMiniStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier.aspectRatio(0.92f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0x42FFFFFF)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF17141B))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            FrameCorner(modifier = Modifier.align(Alignment.TopStart))
            FrameCorner(
                modifier = Modifier.align(Alignment.TopEnd),
                mirrored = true
            )
            FrameCorner(
                modifier = Modifier.align(Alignment.BottomStart),
                upsideDown = true
            )
            FrameCorner(
                modifier = Modifier.align(Alignment.BottomEnd),
                mirrored = true,
                upsideDown = true
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFD8D1CA),
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(modifier = Modifier.size(width = 48.dp, height = 8.dp)) {
                    drawLine(
                        color = Color(0x38FFFFFF),
                        start = Offset(0f, center.y),
                        end = Offset(size.width, center.y),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        lineHeight = 30.sp
                    ),
                    color = Color(0xFFF7F2EA),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = Color(0xFFBEB6AE),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FrameCorner(
    modifier: Modifier = Modifier,
    mirrored: Boolean = false,
    upsideDown: Boolean = false
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val left = if (mirrored) size.width else 0f
        val right = if (mirrored) size.width * 0.28f else size.width * 0.72f
        val top = if (upsideDown) size.height else 0f
        val bottom = if (upsideDown) size.height * 0.28f else size.height * 0.72f
        val verticalNear = if (upsideDown) size.height * 0.8f else size.height * 0.2f
        val horizontalNear = if (mirrored) size.width * 0.8f else size.width * 0.2f

        drawLine(
            color = Color(0x46FFFFFF),
            start = Offset(left, bottom),
            end = Offset(right, top),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = Color(0x46FFFFFF),
            start = Offset(left, verticalNear),
            end = Offset(horizontalNear, top),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private data class XpProgressInfo(
    val currentXp: Int,
    val nextLevelXp: Int,
    val progress: Float
)

private fun buildOverviewSubtitle(
    character: Character?,
    strings: LocalizedStrings
): String {
    val race = character?.race?.ifBlank { strings["placeholder_race"] } ?: strings["placeholder_race"]
    val classLabel = character?.characterClass?.ifBlank { strings["placeholder_class"] } ?: strings["placeholder_class"]
    val subclass = character?.subclass?.ifBlank { null }
    val classLine = if (subclass != null) "$subclass $classLabel" else classLabel
    return strings.format("overview_subtitle_format", race, classLine, character?.level ?: 1)
}

private fun buildXpInfo(character: Character?): XpProgressInfo {
    val currentXp = character?.experience ?: 0
    val level = character?.level ?: 1
    val currentThreshold = levelThreshold(level)
    val nextThreshold = nextLevelThreshold(level)
    val range = (nextThreshold - currentThreshold).coerceAtLeast(1)
    val progress = if (level >= 20) {
        1f
    } else {
        ((currentXp - currentThreshold).coerceAtLeast(0).toFloat() / range.toFloat()).coerceIn(0f, 1f)
    }

    return XpProgressInfo(
        currentXp = currentXp,
        nextLevelXp = nextThreshold,
        progress = progress
    )
}

private fun levelThreshold(level: Int): Int {
    val thresholds = mapOf(
        1 to 0,
        2 to 300,
        3 to 900,
        4 to 2700,
        5 to 6500,
        6 to 14000,
        7 to 23000,
        8 to 34000,
        9 to 48000,
        10 to 64000,
        11 to 85000,
        12 to 100000,
        13 to 120000,
        14 to 140000,
        15 to 165000,
        16 to 195000,
        17 to 225000,
        18 to 265000,
        19 to 305000,
        20 to 355000
    )
    return thresholds[level.coerceIn(1, 20)] ?: 0
}

private fun nextLevelThreshold(level: Int): Int {
    return if (level >= 20) 355000 else levelThreshold(level + 1)
}

private fun signed(value: Int?): String {
    if (value == null) return "-"
    return if (value >= 0) "+$value" else value.toString()
}

@Preview(showBackground = true, showSystemUi = true, device = "spec:width=412dp,height=915dp")
@Composable
private fun OverviewScreenPreview() {
    val previewStrings = LocalizedStrings(
        language = AppLanguage.ENGLISH,
        values = mapOf(
            "overview_name_placeholder" to "Unnamed Adventurer",
            "placeholder_race" to "Human",
            "placeholder_class" to "Wizard",
            "overview_subtitle_format" to "%1\$s • %2\$s • Level %3\$s",
            "overview_short_rest" to "Short Rest",
            "overview_long_rest" to "Long Rest",
            "overview_inspiration" to "Inspiration",
            "overview_xp" to "EXP",
            "overview_hp" to "HP",
            "overview_ac" to "AC",
            "overview_initiative" to "Initiative",
            "overview_speed" to "Speed",
            "overview_settings" to "Settings",
            "drawer_open_character_manager" to "Open character manager"
        )
    )
    val previewCharacter = Character(
        id = 1,
        name = "Alaric Stormwind",
        race = "Human",
        characterClass = "Wizard",
        subclass = "Divination",
        level = 7,
        portraitUri = null,
        currentHp = 38,
        maxHp = 42,
        armorClass = 15,
        speed = 30,
        initiative = 3,
        experience = 23500,
        strength = 8,
        dexterity = 16,
        constitution = 14,
        intelligence = 18,
        wisdom = 14,
        charisma = 12,
        alignment = "",
        background = "",
        faith = "",
        homeland = "",
        age = "",
        gender = "",
        height = "",
        weight = "",
        eyes = "",
        hair = "",
        skin = "",
        personalityTraits = "",
        ideals = "",
        bonds = "",
        flaws = "",
        biography = "",
        createdAt = 0L,
        updatedAt = 0L
    )

    CompositionLocalProvider(LocalStrings provides previewStrings) {
        DnDTheme {
            OverviewContent(
                character = previewCharacter,
                onOpenDrawer = {},
                onOpenSettings = {}
            )
        }
    }
}
