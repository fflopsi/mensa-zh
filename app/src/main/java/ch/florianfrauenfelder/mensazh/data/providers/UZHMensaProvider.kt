package ch.florianfrauenfelder.mensazh.data.providers

import ch.florianfrauenfelder.mensazh.data.local.room.FetchInfoDao
import ch.florianfrauenfelder.mensazh.data.local.room.MenuDao
import ch.florianfrauenfelder.mensazh.data.local.room.RoomMenu
import ch.florianfrauenfelder.mensazh.data.util.AssetService
import ch.florianfrauenfelder.mensazh.data.util.SerializationService
import ch.florianfrauenfelder.mensazh.domain.model.Mensa
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI
import java.util.Locale
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class UZHMensaProvider(menuDao: MenuDao, fetchInfoDao: FetchInfoDao, assetService: AssetService) :
  MensaProvider<UZHMensaProvider.UzhLocation, UZHMensaProvider.UzhMensa, UZHMensaProvider.UzhApi.Root>(
    menuDao = menuDao,
    fetchInfoDao = fetchInfoDao,
    assetService = assetService,
  ) {
  override val institution = Institution.UZH
  override val locationsFile = "uzh/locations_zfv.json"
  override val locationSerializer = UzhLocation.serializer()
  override val apiRootSerializer = UzhApi.Root.serializer()

  override fun buildRequest(destination: Destination, language: Language): Request {
    val isoDateString =
      Clock.System
        .now()
        .run { if (destination == Destination.Tomorrow) plus(1.days) else this }
        .toString()
    val dayQuery =
      "date { weekdayNumber }" +
        "menuItems {" +
        "  prices { amount }" +
        "  ... on OutletMenuItemDish {" +
        "    category { name path }" +
        "    dish {" +
        "      allergens { allergen { name } }" +
        "      name_i18n { label locale }" +
        "      media { media { url } }" +
        "      isVegetarian isVegan" +
        "    }" +
        "  }" +
        "}"
    val requestBody = when (destination) {
      Destination.Today, Destination.Tomorrow -> {
        "{\"query\":\"query Client {" +
          "organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) {" +
          "  outlets(take: 100) { slug calendar {" +
          "    day(date: \\\"$isoDateString\\\") { $dayQuery }" +
          "  }" +
          "} }}\",\"operationName\":\"Client\"}"
      }
      Destination.ThisWeek, Destination.NextWeek -> {
        "{\"query\":\"query Client {" +
          "organisation(where: {id: \\\"cm1tjaby3002o72q4lhhak996\\\", tenantId: \\\"zfv\\\"}) {" +
          "  outlets(take: 100) { slug calendar {" +
          "    week${if (destination == Destination.NextWeek) "(week: \\\"next\\\")" else ""} {" +
          "      daily { $dayQuery }" +
          "    }" +
          "  } }" +
          "}" +
          "}\",\"operationName\":\"Client\"}"
      }
    }

    return Request
      .Builder()
      .url("https://api.zfv.ch/graphql")
      .post(requestBody.toRequestBody("application/json".toMediaType()))
      .header("Accept", "*/*")
      .header("Content-Type", "application/json")
      .header(
        "api-key",
        "Y21nMGdleDdkN2xwbXM2MHRhemIyZWl1MjpBS0dLVkdPSnM5RjJEeDdrVUdySnZGaGZ4dWtpUUN2UHBaRjJrNUt5RENEQldObHRNNUZJUk84MU5JMkdCdmc3",
      )
      .build()
  }

  override fun extractMenus(root: UzhApi.Root, monday: LocalDate, language: Language) =
    root.data?.organisation?.outlets.orEmpty().flatMap { outlet ->
      val uzhMensa = apiMensas.find { it.slug == outlet.slug } ?: return@flatMap emptyList()
      val days =
        outlet.calendar?.week?.daily ?: outlet.calendar?.day?.let { listOf(it) }
        ?: return@flatMap emptyList()
      days.flatMap { day ->
        day.menuItems.orEmpty().flatMapIndexed { index, menuItem ->
          menuItem.dish?.nameI18n.orEmpty()
            .filter { name -> (name.locale ?: "") in Language.entries.map { it.toString() } }
            .mapNotNull { i18nName ->
              parseApiMenu(
                menuItem = menuItem,
                uzhMensa = uzhMensa,
                index = index,
                i18nName = i18nName,
                date = monday.plus(day.date.weekdayNumber - 1, DateTimeUnit.DAY),
              )
            }
        }
      }
    }

  private fun parseApiMenu(
    menuItem: UzhApi.MenuItem,
    uzhMensa: UzhMensa,
    index: Int,
    i18nName: UzhApi.I18nName,
    date: LocalDate,
  ): RoomMenu? =
    if (menuItem.dish == null || menuItem.category?.name == null || i18nName.label == null) null
    else RoomMenu(
      mensaId = uzhMensa.id,
      index = index,
      language = i18nName.locale!!,
      title = menuItem.category.name,
      description = i18nName.label.replaceFirst(",", "\n").replace("\n ", "\n"),
      price = SerializationService.serialize(
        menuItem.prices?.mapNotNull { it.amount?.toFloat() }?.sorted()
          ?.map { String.format(Locale.US, "%.2f", it) } ?: emptyList(),
      ),
      allergens = menuItem.dish.allergens?.mapNotNull { it.allergen?.name }
        ?.joinToString(separator = ", "),
      isVegetarian = menuItem.dish.isVegetarian ?: false,
      isVegan = menuItem.dish.isVegan ?: false,
      imageUrl = menuItem.dish.media?.firstOrNull()?.media?.url,
      date = date.toString(),
    ).run { if (hasClosedNotice) null else this }

  override suspend fun updateFetchInfo(destination: Destination, language: Language) =
    Language.entries.forEach { insertFetchInfo(destination, it) }

  @Serializable
  data class UzhLocation(
    override val id: String,
    override val title: String,
    override val mensas: List<UzhMensa>,
  ) : ApiLocation<UzhMensa>()

  @Serializable
  data class UzhMensa(
    override val id: String,
    override val title: String,
    override val mealTime: String,
    val infoUrlSlug: String,
    val slug: String,
    val categoryPath: String? = null,
  ) : ApiMensa() {
    override fun toMensa() = Mensa(
      id = UUID.fromString(id),
      title = title,
      mealTime = mealTime,
      url = URI("https://www.mensa.uzh.ch/de/menueplaene/$infoUrlSlug.html"),
    )
  }

  object UzhApi {
    @Serializable
    data class Root(val data: Data? = null) : Api.Root()

    @Serializable
    data class Data(val organisation: Organisation? = null)

    @Serializable
    data class Organisation(val outlets: List<Outlet>? = null)

    @Serializable
    data class Outlet(val slug: String? = null, val calendar: Calendar? = null)

    @Serializable
    data class Calendar(val day: Day? = null, val week: Week? = null)

    @Serializable
    data class Week(val daily: List<Day>)

    @Serializable
    data class Day(val date: Date, val menuItems: List<MenuItem>? = null)

    @Serializable
    data class Date(val weekdayNumber: Int)

    @Serializable
    data class MenuItem(
      val prices: List<Price>? = null,
      val category: Category? = null,
      val dish: Dish? = null,
    )

    @Serializable
    data class Price(val amount: String? = null)

    @Serializable
    data class Category(val name: String? = null, val path: List<String>? = null)

    @Serializable
    data class Dish(
      @SerialName("name_i18n") val nameI18n: List<I18nName>? = null,
      val allergens: List<AllergenContainer>? = null,
      val media: List<MediaContainer>? = null,
      val isVegetarian: Boolean? = null,
      val isVegan: Boolean? = null,
    )

    @Serializable
    data class I18nName(val label: String? = null, val locale: String? = null)

    @Serializable
    data class AllergenContainer(val allergen: Allergen? = null)

    @Serializable
    data class Allergen(val name: String? = null)

    @Serializable
    data class MediaContainer(val media: Media? = null)

    @Serializable
    data class Media(val url: String? = null)
  }
}
