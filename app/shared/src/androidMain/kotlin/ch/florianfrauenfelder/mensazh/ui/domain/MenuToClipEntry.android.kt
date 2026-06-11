package ch.florianfrauenfelder.mensazh.ui.domain

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import ch.florianfrauenfelder.mensazh.domain.model.Menu

actual fun Menu.toClipEntry(): ClipEntry {
  return ClipEntry(
    ClipData.newPlainText("meals content", "$title: $description"),
  )
}
