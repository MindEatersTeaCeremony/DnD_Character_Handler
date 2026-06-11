package com.dndcharacterhandler.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dndcharacterhandler.domain.model.AssetReferences
import com.dndcharacterhandler.domain.model.Character
import com.dndcharacterhandler.presentation.localization.text
import com.dndcharacterhandler.presentation.theme.LocalDesignTokens

@Composable
fun CharacterScreenHeader(
    character: Character,
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalDesignTokens.current.typography
    val portraitReference = character.portraitUri ?: AssetReferences.portraitPlaceholderPath("portrait_placeholder.png")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
    ) {
        ScreenTopActions(
            onOpenDrawer = onOpenDrawer,
            onOpenSettings = onOpenSettings,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(start = 52.dp, end = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(70.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0x55A19892),
                        radius = size.minDimension / 2f - 4.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                    drawCircle(
                        color = Color(0x42FFFFFF),
                        radius = size.minDimension / 2f - 9.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    shape = CircleShape,
                    color = Color(0xFF141118)
                ) {
                    AppImage(
                        imageRef = portraitReference,
                        contentDescription = character.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        fallback = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2D2730)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = character.name.take(1).ifBlank { "?" },
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = tokens.portraitInitial.fontSizeSp.sp
                                    ),
                                    color = Color(0xFFF7F2EA)
                                )
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = character.name.ifBlank { text("placeholder_loading_character") },
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = tokens.titleLarge.fontSizeSp.sp),
                    color = Color(0xFFF7F2EA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = buildCharacterHeaderSubtitle(character),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = tokens.bodyLarge.fontSizeSp.sp),
                    color = Color(0xFFD2CAC2),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun buildCharacterHeaderSubtitle(character: Character): String =
    listOfNotNull(
        character.race.ifBlank { null },
        character.subclass.ifBlank { null },
        character.characterClass.ifBlank { null },
        "Level ${character.level}"
    ).joinToString(" • ")
