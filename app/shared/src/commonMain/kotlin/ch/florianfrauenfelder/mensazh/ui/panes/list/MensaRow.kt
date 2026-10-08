package ch.florianfrauenfelder.mensazh.ui.panes.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.domain.model.MensaState
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.preferences.DetailSettings
import ch.florianfrauenfelder.mensazh.ui.theme.myMotionSpec
import kotlinx.coroutines.launch
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
  var menuWidth by remember { mutableFloatStateOf(0f) }
  val offset = remember { Animatable(0f) }
  val scope = rememberCoroutineScope()
  val motion = MaterialTheme.motionScheme

  val rowModifier by remember(mensa.state) {
    derivedStateOf {
      if (mensa.state.active) Modifier.clickable(onClick = toggleIsExpandedMensa) else Modifier
    }
  }
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

  Box(modifier = modifier) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.onSizeChanged { menuWidth = it.width.toFloat() },
    ) {
      IconButton(onClick = toggleIsFavoriteMensa) {
        AnimatedContent(
          targetState = mensa.favorite,
          transitionSpec = {
            fadeIn(motion.defaultEffectsSpec()) togetherWith fadeOut(motion.defaultEffectsSpec())
          },
        ) {
          if (it) {
            Icon(
              painterResource(Res.drawable.ic_star_filled_24),
              stringResource(Res.string.unfavorite),
            )
          } else {
            Icon(painterResource(Res.drawable.ic_star_24), stringResource(Res.string.favorite))
          }
        }
      }
      IconButton(onClick = hideMensa) {
        Icon(painterResource(Res.drawable.ic_visibility_off_24), stringResource(Res.string.hide))
      }
    }
    val motionSpec = myMotionSpec<Float>()
    ElevatedCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .offset { IntOffset(offset.value.roundToInt(), 0) }
        .pointerInput(menuWidth) {
          detectHorizontalDragGestures(
            onHorizontalDrag = { _, dragAmount ->
              scope.launch { offset.snapTo((offset.value + dragAmount).coerceIn(0f, menuWidth)) }
            },
            onDragEnd = {
              scope.launch {
                offset.animateTo(
                  targetValue = if (offset.value > 0.5 * menuWidth) menuWidth else 0f,
                  animationSpec = motionSpec,
                )
              }
            },
          )
        }
        .focusable(),
    ) {
      Row(
        modifier = rowModifier
          .background(containerColor)
          .fillMaxWidth(),
      ) {
        Text(
          text = mensa.mensa.title,
          fontWeight = FontWeight.Bold,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          color = contentColor,
          modifier = Modifier
            .weight(1f)
            .padding(8.dp),
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
        enter = expandVertically(myMotionSpec()),
        exit = shrinkVertically(myMotionSpec()),
      ) {
        val motionSpec = myMotionSpec<IntOffset>()
        AnimatedContent(
          targetState = mensa.menus,
          transitionSpec = {
            slideIntoContainer(
              animationSpec = motionSpec,
              towards = AnimatedContentTransitionScope.SlideDirection.Down,
            ) togetherWith
              slideOutOfContainer(
                animationSpec = motionSpec,
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
              )
          },
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
