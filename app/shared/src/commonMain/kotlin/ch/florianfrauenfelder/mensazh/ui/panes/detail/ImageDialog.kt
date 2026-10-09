package ch.florianfrauenfelder.mensazh.ui.panes.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.ui.theme.myMotionSpec
import kotlinx.coroutines.launch

@Composable
fun ImageDialog(
  show: MutableState<Boolean>,
  painter: Painter,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val floatMotionSpec = myMotionSpec<Float>()
  val offsetMotionSpec = myMotionSpec<Offset>()

  if (show.value) {
    val scale = retain { Animatable(1.5f) }
    val offset = retain { Animatable(Offset.Zero, Offset.VectorConverter) }
    val ratio by retain(painter.intrinsicSize.width, painter.intrinsicSize.height) {
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
      modifier = modifier
        .clip(RoundedCornerShape(8.dp))
        .pointerInput(Unit) { detectTapGestures { show.value = false } },
    ) {
      BoxWithConstraints(
        modifier = Modifier
          .aspectRatio(ratio)
          .fillMaxWidth(),
      ) {
        val state = rememberTransformableState { _, zoomChange, panChange, _ ->
          scope.launch {
            scale.snapTo((scale.value * zoomChange).coerceIn(1f..5f))
            val extraWidth = (scale.value - 1) * constraints.maxWidth
            val extraHeight = (scale.value - 1) * constraints.maxHeight
            val maxX = extraWidth / 2
            val maxY = extraHeight / 2
            offset.snapTo(
              Offset(
                x = (offset.value.x + scale.value * panChange.x).coerceIn(-maxX, maxX),
                y = (offset.value.y + scale.value * panChange.y).coerceIn(-maxY, maxY),
              ),
            )
          }
        }
        Image(
          painter = painter,
          contentDescription = null,
          modifier = Modifier
            .graphicsLayer {
              scaleX = scale.value
              scaleY = scale.value
              translationX = offset.value.x
              translationY = offset.value.y
            }
            .transformable(state)
            .pointerInput(Unit) {
              detectTapGestures(
                onDoubleTap = {
                  scope.launch {
                    scale.animateTo(
                      targetValue = if (scale.value >= 2.5f) 1.5f else 3.0f,
                      animationSpec = floatMotionSpec,
                    )
                  }
                  scope.launch {
                    offset.animateTo(targetValue = Offset.Zero, animationSpec = offsetMotionSpec)
                  }
                },
              )
            }
            .fillMaxWidth(),
        )
      }
    }
  }
}
