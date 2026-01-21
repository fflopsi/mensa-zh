package ch.florianfrauenfelder.mensazh.ui.main.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageDialog(
  show: MutableState<Boolean>,
  painter: Painter,
  modifier: Modifier = Modifier,
) {
  if (show.value) {
    var scale by remember { mutableFloatStateOf(1.5f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val ratio by remember(painter.intrinsicSize.width, painter.intrinsicSize.height) {
      derivedStateOf {
        if (painter.intrinsicSize.width.isNaN() || painter.intrinsicSize.height.isNaN()) {
          Float.POSITIVE_INFINITY
        } else {
          painter.intrinsicSize.width / painter.intrinsicSize.height
        }
      }
    }

    BasicAlertDialog(
      onDismissRequest = { show.value = false },
      properties = DialogProperties(),
      modifier = modifier.pointerInput(Unit) { detectTapGestures { show.value = false } },
    ) {
      BoxWithConstraints(
        modifier = Modifier
          .aspectRatio(ratio)
          .fillMaxWidth(),
      ) {
        val state = rememberTransformableState { zoomChange, panChange, _ ->
          scale = (scale * zoomChange).coerceIn(1f..5f)
          val extraWidth = (scale - 1) * constraints.maxWidth
          val extraHeight = (scale - 1) * constraints.maxHeight
          val maxX = extraWidth / 2
          val maxY = extraHeight / 2
          offset = Offset(
            x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
            y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY),
          )
        }
        Image(
          painter = painter,
          contentDescription = null,
          modifier = Modifier
            .graphicsLayer(
              scaleX = scale,
              scaleY = scale,
              translationX = offset.x,
              translationY = offset.y,
            )
            .transformable(state)
            .pointerInput(Unit) {
              detectTapGestures(
                onDoubleTap = {
                  scale = if (scale >= 2.5f) 1.5f else 3.0f
                  offset = Offset.Zero
                },
              )
            }
            .fillMaxWidth(),
        )
      }
    }
  }
}
