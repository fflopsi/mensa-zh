package ch.florianfrauenfelder.mensazh.ui.theme

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MensaZHTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  expressiveAnimations: Boolean = true,
  content: @Composable () -> Unit,
) = MaterialExpressiveTheme(
  colorScheme = colorScheme(darkTheme, dynamicColor),
  motionScheme = if (expressiveAnimations) MotionScheme.expressive() else MotionScheme.standard(),
  content = content,
)

@Composable
expect fun colorScheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean,
): ColorScheme

@Composable
fun standardColorScheme(darkTheme: Boolean = isSystemInDarkTheme()) =
  if (darkTheme) darkScheme else lightScheme

@Composable
fun <T> myMotionSpec() = MaterialTheme.motionScheme.fastSpatialSpec<T>()

@Composable
fun myExpandVertically() = expandVertically(myMotionSpec())

@Composable
fun myShrinkVertically() = shrinkVertically(myMotionSpec())

@Composable
fun myExpandHorizontally() = expandHorizontally(myMotionSpec())

@Composable
fun myShrinkHorizontally() = shrinkHorizontally(myMotionSpec())

@Composable
fun myScaleIn() = scaleIn(myMotionSpec())

@Composable
fun myScaleOut() = scaleOut(myMotionSpec())

@Composable
fun myExpandIn(expandFrom: Alignment) = expandIn(myMotionSpec(), expandFrom = expandFrom)

@Composable
fun myShrinkOut(shrinkTowards: Alignment) = shrinkOut(myMotionSpec(), shrinkTowards = shrinkTowards)

@Composable
context(scope: LazyItemScope)
fun Modifier.myAnimateItem(): Modifier {
  return with(scope) {
    animateItem(
      fadeInSpec = myMotionSpec(),
      placementSpec = myMotionSpec(),
      fadeOutSpec = myMotionSpec(),
    )
  }
}

@Composable
fun Modifier.myAnimateContentSize() = animateContentSize(myMotionSpec())
