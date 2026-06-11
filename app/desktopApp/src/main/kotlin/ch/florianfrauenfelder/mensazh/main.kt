package ch.florianfrauenfelder.mensazh

import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.florianfrauenfelder.mensazh.data.local.datastore.createDataStore
import ch.florianfrauenfelder.mensazh.data.local.room.getDatabaseBuilder
import ch.florianfrauenfelder.mensazh.data.local.room.getRoomDatabase
import ch.florianfrauenfelder.mensazh.ui.AppViewModel
import ch.florianfrauenfelder.mensazh.ui.MensaApp

fun main() {
  val container = AppContainer(createDataStore(), getRoomDatabase(getDatabaseBuilder()))

  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "MensaZH",
    ) {
      val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(container))
      val theme by appViewModel.themeSettings.collectAsStateWithLifecycle()

      MensaApp(container = container, theme = theme)
    }
  }
}
