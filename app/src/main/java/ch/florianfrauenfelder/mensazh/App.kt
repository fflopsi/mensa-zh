package ch.florianfrauenfelder.mensazh

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.models.AppViewModel
import ch.florianfrauenfelder.mensazh.services.Prefs
import ch.florianfrauenfelder.mensazh.services.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.services.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.ui.ListDetailScreen
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import java.util.Date

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MensaZHApp(
  modifier: Modifier = Modifier,
  viewModel: AppViewModel = viewModel(),
) {
  val context = LocalContext.current

  val showMenusInGerman by context.showMenusInGermanFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_MENUS_IN_GERMAN,
  )
  val language = remember(showMenusInGerman) {
    if (showMenusInGerman) MensaProvider.Language.German else MensaProvider.Language.English
  }

  LaunchedEffect(showMenusInGerman) {
    viewModel.refresh(date = Date(), language = language)
  }

  MensaZHTheme {
    ListDetailScreen(
      locations = viewModel.locations,
      showMenusInGerman = showMenusInGerman,
      isRefreshing = viewModel.isRefreshing,
      onRefresh = { viewModel.refresh(date = Date(), language = language, ignoreCache = true) },
      modifier = modifier,
    )
  }
}
