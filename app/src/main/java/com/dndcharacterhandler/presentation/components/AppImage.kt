package com.dndcharacterhandler.presentation.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.dndcharacterhandler.domain.model.AssetReferences
import java.io.File
import java.io.FileInputStream

private sealed interface ResolvedImageSource {
    data class Drawable(@DrawableRes val resId: Int) : ResolvedImageSource
    data class Bitmap(val painter: BitmapPainter) : ResolvedImageSource
}

@Composable
fun AppImage(
    imageRef: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallback: @Composable (() -> Unit)? = null
) {
    val context = LocalContext.current
    val resolved = remember(imageRef) {
        resolveImageSource(
            rawValue = imageRef,
            drawableResolver = { name ->
                context.resources.getIdentifier(name, "drawable", context.packageName)
            },
            openBitmap = { reference ->
                when {
                    reference.startsWith("content://") || reference.startsWith("file://") -> {
                        context.contentResolver.openInputStream(Uri.parse(reference)).use(BitmapFactory::decodeStream)
                    }

                    reference.startsWith("${AssetReferences.iconsRoot}/") ||
                        reference.startsWith("${AssetReferences.portraitsRoot}/") -> {
                        context.assets.open(reference).use(BitmapFactory::decodeStream)
                    }

                    File(reference).exists() -> {
                        FileInputStream(reference).use(BitmapFactory::decodeStream)
                    }

                    else -> null
                }
            }
        )
    }

    when (resolved) {
        is ResolvedImageSource.Drawable -> {
            Image(
                painter = painterResource(id = resolved.resId),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }

        is ResolvedImageSource.Bitmap -> {
            Image(
                painter = resolved.painter,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }

        null -> {
            if (fallback != null) {
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    fallback()
                }
            }
        }
    }
}

private fun resolveImageSource(
    rawValue: String?,
    drawableResolver: (String) -> Int,
    openBitmap: (String) -> android.graphics.Bitmap?
): ResolvedImageSource? {
    val reference = rawValue?.trim().orEmpty()
    if (reference.isEmpty()) return null

    val drawableName = when {
        reference.startsWith(AssetReferences.drawableReferencePrefix) -> {
            reference.removePrefix(AssetReferences.drawableReferencePrefix)
        }

        reference.startsWith("res:drawable/") -> {
            reference.removePrefix("res:drawable/")
        }

        !reference.contains('/') && !reference.contains(':') && !reference.contains('\\') -> {
            reference
        }

        else -> null
    }

    if (!drawableName.isNullOrBlank()) {
        val resId = drawableResolver(drawableName)
        if (resId != 0) {
            return ResolvedImageSource.Drawable(resId)
        }
    }

    val bitmap = runCatching { openBitmap(reference) }.getOrNull() ?: return null
    return ResolvedImageSource.Bitmap(BitmapPainter(bitmap.asImageBitmap()))
}
