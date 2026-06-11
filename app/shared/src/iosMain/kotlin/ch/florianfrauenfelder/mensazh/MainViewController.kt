package ch.florianfrauenfelder.mensazh

import androidx.compose.ui.window.ComposeUIViewController
import ch.florianfrauenfelder.mensazh.ui.MensaApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController {
  val container = createAppContainer()
  return ComposeUIViewController {
    MensaApp(container = container)
  }
}
