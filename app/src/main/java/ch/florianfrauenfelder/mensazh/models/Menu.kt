package ch.florianfrauenfelder.mensazh.models

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Parcelize
@Serializable
data class Menu(
  val title: String,
  val description: String,
  val price: List<String>,
  val allergens: String?,
) : Parcelable {
  @IgnoredOnParcel
  @Transient
  var mensa: Mensa? = null

  override fun toString() = title + description

  companion object {
    val dummy = Menu(
      title = "vitality",
      description = "Gnocchi mit Quornwürfel und Tomatensauce",
      price = listOf("14.50", "20.50", "21.30"),
      allergens = "Sellerie, Nüsse, Schweinefleisch",
    )
  }
}
