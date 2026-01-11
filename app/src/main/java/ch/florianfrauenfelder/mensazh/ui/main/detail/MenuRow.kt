package ch.florianfrauenfelder.mensazh.ui.main.detail

import android.content.ClipData
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

@Composable
fun MenuRow(
  menu: Menu,
  selected: Boolean,
  select: (Menu) -> Unit,
  autoShowImage: Boolean,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val clipboard = LocalClipboard.current
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  var showMore by rememberSaveable { mutableStateOf(if (autoShowImage) selected else false) }
  val painter = rememberAsyncImagePainter(model = menu.imageUrl)

  Box(
    modifier = modifier
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .clip(CardDefaults.shape),
  ) {
    ElevatedCard(
      colors = if (selected) {
        CardDefaults.elevatedCardColors(
          containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
      } else {
        CardDefaults.elevatedCardColors()
      },
      modifier = Modifier
        .combinedClickable(
          onClick = { showMore = !showMore },
          onLongClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            select(menu)
          },
          onLongClickLabel = stringResource(R.string.select_menu),
        )
        .animateContentSize(),
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(8.dp),
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row {
            if (menu.title.isNotBlank()) {
              Text(
                text = menu.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
              )
              if (menu.isVegan || menu.isVegetarian) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = stringResource(if (menu.isVegan) R.string.vegan else R.string.vegetarian),
                  color = Color(0xFF22AA22),
                  fontWeight = FontWeight.Bold,
                )
              }
            }
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
        AnimatedVisibility(showMore) {
          Column(verticalArrangement = Arrangement.Bottom) {
            FilledIconButton(
              onClick = {
                scope.launch {
                  clipboard.setClipEntry(
                    ClipEntry(
                      ClipData.newPlainText("meals content", "${menu.title}: ${menu.description}"),
                    ),
                  )
                }
              },
            ) {
              Icon(Icons.Default.ContentCopy, stringResource(R.string.copy_menu))
            }
            FilledIconButton(
              onClick = {
                context.startActivity(
                  Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                      putExtra(Intent.EXTRA_TEXT, "${menu.title}: ${menu.description}")
                      type = "text/plain"
                    },
                    null,
                  ),
                )
              },
            ) {
              Icon(Icons.Default.Share, stringResource(R.string.share))
            }
          }
        }
      }
      if (!menu.imageUrl.isNullOrEmpty()) {
        AnimatedVisibility(showMore) {
          Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
    }

    if (!menu.imageUrl.isNullOrEmpty()) {
      AnimatedVisibility(
        visible = !showMore,
        modifier = Modifier
          .size(48.dp)
          .align(Alignment.TopEnd),
      ) {
        Image(
          painter = painter,
          contentDescription = stringResource(R.string.image_available),
          alignment = Alignment.TopEnd,
        )
      }
    }
  }
}
