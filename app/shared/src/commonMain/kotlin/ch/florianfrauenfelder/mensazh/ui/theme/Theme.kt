package ch.florianfrauenfelder.mensazh.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MensaZHTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) = MaterialExpressiveTheme(
  colorScheme = colorScheme(darkTheme, dynamicColor),
  motionScheme = MotionScheme.expressive(),
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
context(scope: LazyItemScope)
fun Modifier.animateItemModifier(): Modifier {
  return with(scope) {
    animateItem(
      fadeInSpec = myMotionSpec(),
      placementSpec = myMotionSpec(),
      fadeOutSpec = myMotionSpec(),
    )
  }
}
