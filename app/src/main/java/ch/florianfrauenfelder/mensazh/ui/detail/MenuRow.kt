package ch.florianfrauenfelder.mensazh.ui.detail

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Menu
import kotlinx.coroutines.launch

@Composable
fun MenuRow(
  menu: Menu,
  selected: Boolean,
  select: (Menu) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val clipboard = LocalClipboard.current
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  ElevatedCard(
    colors = if (selected) {
      CardDefaults.elevatedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
      )
    } else {
      CardDefaults.elevatedCardColors()
    },
    modifier = modifier
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .clip(CardDefaults.shape)
      .combinedClickable(
        onClick = {
          scope.launch {
            clipboard.setClipEntry(ClipEntry(
              ClipData.newPlainText("meals content", "${menu.title}: ${menu.description}"),
            ))
          }
        },
        onLongClick = {
          haptics.performHapticFeedback(HapticFeedbackType.LongPress)
          select(menu)
        },
      )
      .fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        if (menu.title.isNotBlank()) {
          Text(
            text = menu.title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
          )
        }
        if (menu.price.isNotEmpty()) {
          Text(
            text = menu.price.joinToString(" / "),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
        if (menu.description.isNotBlank()) {
          Text(
            text = menu.description,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
              .padding(top = 8.dp)
              .fillMaxWidth(),
          )
        }
        if (!menu.allergens.isNullOrBlank()) {
          Text(
            text = menu.allergens,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
              .padding(top = 8.dp)
              .fillMaxWidth(),
          )
        }
      }
      FilledIconButton(
        onClick = {
          context.startActivity(Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
              putExtra(Intent.EXTRA_TEXT, "${menu.title}: ${menu.description}")
              type = "text/plain"
            },
            null,
          ))
        },
        modifier = Modifier.align(Alignment.Bottom),
      ) {
        Icon(Icons.Filled.Share, stringResource(R.string.share))
      }
    }
  }
}
