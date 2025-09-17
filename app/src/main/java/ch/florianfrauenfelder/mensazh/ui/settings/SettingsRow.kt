package ch.florianfrauenfelder.mensazh.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable (RowScope.() -> Unit) = {},
) {
  val newModifier = if (enabled && onClick != null) {
    modifier.clickable(onClick = onClick)
  } else {
    modifier.alpha(0.4f)
  }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = newModifier.padding(16.dp),
  ) {
    Column(
      modifier = Modifier
        .padding(end = 16.dp)
        .weight(1f),
    ) {
      Text(text = title, style = MaterialTheme.typography.titleLarge)
      subtitles?.map {
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
  enabled: Boolean = true,
  onClick: (() -> Unit)? = null,
  content: @Composable (RowScope.() -> Unit) = {},
) = SettingsRow(
  title = title,
  modifier = modifier,
  subtitles = listOf(subtitle),
  enabled = enabled,
  onClick = onClick,
  content = content,
)
