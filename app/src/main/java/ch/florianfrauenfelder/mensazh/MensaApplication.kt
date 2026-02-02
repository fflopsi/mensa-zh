package ch.florianfrauenfelder.mensazh

import android.app.Application

class MensaApplication : Application() {
  lateinit var container: AppContainer
    private set

  override fun onCreate() {
    super.onCreate()
    container = AppContainer(this)
  }
}
