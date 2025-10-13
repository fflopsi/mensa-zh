package ch.florianfrauenfelder.mensazh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.room.Room
import ch.florianfrauenfelder.mensazh.models.AppViewModel
import ch.florianfrauenfelder.mensazh.services.CacheDatabase
import ch.florianfrauenfelder.mensazh.ui.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.days

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)

    val db = Room
      .databaseBuilder(applicationContext, CacheDatabase::class.java, "cache-database")
      .build()
    val menuDao = db.menuDao()
    val fetchInfoDao = db.fetchInfoDao()

    setContent {
      val viewModel: AppViewModel by viewModels { AppViewModel.Factory(menuDao, fetchInfoDao) }

      LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
          menuDao.deleteExpired(System.currentTimeMillis() - 1.days.inWholeMilliseconds)
        }
        viewModel.refresh()
      }

      App(
        destination = viewModel.destination,
        setDestination = viewModel::setDestination,
        weekday = viewModel.weekday,
        setWeekday = viewModel::setWeekday,
        locations = viewModel.locations,
        language = viewModel.language,
        setLanguage = viewModel::setLanguage,
        isRefreshing = viewModel.isRefreshing,
        onRefresh = { viewModel.refresh(ignoreCache = true) },
      )
    }
  }
}
