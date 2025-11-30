package ch.florianfrauenfelder.mensazh.ui.main.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.R
import ch.florianfrauenfelder.mensazh.models.Mensa
import ch.florianfrauenfelder.mensazh.models.Menu

@Composable
fun MensaRow(
  mensa: Mensa,
  saveIsExpandedMensa: (Mensa, Boolean) -> Unit,
  listUseShortDescription: Boolean,
  listShowAllergens: Boolean,
  onMenuClick: (Menu) -> Unit,
  modifier: Modifier = Modifier,
) {
  val rowModifier by remember(mensa.state) {
    derivedStateOf {
      if (mensa.state == Mensa.State.Available || mensa.state == Mensa.State.Expanded) {
        Modifier.clickable {
          if (mensa.state == Mensa.State.Available) {
            mensa.state = Mensa.State.Expanded
            saveIsExpandedMensa(mensa, true)
          } else if (mensa.state == Mensa.State.Expanded) {
            mensa.state = Mensa.State.Available
            saveIsExpandedMensa(mensa, false)
          }
        }
      } else Modifier
    }
  }

  ElevatedCard(
    modifier = modifier
      .padding(
        horizontal = 8.dp,
        vertical = 4.dp,
      )
      .focusable(),
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = rowModifier
          .background(
            color = when (mensa.state) {
              Mensa.State.Closed, Mensa.State.Initial -> MaterialTheme.colorScheme.primaryContainer
              Mensa.State.Available, Mensa.State.Expanded -> MaterialTheme.colorScheme.primary
            },
          )
          .fillMaxWidth(),
      ) {
        Text(
          text = mensa.title,
          fontWeight = FontWeight.Bold,
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          color = when (mensa.state) {
            Mensa.State.Closed, Mensa.State.Initial -> MaterialTheme.colorScheme.onPrimaryContainer
            Mensa.State.Available, Mensa.State.Expanded -> MaterialTheme.colorScheme.onPrimary
          },
          modifier = Modifier
            .weight(1f)
            .padding(8.dp),
        )
        Text(
          text = when (mensa.state) {
            Mensa.State.Closed -> stringResource(R.string.closed)
            else -> mensa.mealTime
          },
          color = when (mensa.state) {
            Mensa.State.Closed, Mensa.State.Initial -> MaterialTheme.colorScheme.onPrimaryContainer
            Mensa.State.Available, Mensa.State.Expanded -> MaterialTheme.colorScheme.onPrimary
          },
          fontStyle = when (mensa.state) {
            Mensa.State.Closed -> FontStyle.Italic
            else -> null
          },
          modifier = Modifier.padding(8.dp),
        )
      }
      AnimatedVisibility(mensa.state == Mensa.State.Expanded) {
        Column(modifier = Modifier.fillMaxWidth()) {
          mensa.menus.forEach { menu ->
            AnimatedContent(
              targetState = menu,
              transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            ) {
              MenuRow(
                menu = it,
                listUseShortDescription = listUseShortDescription,
                listShowAllergens = listShowAllergens,
                onClick = { onMenuClick(it) },
                modifier = Modifier.fillMaxWidth(),
              )
            }
            HorizontalDivider()
          }
        }
      }
    }
  }
}
