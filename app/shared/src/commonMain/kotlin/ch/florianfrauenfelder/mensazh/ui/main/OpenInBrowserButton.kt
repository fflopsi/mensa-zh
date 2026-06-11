package ch.florianfrauenfelder.mensazh.ui.main

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import ch.florianfrauenfelder.mensazh.domain.model.MensaState

@Composable
expect fun OpenInBrowserButton(
  selectedMensa: MensaState?,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
  interactionSource: MutableInteractionSource? = null,
  shape: Shape = IconButtonDefaults.standardShape,
  content: @Composable (() -> Unit),
)
