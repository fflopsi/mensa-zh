package ch.florianfrauenfelder.mensazh.ui.main.detail

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
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
}
