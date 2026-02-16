package ch.florianfrauenfelder.mensazh.data.repository

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Event
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerializationException
import java.io.IOException
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.uuid.Uuid

class MensaRepository(
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  private val providers: Map<Institution, MensaProvider<*, *, *>>,
  appScope: CoroutineScope,
) {
  private val _isRefreshing = providers.keys.associateWith { MutableStateFlow(false) }
  val isRefreshing = combine(_isRefreshing.values) { refreshes -> refreshes.any { it } }

  val eventChannel = Channel<Event>()
  private val noInternetSent = AtomicBoolean(false)
  private val handler = CoroutineExceptionHandler { _, throwable ->
    when (throwable) {
      is IOException -> {
        if (noInternetSent.compareAndSet(expectedValue = false, newValue = true)) {
          appScope.launch { eventChannel.send(Event.NoInternet) }
        }
      }
      is SerializationException, is IllegalArgumentException -> {
        appScope.launch { eventChannel.send(Event.ApiError) }
      }
    }
  }

  val baseLocations = flow {
    emit(
      coroutineScope {
        providers.values.map { async { it.getLocations() } }.awaitAll().flatten()
      },
    )
  }.stateIn(
    scope = appScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList(),
  )

  fun observeMenus(
    mensaId: Uuid,
    language: Language,
    date: LocalDate,
  ): Flow<List<Menu>> =
    menuDao.getMenus(mensaId, language, date).map { list -> list.map { it.toMenu() } }

  suspend fun forceRefresh(destination: Destination, language: Language) = supervisorScope {
    noInternetSent.store(false)
    providers.forEach { (institution, _) ->
      launch(handler) { refresh(institution, destination, language) }
    }
  }

  suspend fun refreshIfNeeded(destination: Destination, language: Language) = supervisorScope {
    noInternetSent.store(false)
    providers.forEach { (institution, _) ->
      if (shouldRefresh(institution, destination, language)) {
        launch(handler) { refresh(institution, destination, language) }
      }
    }
  }

  suspend fun deleteExpired() {
    menuDao.deleteExpired(System.currentTimeMillis() - 1.days.inWholeMilliseconds)
  }

  suspend fun clearCache() {
    fetchInfoDao.clearAll()
    menuDao.clearAll()
  }

  private val refreshMutexes = providers.keys.associateWith { Mutex() }

  /**
   * @throws IOException Menus could not be fetched
   * @throws IllegalStateException Call already executed
   * @throws [SerializationException] Menus could not be parsed
   * @throws IllegalArgumentException Menus could not be parsed
   * */
  private suspend fun refresh(
    institution: Institution,
    destination: Destination,
    language: Language,
  ) {
    refreshMutexes[institution]?.withLock {
      _isRefreshing[institution]?.value = true
      try {
        providers[institution]?.fetchMenus(destination, language)
      } finally {
        _isRefreshing[institution]?.value = false
      }
    }
  }

  private suspend fun shouldRefresh(
    institution: Institution,
    destination: Destination,
    language: Language,
  ): Boolean {
    val now = System.currentTimeMillis()
    val fetchInfo = fetchInfoDao.getFetchInfo(institution, destination, language)

    return _isRefreshing[institution]?.value != true &&
      now - (fetchInfo?.fetchDate ?: 0) > 12.hours.inWholeMilliseconds
  }
}
