package ch.florianfrauenfelder.mensazh.ui.main.detail

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import ch.florianfrauenfelder.mensazh.domain.model.Menu

@Composable
expect fun ShareButton(
  menu: Menu,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = IconButtonDefaults.filledShape,
  colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
  interactionSource: MutableInteractionSource? = null,
  content: @Composable (() -> Unit),
)
