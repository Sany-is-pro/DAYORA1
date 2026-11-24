package com.example.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect as AndroidRect
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalDayoraColors
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatarCropDialog(
  sourceUri: Uri,
  onDismiss: () -> Unit,
  onCropped: (Bitmap) -> Unit,
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  val colors = LocalDayoraColors.current
  val scope = rememberCoroutineScope()

  var workingBitmap by remember(sourceUri) { mutableStateOf<Bitmap?>(null) }
  var isLoading by remember(sourceUri) { mutableStateOf(true) }
  var loadError by remember(sourceUri) { mutableStateOf(false) }
  var isSaving by remember { mutableStateOf(false) }

  var zoom by remember(sourceUri) { mutableFloatStateOf(1f) }
  var offsetX by remember(sourceUri) { mutableFloatStateOf(0f) }
  var offsetY by remember(sourceUri) { mutableFloatStateOf(0f) }
  var viewportPx by remember { mutableFloatStateOf(0f) }

  LaunchedEffect(sourceUri) {
    isLoading = true
    loadError = false
    val decoded = withContext(Dispatchers.IO) {
      loadOrientedBitmapFromUri(context, sourceUri)
    }
    if (decoded != null) {
      workingBitmap = decoded
    } else {
      loadError = true
    }
    isLoading = false
  }

  fun clampOffset(bmp: Bitmap, targetZoom: Float, rawX: Float, rawY: Float): Pair<Float, Float> {
    if (viewportPx <= 0f) return 0f to 0f
    val circleDiameter = viewportPx * 0.90f
    val bmpW = bmp.width.toFloat()
    val bmpH = bmp.height.toFloat()
    val baseScale = circleDiameter / min(bmpW, bmpH)
    val scale = baseScale * targetZoom
    val maxTx = ((bmpW * scale - circleDiameter) / 2f).coerceAtLeast(0f)
    val maxTy = ((bmpH * scale - circleDiameter) / 2f).coerceAtLeast(0f)
    return rawX.coerceIn(-maxTx, maxTx) to rawY.coerceIn(-maxTy, maxTy)
  }

  Dialog(
    onDismissRequest = { if (!isSaving) onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 520.dp)
        .padding(horizontal = 16.dp, vertical = 20.dp),
      shape = RoundedCornerShape(28.dp),
      color = colors.popupSurface,
      border = BorderStroke(1.5.dp, colors.primaryAccent.copy(alpha = 0.35f)),
      shadowElevation = 12.dp,
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 18.dp),
      ) {
        // Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(colors.lavenderTint)
            .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = strings.cropPhotoTitle,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = colors.mainText,
            )
            Text(
              text = strings.cropPhotoHint,
              style = MaterialTheme.typography.bodySmall,
              color = colors.secondaryText,
            )
          }
          IconButton(
            onClick = { if (!isSaving) onDismiss() },
            modifier = Modifier.minimumInteractiveComponentSize(),
          ) {
            Icon(Icons.Rounded.Close, contentDescription = strings.closeLabel, tint = colors.mainText)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Crop Preview Area
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF14101D))
            .onSizeChanged { viewportPx = it.width.toFloat() },
          contentAlignment = Alignment.Center,
        ) {
          when {
            isLoading -> {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = colors.primaryAccent)
                Spacer(modifier = Modifier.height(10.dp))
                Text(strings.loadingImageLabel, color = Color.White, style = MaterialTheme.typography.bodySmall)
              }
            }

            loadError || workingBitmap == null -> {
              Text(
                text = strings.imageLoadError,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
              )
            }

            else -> {
              val bmp = workingBitmap!!
              val imageBmp = remember(bmp) { bmp.asImageBitmap() }

              Canvas(
                modifier = Modifier
                  .fillMaxSize()
                  .pointerInput(bmp) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                      val nextZoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                      val (cx, cy) = clampOffset(bmp, nextZoom, offsetX + pan.x, offsetY + pan.y)
                      zoom = nextZoom
                      offsetX = cx
                      offsetY = cy
                    }
                  },
              ) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val radius = w * 0.45f
                val diameter = radius * 2f

                val bmpW = bmp.width.toFloat()
                val bmpH = bmp.height.toFloat()
                val baseScale = diameter / min(bmpW, bmpH)
                val scale = baseScale * zoom
                val dispW = bmpW * scale
                val dispH = bmpH * scale

                val maxTx = ((dispW - diameter) / 2f).coerceAtLeast(0f)
                val maxTy = ((dispH - diameter) / 2f).coerceAtLeast(0f)
                val tx = offsetX.coerceIn(-maxTx, maxTx)
                val ty = offsetY.coerceIn(-maxTy, maxTy)

                val dstLeft = cx + tx - dispW / 2f
                val dstTop = cy + ty - dispH / 2f

                drawImage(
                  image = imageBmp,
                  dstOffset = IntOffset(dstLeft.roundToInt(), dstTop.roundToInt()),
                  dstSize = IntSize(dispW.roundToInt().coerceAtLeast(1), dispH.roundToInt().coerceAtLeast(1)),
                )

                // Dim outside the circular crop preview
                val maskPath = Path().apply {
                  fillType = PathFillType.EvenOdd
                  addRect(Rect(0f, 0f, w, h))
                  addOval(Rect(cx - radius, cy - radius, cx + radius, cy + radius))
                }
                drawPath(path = maskPath, color = Color.Black.copy(alpha = 0.58f))

                // Subtle framing circle border
                drawCircle(
                  color = Color.White.copy(alpha = 0.9f),
                  radius = radius,
                  center = Offset(cx, cy),
                  style = Stroke(width = 2.5.dp.toPx()),
                )
              }
            }
          }
        }

        if (workingBitmap != null && !isLoading) {
          Spacer(modifier = Modifier.height(12.dp))

          // Zoom Slider
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.ZoomIn,
              contentDescription = strings.zoomLabel,
              tint = colors.primaryAccent,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Slider(
              value = zoom,
              onValueChange = { nextZoom ->
                val bmp = workingBitmap ?: return@Slider
                val (cx, cy) = clampOffset(bmp, nextZoom, offsetX, offsetY)
                zoom = nextZoom
                offsetX = cx
                offsetY = cy
              },
              valueRange = 1f..4f,
              modifier = Modifier
                .weight(1f)
                .testTag("avatar_crop_zoom_slider"),
            )
            Text(
              text = "${"%.1f".format(zoom)}x",
              style = MaterialTheme.typography.labelMedium,
              color = colors.secondaryText,
              modifier = Modifier.padding(start = 6.dp),
            )
          }

          // Rotate & Reset Actions
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = {
                val bmp = workingBitmap ?: return@OutlinedButton
                workingBitmap = rotateBitmap90(bmp)
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
              },
              enabled = !isSaving,
              modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("avatar_crop_rotate_button"),
              shape = RoundedCornerShape(16.dp),
            ) {
              Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.rotateLabel)
            }
            OutlinedButton(
              onClick = {
                zoom = 1f
                offsetX = 0f
                offsetY = 0f
              },
              enabled = !isSaving,
              modifier = Modifier
                .weight(1f)
                .minimumInteractiveComponentSize()
                .testTag("avatar_crop_reset_button"),
              shape = RoundedCornerShape(16.dp),
            ) {
              Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.resetCropLabel)
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cancel & Save Buttons
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          TextButton(
            onClick = onDismiss,
            enabled = !isSaving,
            modifier = Modifier
              .weight(1f)
              .minimumInteractiveComponentSize(),
          ) {
            Text(strings.cancel)
          }
          Button(
            onClick = {
              val bmp = workingBitmap ?: return@Button
              if (viewportPx <= 0f || isSaving) return@Button
              isSaving = true
              scope.launch {
                val cropped = withContext(Dispatchers.Default) {
                  cropSquareAvatarBitmap(
                    source = bmp,
                    zoom = zoom,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    viewportSizePx = viewportPx,
                  )
                }
                isSaving = false
                onCropped(cropped)
              }
            },
            enabled = workingBitmap != null && !isLoading && !isSaving,
            modifier = Modifier
              .weight(1.4f)
              .minimumInteractiveComponentSize()
              .testTag("avatar_crop_save_button"),
            shape = RoundedCornerShape(18.dp),
          ) {
            if (isSaving) {
              CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
              )
            } else {
              Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.applyCropLabel)
            }
          }
        }
      }
    }
  }
}

private fun loadOrientedBitmapFromUri(context: Context, uri: Uri, maxDim: Int = 1440): Bitmap? {
  return runCatching {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, bounds)
    } ?: return null

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    var sampleSize = 1
    var longest = max(bounds.outWidth, bounds.outHeight)
    while (longest / sampleSize > maxDim) {
      sampleSize *= 2
    }

    val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
      ExifInterface(stream).getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL,
      )
    } ?: ExifInterface.ORIENTATION_NORMAL

    val decodeOpts = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val raw = context.contentResolver.openInputStream(uri)?.use { stream ->
      BitmapFactory.decodeStream(stream, null, decodeOpts)
    } ?: return null

    val matrix = Matrix()
    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
      ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
      ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
      ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
      ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
      ExifInterface.ORIENTATION_TRANSPOSE -> {
        matrix.postRotate(90f)
        matrix.postScale(-1f, 1f)
      }
      ExifInterface.ORIENTATION_TRANSVERSE -> {
        matrix.postRotate(270f)
        matrix.postScale(-1f, 1f)
      }
    }

    if (matrix.isIdentity) {
      raw
    } else {
      val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
      if (rotated !== raw) raw.recycle()
      rotated
    }
  }.getOrNull()
}

private fun rotateBitmap90(source: Bitmap): Bitmap {
  val matrix = Matrix().apply { postRotate(90f) }
  return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

internal fun cropSquareAvatarBitmap(
  source: Bitmap,
  zoom: Float,
  offsetX: Float,
  offsetY: Float,
  viewportSizePx: Float,
  outSize: Int = 512,
): Bitmap {
  val radius = viewportSizePx * 0.45f
  val diameter = radius * 2f
  val bmpW = source.width.toFloat()
  val bmpH = source.height.toFloat()
  val baseScale = diameter / min(bmpW, bmpH)
  val scale = baseScale * zoom.coerceIn(1f, 4f)
  val dispW = bmpW * scale
  val dispH = bmpH * scale

  val maxTx = ((dispW - diameter) / 2f).coerceAtLeast(0f)
  val maxTy = ((dispH - diameter) / 2f).coerceAtLeast(0f)
  val tx = offsetX.coerceIn(-maxTx, maxTx)
  val ty = offsetY.coerceIn(-maxTy, maxTy)

  val srcSize = (diameter / scale).coerceAtLeast(1f)
  val srcLeft = ((-radius - tx) / scale + bmpW / 2f).coerceIn(0f, (bmpW - srcSize).coerceAtLeast(0f))
  val srcTop = ((-radius - ty) / scale + bmpH / 2f).coerceIn(0f, (bmpH - srcSize).coerceAtLeast(0f))

  val leftPx = srcLeft.roundToInt().coerceIn(0, source.width - 1)
  val topPx = srcTop.roundToInt().coerceIn(0, source.height - 1)
  val rightPx = (srcLeft + srcSize).roundToInt().coerceIn(leftPx + 1, source.width)
  val bottomPx = (srcTop + srcSize).roundToInt().coerceIn(topPx + 1, source.height)

  val output = Bitmap.createBitmap(outSize, outSize, Bitmap.Config.ARGB_8888)
  val canvas = AndroidCanvas(output)
  val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
  canvas.drawBitmap(
    source,
    AndroidRect(leftPx, topPx, rightPx, bottomPx),
    AndroidRect(0, 0, outSize, outSize),
    paint,
  )
  return output
}
