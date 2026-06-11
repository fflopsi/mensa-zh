package ch.florianfrauenfelder.mensazh.ui.domain

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import ch.florianfrauenfelder.mensazh.domain.model.Menu

@OptIn(ExperimentalComposeUiApi::class)
actual fun Menu.toClipEntry(): ClipEntry {
  return ClipEntry.withPlainText("$title: $description")
}
