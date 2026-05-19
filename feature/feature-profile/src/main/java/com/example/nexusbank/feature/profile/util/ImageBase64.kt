package com.example.nexusbank.feature.profile.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private const val TAG = "ImageBase64"

/**
 * Loads an image from [uri], scales it so the longest side is at most
 * [maxDimension] px, compresses to JPEG at [quality], and returns
 * a Base64 `data:image/jpeg;base64,...` string.
 *
 * Returns `null` only if decoding completely fails.
 */
suspend fun encodeImageAsBase64DataUri(
    context: Context,
    uri: Uri,
    maxDimension: Int = 512,
    quality: Int = 75
): String? = withContext(Dispatchers.IO) {
    val source = decodeBitmapFromUri(context, uri) ?: return@withContext null

    val scaled = if (maxOf(source.width, source.height) > maxDimension) {
        val ratio = maxDimension.toFloat() / maxOf(source.width, source.height)
        val w = (source.width * ratio).toInt().coerceAtLeast(1)
        val h = (source.height * ratio).toInt().coerceAtLeast(1)
        Bitmap.createScaledBitmap(source, w, h, true).also {
            if (it !== source) source.recycle()
        }
    } else source

    // Hardware bitmaps can't be compressed — copy to a software bitmap if needed.
    val safe = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
        scaled.config == Bitmap.Config.HARDWARE
    ) {
        scaled.copy(Bitmap.Config.ARGB_8888, false).also { scaled.recycle() }
    } else scaled

    val baos = ByteArrayOutputStream()
    val ok = safe.compress(Bitmap.CompressFormat.JPEG, quality, baos)
    safe.recycle()
    if (!ok) {
        Log.w(TAG, "Bitmap.compress returned false")
        return@withContext null
    }

    val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
    Log.d(TAG, "Encoded: jpegBytes=${baos.size()} base64Len=${base64.length}")
    "data:image/jpeg;base64,$base64"
}

/**
 * Decodes [uri] into a software [Bitmap]. Uses [ImageDecoder] on API 28+
 * (works reliably with Photo Picker URIs) and falls back to [BitmapFactory].
 */
private fun decodeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        try {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "ImageDecoder failed for $uri: ${e.message}", e)
        }
    }
    return try {
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it)
        }
    } catch (e: Exception) {
        Log.e(TAG, "BitmapFactory fallback failed for $uri: ${e.message}", e)
        null
    }
}

/**
 * Decodes a `data:image/...;base64,...` URI back into a [Bitmap].
 * Returns `null` if [dataUri] is not a base64 data URI.
 */
fun decodeBase64DataUri(dataUri: String): Bitmap? {
    if (!dataUri.startsWith("data:")) return null
    val commaIndex = dataUri.indexOf(',')
    if (commaIndex < 0) return null
    val base64 = dataUri.substring(commaIndex + 1)
    return try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (_: Exception) {
        null
    }
}
