package ch.florianfrauenfelder.mensazh.ui

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.models.MensaViewModel
import ch.florianfrauenfelder.mensazh.services.Prefs
import ch.florianfrauenfelder.mensazh.services.providers.AbstractMensaProvider
import ch.florianfrauenfelder.mensazh.services.showMenusInGermanFlow
import ch.florianfrauenfelder.mensazh.ui.theme.MensaZHTheme
import java.util.Date

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MensaZHApp(
  modifier: Modifier = Modifier,
  viewModel: MensaViewModel = viewModel(),
) {
  val context = LocalContext.current
  val showMenusInGerman by context.showMenusInGermanFlow.collectAsStateWithLifecycle(
    initialValue = Prefs.Defaults.SHOW_MENUS_IN_GERMAN,
  )
  val language by remember {
    derivedStateOf {
      if (showMenusInGerman) {
        AbstractMensaProvider.Language.German
      } else {
        AbstractMensaProvider.Language.English
      }
    }
  }

  // Not necessary I think
//  LaunchedEffect(Unit) {
//    viewModel.refresh(date = Date(), language = language)
//  }
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
