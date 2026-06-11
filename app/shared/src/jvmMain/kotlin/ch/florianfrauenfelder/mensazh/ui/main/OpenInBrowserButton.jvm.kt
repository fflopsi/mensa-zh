package ch.florianfrauenfelder.mensazh.ui.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
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
}
