package ch.florianfrauenfelder.mensazh.data.repository

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.providers.MensaProvider
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.navigation.Params
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class MensaRepository(
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  private val providers: Map<Institution, MensaProvider<*, *>>,
) {
  private val _isRefreshing = providers.keys.associateWith { MutableStateFlow(false) }
  val isRefreshing = combine(_isRefreshing.values) { refreshes -> refreshes.any { it } }

  suspend fun getLocations(): List<Location> = coroutineScope {
    providers.values.map { async { it.getLocations() } }.awaitAll().flatten()
  }

  fun observeMenus(
    mensaId: UUID,
    language: Language,
    date: LocalDate,
  ): Flow<List<Menu>> = menuDao.getMenus2(
    mensaId = mensaId.toString(),
    language = language.toString(),
    date = date.toString(),
  ).map { list -> list.map { it.toMenu() } }

  suspend fun forceRefresh(params: Params) = supervisorScope {
    providers.forEach { (institution, _) ->
      launch { refresh(params, institution) }
    }
  }

  suspend fun refreshIfNeeded(params: Params) = supervisorScope {
    providers.forEach { (institution, _) ->
      if (shouldRefresh(params, institution)) launch { refresh(params, institution) }
    }
  }

  private val refreshMutexes = providers.keys.associateWith { Mutex() }

  private suspend fun refresh(params: Params, institution: Institution) {
    refreshMutexes[institution]?.withLock {
      _isRefreshing[institution]?.value = true
      try {
        withContext(Dispatchers.IO) {
          providers[institution]?.fetchMenus(
            language = params.language,
            destination = params.destination,
          )
        }
      } finally {
        _isRefreshing[institution]?.value = false
      }
    }
  }

  private suspend fun shouldRefresh(
    params: Params,
    institution: Institution,
  ): Boolean {
    val now = System.currentTimeMillis()

    val fetchInfo = withContext(Dispatchers.IO) {
      fetchInfoDao.getFetchInfo(
        institution = institution.toString(),
        destination = params.destination.toString(),
        language = params.language.toString(),
      )
    }

    return now - (fetchInfo?.fetchDate ?: 0) > 12.hours.inWholeMilliseconds
  }
}
