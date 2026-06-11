package ch.florianfrauenfelder.mensazh.ui.main.detail

import android.content.Intent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import ch.florianfrauenfelder.mensazh.domain.model.Menu

@Composable
actual fun ShareButton(
  menu: Menu,
  modifier: Modifier,
  enabled: Boolean,
  shape: Shape,
  colors: IconButtonColors,
  interactionSource: MutableInteractionSource?,
  content: @Composable (() -> Unit),
) {
  val context = LocalContext.current

  FilledIconButton(
    onClick = {
      context.startActivity(
        Intent.createChooser(
          Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "${menu.title}: ${menu.description}")
            type = "text/plain"
          },
          null,
        ),
      )
    },
    modifier = modifier,
    enabled = enabled,
    shape = shape,
    colors = colors,
    interactionSource = interactionSource,
    content = content,
  )
}
