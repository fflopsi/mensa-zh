package ch.florianfrauenfelder.mensazh.data.local.room

import androidx.room.TypeConverter
import ch.florianfrauenfelder.mensazh.data.util.SerializationService
import ch.florianfrauenfelder.mensazh.domain.navigation.Destination
import ch.florianfrauenfelder.mensazh.domain.value.Institution
import ch.florianfrauenfelder.mensazh.domain.value.Language
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class Converters {
  @TypeConverter
  fun institutionToString(institution: Institution?): String? = institution?.code

  @TypeConverter
  fun stringToInstitution(code: String?): Institution? = code?.let(Institution::fromCode)

  @TypeConverter
  fun destinationToString(destination: Destination?): String? = destination?.code

  @TypeConverter
  fun stringToDestination(code: String?): Destination? = code?.let(Destination::fromCode)

  @TypeConverter
  fun languageToString(language: Language?): String? = language?.code

  @TypeConverter
  fun stringToLanguage(code: String?): Language? = code?.let(Language::fromCode)

  @TypeConverter
  fun dateToString(date: LocalDate?): String? = date?.toString()

  @TypeConverter
  fun stringToDate(string: String?): LocalDate? = string?.let(LocalDate::parse)

  @TypeConverter
  fun uuidToString(uuid: Uuid?): String? = uuid?.toString()

  @TypeConverter
  fun stringListToString(list: List<String>?): String? = list?.let(SerializationService::serialize)

  @TypeConverter
  fun stringToStringList(string: String?): List<String>? =
    string?.let(SerializationService::deserializeList)
}
