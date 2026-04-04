package ch.florianfrauenfelder.mensazh.data.providers

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfo
import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.local.room.RoomMenu
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.data.util.SerializationService
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.reflect.TypeInfo
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import java.io.IOException
import kotlin.time.Clock
import kotlin.uuid.Uuid

sealed class MensaProvider<L : MensaProvider.ApiLocation<M>, M : MensaProvider.ApiMensa, R : MensaProvider.Api.Root>(
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  private val assetService: AssetService,
) {
  abstract val institution: Institution
  protected abstract val locationsFile: String
  protected abstract val locationSerializer: KSerializer<L>
  protected abstract val apiRootTypeInfo: TypeInfo
  private val _apiMensas = mutableListOf<M>()
  protected val apiMensas: List<M> = _apiMensas
  protected abstract val oneLanguagePerCall: Boolean
  protected val client = HttpClient {
    install(ContentNegotiation) {
      json(json = SerializationService.safeJson)
    }
  }

  suspend fun getLocations(): List<Location> {
    val json: String = assetService.readStringFile(locationsFile) ?: return emptyList()
    return SerializationService.safeJson
      .decodeFromString(
        ListSerializer(locationSerializer),
        json,
      ) // Should not throw during normal operation
      .map { apiLocation ->
        Location(
          id = Uuid.parse(apiLocation.id),
          title = apiLocation.title,
          mensas = apiLocation.mensas.map {
            _apiMensas += it
            it.toMensa().toMensaState()
          },
        )
      }
  }

  /**
   * @throws IOException Menus could not be fetched
   * @throws IllegalStateException Call already executed
   * @throws SerializationException Menus could not be parsed
   * @throws IllegalArgumentException Menus could not be parsed
   * */
  suspend fun fetchMenus(
    destination: Destination,
    language: Language,
  ) {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    val monday = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY).run {
      if (destination == Destination.NextWeek) {
        plus(7, DateTimeUnit.DAY)
      } else this
    }

    supervisorScope {
      launch {
        val root = fetchJson(destination, language) ?: return@launch
        updateFetchInfo(destination, language)
        menuDao.insertMenus(extractMenus(root, monday, language))
      }
      if (oneLanguagePerCall) launch {
        val root = fetchJson(destination, !language) ?: return@launch
        updateFetchInfo(destination, !language)
        menuDao.insertMenus(extractMenus(root, monday, !language))
      }
    }

  }

  /**
   * @throws IOException JSON could not be fetched
   * @throws IllegalStateException Call already executed
   * */
  private suspend fun fetchJson(destination: Destination, language: Language): R? {
    val response = client.request {
      request(destination, language)
    }
    return if (response.status.value in 200..299) {
      response.body(apiRootTypeInfo)
    } else null
  }

  protected abstract fun HttpRequestBuilder.request(destination: Destination, language: Language)

  protected abstract fun extractMenus(
    root: R,
    monday: LocalDate,
    language: Language,
  ): List<RoomMenu>

  protected abstract suspend fun updateFetchInfo(destination: Destination, language: Language)

  protected suspend fun insertFetchInfo(destination: Destination, language: Language) =
    fetchInfoDao.insertFetchInfo(
      FetchInfo(
        institution = institution,
        destination = destination,
        language = language,
      ),
    )

  protected val RoomMenu.hasClosedNotice: Boolean
    get() = listOf(
      "We look forward to serving you this menu again soon!",
      "Dieses Menu servieren wir Ihnen gerne bald wieder!",
      "closed",
      "geschlossen",
      "kein Abendessen",
      "no dinner",
      "novalue",
      "Wir sind ab Vollsemester",
      "Betriebsferien",
    )
      .onEach { it.lowercase() }
      .any { description.lowercase().contains(it) || title.lowercase() == it }
      || description.isBlank()

  @Serializable
  sealed class ApiLocation<M : ApiMensa> {
    abstract val id: String
    abstract val title: String
    abstract val mensas: List<M>
  }

  @Serializable
  sealed class ApiMensa {
    abstract val id: String
    abstract val title: String
    abstract val mealTime: String
    abstract fun toMensa(): Mensa
  }

  object Api {
    @Serializable
    sealed class Root
  }
}
