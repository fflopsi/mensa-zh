package ch.florianfrauenfelder.mensazh.ui.main

import android.content.Intent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import ch.florianfrauenfelder.mensazh.domain.model.MensaState

@Composable
actual fun OpenInBrowserButton(
  selectedMensa: MensaState?,
  modifier: Modifier,
  enabled: Boolean,
  colors: IconButtonColors,
  interactionSource: MutableInteractionSource?,
  shape: Shape,
  content: @Composable (() -> Unit),
) {
  val context = LocalContext.current

  IconButton(
    onClick = {
      Intent(Intent.ACTION_VIEW).apply {
        data = selectedMensa?.mensa?.url?.toUri()
        context.startActivity(this)
      }
    },
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    interactionSource = interactionSource,
    shape = shape,
    content = content,
  )
}
