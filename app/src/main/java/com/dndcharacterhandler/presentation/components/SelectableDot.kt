package com.dndcharacterhandler.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A toggleable dot: a filled inner circle inside a ring when [selected], an empty ring otherwise.
 * Used for inventory "equipped" and spells "prepared" toggles.
 */
@Composable
fun SelectableDot(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(22.dp)
            .clickable(onClick = onClick)
    ) {
        drawCircle(
            color = if (selected) Color(0xFFF7F2EA) else Color.Transparent,
            radius = size.minDimension * 0.32f
        )
        drawCircle(
            color = Color(0xFFC2BBB3),
            radius = size.minDimension * 0.42f,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}
