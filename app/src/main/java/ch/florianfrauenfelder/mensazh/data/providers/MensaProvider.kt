package ch.florianfrauenfelder.mensazh.data.providers

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfo
import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.local.room.RoomMenu
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.data.util.SerializationService
import ch.florianfrauenfelder.mensazh.domain.model.Location
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.model.Menu
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import ch.florianfrauenfelder.mensazh.ui.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import java.util.UUID
import java.util.concurrent.TimeUnit

abstract class MensaProvider<L : MensaProvider.ApiLocation<M>, M : MensaProvider.ApiMensa>(
  private val menuDao: MenuDao,
  private val fetchInfoDao: FetchInfoDao,
  private val assetService: AssetService,
) {
  abstract val institution: Institution
  protected abstract val locationsFile: String
  protected abstract val locationSerializer: KSerializer<L>
  protected val apiMensas = mutableListOf<M>()
  protected val client = OkHttpClient
    .Builder()
    .connectTimeout(5, TimeUnit.SECONDS)
    .readTimeout(5, TimeUnit.SECONDS)
    .build()

  suspend fun getLocations(): List<Location> {
    val json: String = assetService.readStringFile(locationsFile) ?: return emptyList()
    return SerializationService
      .deserializeLocationList(json, locationSerializer)
      .map { apiLocation ->
        Location(
          id = UUID.fromString(apiLocation.id),
          title = apiLocation.title,
          mensas = apiLocation.mensas.map {
            apiMensas += it
            it.toMensa().toMensaState()
          },
        )
      }
  }

  abstract suspend fun fetchMenus(
    language: Language,
    destination: Destination,
  )

  protected abstract suspend fun fetchJson(language: Language, destination: Destination): String?

  protected suspend fun cacheMenu(
    facilityId: String,
    date: LocalDate,
    language: Language,
    index: Int,
    menu: Menu,
  ) = menuDao.insertMenu(menu.toRoomMenu(facilityId, date, language, index))

  protected suspend fun cacheMenus(
    facilityId: String,
    date: LocalDate,
    language: Language,
    index: Int,
    menus: List<Menu>,
  ) = menuDao.insertMenus(menus.map { it.toRoomMenu(facilityId, date, language, index) })

  protected suspend fun updateFetchInfo(
    destination: Destination,
    language: Language,
  ) {
    if (institution == Institution.ETH && destination != Destination.NextWeek) {
      listOf(Destination.Today, Destination.Tomorrow, Destination.ThisWeek).forEach {
        fetchInfoDao.insertFetchInfo(
          FetchInfo(
            institution = institution.toString(),
            destination = it.toString(),
            language = language.toString(),
          ),
        )
      }
    } else {
      fetchInfoDao.insertFetchInfo(
        FetchInfo(
          institution = institution.toString(),
          destination = destination.toString(),
          language = language.toString(),
        ),
      )
    }
  }

  protected abstract fun isNoMenuNotice(menu: Menu, language: Language): Boolean

  protected fun normalizeText(text: String): String =
    // remove too much whitespace
    text.apply {
      replace("  ", " ")
      replace(" \n", "\n")
      replace("\n ", "\n")
    }

  protected fun fallbackLanguage(language: Language) = !language

  private fun Menu.toRoomMenu(
    facilityId: String,
    date: LocalDate,
    language: Language,
    index: Int,
  ) = RoomMenu(
    mensaId = facilityId,
    index = index,
    language = language.toString(),
    title = title,
    description = description,
    price = SerializationService.serialize(price),
    allergens = allergens,
    isVegetarian = isVegetarian,
    isVegan = isVegan,
    imageUrl = imageUrl,
    date = date.toString(),
  )

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
}
