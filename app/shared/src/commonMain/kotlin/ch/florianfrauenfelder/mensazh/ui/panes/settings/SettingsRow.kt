package ch.florianfrauenfelder.mensazh.ui.panes.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun SettingsRow(
  title: String,
  modifier: Modifier = Modifier,
  subtitles: List<String>? = null,
  weightTitle: Boolean = true,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable (RowScope.() -> Unit) = {},
) {
  val newModifier = if (enabled && onClick != null) {
    modifier.clickable(onClick = onClick)
  } else if (!enabled) {
    modifier.alpha(0.4f)
  } else {
    modifier
  }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = newModifier.padding(16.dp).fillMaxWidth(),
  ) {
    Column(
      modifier = (if (weightTitle) Modifier.weight(1f) else Modifier)
        .padding(end = 16.dp),
    ) {
      Text(text = title, style = MaterialTheme.typography.titleLarge)
      subtitles?.forEach {
        Text(
          text = it,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          style = MaterialTheme.typography.bodyMedium,
        )
      }
    }
    content()
  }
}

@Composable
fun SettingsRow(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String,
  weightTitle: Boolean = true,
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable (RowScope.() -> Unit) = {},
) = SettingsRow(
  title = title,
  modifier = modifier,
  subtitles = listOf(subtitle),
  weightTitle = weightTitle,
  enabled = enabled,
  onClick = onClick,
  content = content,
)
