package ch.florianfrauenfelder.mensazh

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ch.florianfrauenfelder.mensazh.ui.MensaApp

fun main() {
  val container = createAppContainer()
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "MensaZH",
    ) {
      MensaApp(container = container)
    }
  }
}
