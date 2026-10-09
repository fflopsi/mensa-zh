package ch.florianfrauenfelder.mensazh.ui.panes.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.value.NutrientsPer
import ch.florianfrauenfelder.mensazh.ui.domain.toClipEntry
import ch.florianfrauenfelder.mensazh.ui.theme.myMotionSpec
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import mensazh.app.shared.generated.resources.Res
import mensazh.app.shared.generated.resources.carbohydrates
import mensazh.app.shared.generated.resources.copy_menu
import mensazh.app.shared.generated.resources.energy
import mensazh.app.shared.generated.resources.fat
import mensazh.app.shared.generated.resources.fiber
import mensazh.app.shared.generated.resources.ic_content_copy_24
import mensazh.app.shared.generated.resources.ic_share_24
import mensazh.app.shared.generated.resources.image_available
import mensazh.app.shared.generated.resources.one_hundred_grams
import mensazh.app.shared.generated.resources.per
import mensazh.app.shared.generated.resources.protein
import mensazh.app.shared.generated.resources.salt
import mensazh.app.shared.generated.resources.saturated_fatty_acids
import mensazh.app.shared.generated.resources.select_menu
import mensazh.app.shared.generated.resources.serving
import mensazh.app.shared.generated.resources.share
import mensazh.app.shared.generated.resources.sugar
import mensazh.app.shared.generated.resources.vegan
import mensazh.app.shared.generated.resources.vegetarian
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MenuRow(
  menu: Menu,
  selected: Boolean,
  select: (Menu) -> Unit,
  autoShowImage: Boolean,
  modifier: Modifier = Modifier,
) {
  val clipboard = LocalClipboard.current
  val haptics = LocalHapticFeedback.current
  val scope = rememberCoroutineScope()

  var showMore by rememberSaveable { mutableStateOf(autoShowImage && selected) }
  val painter = rememberAsyncImagePainter(model = menu.imageUrl)

  val showImage = rememberSaveable { mutableStateOf(false) }
  ImageDialog(
    show = showImage,
    painter = painter,
    modifier = Modifier
      .fillMaxWidth()
      .fillMaxHeight(0.7f),
  )

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
    ) {
      Row(
        modifier = Modifier
          .combinedClickable(
            onClick = { showMore = !showMore },
            onLongClick = {
              haptics.performHapticFeedback(HapticFeedbackType.LongPress)
              select(menu)
            },
            onLongClickLabel = stringResource(Res.string.select_menu),
          )
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
                modifier = Modifier.animateContentSize(myMotionSpec()),
              )
              if (menu.isVegan || menu.isVegetarian) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = stringResource(if (menu.isVegan) Res.string.vegan else Res.string.vegetarian),
                  color = Color(0xFF22AA22),
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.animateContentSize(myMotionSpec()),
                )
              }
            }
          }
          if (menu.price.isNotEmpty()) {
            Text(
              text = menu.price.joinToString(" / "),
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.animateContentSize(myMotionSpec()),
            )
          }
          if (menu.description.isNotBlank()) {
            Text(
              text = menu.description,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .animateContentSize(myMotionSpec()),
            )
          }
          if (!menu.allergens.isNullOrBlank()) {
            Text(
              text = menu.allergens,
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .animateContentSize(myMotionSpec()),
            )
          }
          Nutrients(
            menu = menu,
            modifier = Modifier.fillMaxWidth().animateContentSize(myMotionSpec()),
          )
        }
        Column(verticalArrangement = Arrangement.Bottom) {
          AnimatedVisibility(
            visible = showMore,
            enter = scaleIn(myMotionSpec()),
            exit = scaleOut(myMotionSpec()),
          ) {
            FilledIconButton(
              onClick = { scope.launch { clipboard.setClipEntry(menu.toClipEntry()) } },
            ) {
              Icon(
                painterResource(Res.drawable.ic_content_copy_24),
                stringResource(Res.string.copy_menu),
              )
            }
          }
          AnimatedVisibility(
            visible = showMore,
            enter = scaleIn(myMotionSpec()),
            exit = scaleOut(myMotionSpec()),
          ) {
            ShareButton(menu = menu) {
              Icon(painterResource(Res.drawable.ic_share_24), stringResource(Res.string.share))
            }
          }
        }
      }
      if (!menu.imageUrl.isNullOrEmpty()) {
        AnimatedVisibility(
          visible = showMore,
          enter = expandVertically(myMotionSpec()),
          exit = shrinkVertically(myMotionSpec()),
        ) {
          Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
              .clickable { showImage.value = true }
              .fillMaxWidth(),
          )
        }
      }
    }

    if (!menu.imageUrl.isNullOrEmpty()) {
      AnimatedVisibility(
        visible = !showMore,
        enter = expandIn(myMotionSpec(), Alignment.TopEnd),
        exit = shrinkOut(myMotionSpec(), Alignment.TopEnd),
        modifier = Modifier.align(Alignment.TopEnd).clip(RoundedCornerShape(4.dp)),
      ) {
        Image(
          painter = painter,
          contentDescription = stringResource(Res.string.image_available),
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(48.dp),
        )
      }
    }
  }
}

@Composable
fun Nutrients(menu: Menu, modifier: Modifier = Modifier) {
  if (menu.energy == null && menu.fat == null && menu.saturatedFattyAcids == null
    && menu.carbohydrates == null && menu.sugar == null && menu.fiber == null
    && menu.protein == null && menu.salt == null
  ) return
  Text(
    text = "(${stringResource(Res.string.per)} ${
      when (menu.nutrientsPer) {
        NutrientsPer.Serving -> stringResource(Res.string.serving)
        NutrientsPer.OneHundredGrams -> stringResource(Res.string.one_hundred_grams)
      }
    }) " +
      (menu.energy?.let { "${stringResource(Res.string.energy)}: ${it}kcal, " } ?: "") +
      (menu.fat?.let { "${stringResource(Res.string.fat)}: ${it}g, " } ?: "") +
      (menu.saturatedFattyAcids?.let { "${stringResource(Res.string.saturated_fatty_acids)}: ${it}g, " }
        ?: "") +
      (menu.carbohydrates?.let { "${stringResource(Res.string.carbohydrates)}: ${it}g, " } ?: "") +
      (menu.sugar?.let { "${stringResource(Res.string.sugar)}: ${it}g, " } ?: "") +
      (menu.fiber?.let { "${stringResource(Res.string.fiber)}: ${it}g, " } ?: "") +
      (menu.protein?.let { "${stringResource(Res.string.protein)}: ${it}g, " } ?: "") +
      (menu.salt?.let { "${stringResource(Res.string.salt)}: ${it}g" } ?: ""),
    style = MaterialTheme.typography.bodySmall,
    modifier = modifier.padding(top = 8.dp),
  )
}
