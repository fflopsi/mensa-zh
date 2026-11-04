package ch.florianfrauenfelder.mensazh

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ch.florianfrauenfelder.mensazh.ui.MensaApp
import mensazh.app.desktopapp.generated.resources.Res
import mensazh.app.desktopapp.generated.resources.app
import mensazh.app.desktopapp.generated.resources.reload
import org.jetbrains.compose.resources.stringResource

fun main() {
  val container = createAppContainer()
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "MensaZH",
    ) {
      MenuBar {
        Menu(
          text = stringResource(Res.string.app),
          mnemonic = 'a',
        ) {
          Item(
            text = stringResource(Res.string.reload),
            mnemonic = 'r',
            shortcut = KeyShortcut(Key.F5),
            onClick = { container.commandChannel.trySend(AppCommand.Reload) },
          )
        }
      }
      MensaApp(container = container)
    }
  }
}
