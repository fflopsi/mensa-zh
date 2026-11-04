package ch.florianfrauenfelder.mensazh.ui.domain

import androidx.compose.ui.platform.ClipEntry
import ch.florianfrauenfelder.mensazh.domain.model.Menu

expect fun Menu.toClipEntry(): ClipEntry
