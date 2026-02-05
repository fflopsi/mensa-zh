package ch.florianfrauenfelder.mensazh.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ch.florianfrauenfelder.mensazh.domain.value.Theme
import ch.florianfrauenfelder.mensazh.ui.main.MainScreen
import ch.florianfrauenfelder.mensazh.ui.navigation.Route
import ch.florianfrauenfelder.mensazh.ui.settings.SettingsScreen
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme

@Composable
fun MensaApp(
  viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory),
) {
  val context = LocalContext.current

  val theme by viewModel.themeSettings.collectAsStateWithLifecycle()

  val navController = rememberNavController()

  MensaZHTheme(
    darkTheme = when (theme.theme) {
      Theme.Auto -> isSystemInDarkTheme()
      Theme.Light -> false
      Theme.Dark -> true
    },
    dynamicColor = theme.useDynamicColor,
  ) {
    NavHost(
      navController = navController,
      startDestination = Route.Main,
      enterTransition = { slideIn { IntOffset(it.width, 0) } },
      exitTransition = { slideOut { IntOffset(-it.width, 0) } },
      popEnterTransition = { slideIn { IntOffset(-it.width, 0) } },
      popExitTransition = { slideOut { IntOffset(it.width, 0) } },
    ) {
      composable<Route.Main> {
        MainScreen(navigateToSettings = { navController.navigate(Route.Settings) })
      }
      composable<Route.Settings> {
        SettingsScreen(
          openSystemSettings = {
            Intent(
              Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
              Uri.fromParts("package", context.packageName, null),
            ).apply { context.startActivity(this) }
          },
          navigateUp = { navController.navigateUp() },
        )
      }
    }
  }
}
