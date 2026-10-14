package ch.florianfrauenfelder.mensazh.ui.panes.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.ui.theme.myExpandVertically
import ch.florianfrauenfelder.mensazh.ui.theme.myShrinkVertically
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.closed
import mensazh.app.shared.generated.resources.favorite
import mensazh.app.shared.generated.resources.hide
import mensazh.app.shared.generated.resources.ic_star_24
import mensazh.app.shared.generated.resources.ic_star_filled_24
import mensazh.app.shared.generated.resources.ic_visibility_off_24
import mensazh.app.shared.generated.resources.unfavorite
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
  val density = LocalDensity.current
  val motion = MaterialTheme.motionScheme

  val actionsWidth = 96.dp
  val actionsWidthPx = with(density) { actionsWidth.toPx() }
  val state = retain {
    AnchoredDraggableState(
      initialValue = 0,
      anchors = DraggableAnchors { 0 at 0f; 1 at actionsWidthPx },
    )
  }
  val revealed by retain { derivedStateOf { state.offset != 0f } }

  val containerColor by animateColorAsState(
    targetValue = if (mensa.state.active) {
      MaterialTheme.colorScheme.primary
    } else {
      MaterialTheme.colorScheme.primaryContainer
    },
    animationSpec = motion.defaultEffectsSpec(),
  )
  val contentColor by animateColorAsState(
    targetValue = if (mensa.state.active) {
      MaterialTheme.colorScheme.onPrimary
    } else {
      MaterialTheme.colorScheme.onPrimaryContainer
    },
    animationSpec = motion.defaultEffectsSpec(),
  )

  val favorite = stringResource(Res.string.favorite)
  val unfavorite = stringResource(Res.string.unfavorite)
  val hide = stringResource(Res.string.hide)

  Box(modifier = modifier.anchoredDraggable(state = state, orientation = Orientation.Horizontal)) {
    if (revealed) {
      Row(modifier = Modifier.padding(horizontal = 8.dp).width(actionsWidth)) {
        IconButton(onClick = toggleIsFavoriteMensa) {
          AnimatedContent(
            targetState = mensa.favorite,
            transitionSpec = {
              fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.defaultEffectsSpec())
            },
          ) {
            if (it) {
              Icon(painterResource(Res.drawable.ic_star_filled_24), unfavorite)
            } else {
              Icon(painterResource(Res.drawable.ic_star_24), favorite)
            }
          }
        }
        IconButton(onClick = hideMensa) {
          Icon(painterResource(Res.drawable.ic_visibility_off_24), hide)
        }
      }
    }
    Box(modifier = Modifier.offset { IntOffset(state.offset.roundToInt(), 0) }) {
      ElevatedCard(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 5.dp)
          .semantics {
            CustomAccessibilityAction(label = if (mensa.favorite) unfavorite else favorite) {
              toggleIsFavoriteMensa()
              true
            }
            CustomAccessibilityAction(label = hide) { hideMensa(); true }
          },
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable(enabled = mensa.state.active, onClick = toggleIsExpandedMensa)
            .background(containerColor)
            .fillMaxWidth(),
        ) {
          Text(
            text = mensa.mensa.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = contentColor,
            modifier = Modifier
              .weight(1f)
              .padding(9.dp),
          )
          AnimatedContent(targetState = mensa.state == MensaState.State.Closed) { closed ->
            Text(
              text = if (closed) stringResource(Res.string.closed) else mensa.mensa.mealTime,
              color = contentColor,
              fontStyle = if (closed) FontStyle.Italic else null,
              modifier = Modifier.padding(8.dp),
            )
          }
        }
        AnimatedVisibility(
          visible = mensa.state == MensaState.State.Expanded,
          enter = myExpandVertically(),
          exit = myShrinkVertically(),
        ) {
          AnimatedContent(targetState = mensa.menus) { menus ->
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
