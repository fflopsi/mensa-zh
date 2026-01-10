package ch.florianfrauenfelder.mensazh.data.util

import ch.florianfrauenfelder.mensazh.data.providers.MensaProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object SerializationService {
  val safeJson = Json { ignoreUnknownKeys = true }
  fun <L : MensaProvider.ApiLocation<M>, M : MensaProvider.ApiMensa> deserializeLocationList(
    json: String,
    serializer: KSerializer<L>,
  ): List<L> = safeJson.decodeFromString(ListSerializer(serializer), json)

  inline fun <reified T> deserializeList(json: String): List<T> = safeJson.decodeFromString(json)
  inline fun <reified T> deserialize(json: String): T = safeJson.decodeFromString(json)
  inline fun <reified T : Any> serialize(request: T) = Json.encodeToString(request)
}
