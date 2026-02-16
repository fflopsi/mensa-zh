package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MensaRow(
  mensa: MensaState,
  detail: DetailSettings,
  onMenuClick: (Menu) -> Unit,
  toggleIsExpandedMensa: () -> Unit,
  toggleIsFavoriteMensa: () -> Unit,
  hideMensa: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var menuWidth by remember { mutableFloatStateOf(0f) }
  val offset = remember { Animatable(0f) }
  val scope = rememberCoroutineScope()

  val rowModifier by remember(mensa.state) {
    derivedStateOf {
      if (mensa.state == MensaState.State.Available || mensa.state == MensaState.State.Expanded) {
        Modifier.clickable(onClick = toggleIsExpandedMensa)
      } else Modifier
    }
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.onSizeChanged { menuWidth = it.width.toFloat() },
  ) {
    IconButton(onClick = toggleIsFavoriteMensa) {
      AnimatedContent(targetState = mensa.favorite) {
        if (it) {
          Icon(Icons.Default.Star, stringResource(R.string.unfavorite))
        } else {
          Icon(Icons.Default.StarBorder, stringResource(R.string.favorite))
        }
      }
    }
    IconButton(onClick = hideMensa) {
      Icon(Icons.Default.VisibilityOff, stringResource(R.string.hide))
    }
  }
  Surface(
    modifier = Modifier
      .offset { IntOffset(offset.value.roundToInt(), 0) }
      .pointerInput(menuWidth) {
        detectHorizontalDragGestures(
          onHorizontalDrag = { _, dragAmount ->
            scope.launch {
              val newOffset = (offset.value + dragAmount).coerceIn(0f, menuWidth)
              offset.snapTo(newOffset)
            }
          },
          onDragEnd = {
            scope.launch { offset.animateTo(if (offset.value > 0.5 * menuWidth) menuWidth else 0f) }
          },
        )
      }
      .fillMaxWidth(),
  ) {
    AnimatedContent(
      targetState = mensa.state,
      transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { state ->
      ElevatedCard(
        modifier = modifier
          .padding(horizontal = 8.dp, vertical = 4.dp)
          .focusable(),
      ) {
        Row(
          modifier = rowModifier
            .background(
              color = when (state) {
                MensaState.State.Closed, MensaState.State.Initial -> MaterialTheme.colorScheme.primaryContainer
                MensaState.State.Available, MensaState.State.Expanded -> MaterialTheme.colorScheme.primary
              },
            )
            .fillMaxWidth(),
        ) {
          Text(
            text = mensa.mensa.title,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = when (state) {
              MensaState.State.Closed, MensaState.State.Initial -> MaterialTheme.colorScheme.onPrimaryContainer
              MensaState.State.Available, MensaState.State.Expanded -> MaterialTheme.colorScheme.onPrimary
            },
            modifier = Modifier
              .weight(1f)
              .padding(8.dp),
          )
          Text(
            text = when (state) {
              MensaState.State.Closed -> stringResource(R.string.closed)
              else -> mensa.mensa.mealTime
            },
            color = when (state) {
              MensaState.State.Closed, MensaState.State.Initial -> MaterialTheme.colorScheme.onPrimaryContainer
              MensaState.State.Available, MensaState.State.Expanded -> MaterialTheme.colorScheme.onPrimary
            },
            fontStyle = when (state) {
              MensaState.State.Closed -> FontStyle.Italic
              else -> null
            },
            modifier = Modifier.padding(8.dp),
          )
        }
        AnimatedVisibility(mensa.state == MensaState.State.Expanded) {
          AnimatedContent(
            targetState = mensa.menus,
            transitionSpec = { expandVertically() togetherWith shrinkVertically() },
          ) { menus ->
            Column(modifier = Modifier.fillMaxWidth()) {
              menus.forEach {
                MenuRow(
                  menu = it,
                  detail = detail,
                  onClick = { onMenuClick(it) },
                  modifier = Modifier.fillMaxWidth(),
                )
                HorizontalDivider()
              }
            }
          }
        }
      }
    }
  }
}
