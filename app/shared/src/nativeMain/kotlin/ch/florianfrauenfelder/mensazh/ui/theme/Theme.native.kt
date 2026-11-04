package ch.florianfrauenfelder.mensazh.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun colorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme =
  standardColorScheme(darkTheme)
