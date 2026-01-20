package ch.florianfrauenfelder.mensazh.data.util

import ch.florianfrauenfelder.mensazh.data.providers.MensaProvider
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object SerializationService {
  val safeJson = Json { ignoreUnknownKeys = true }
  fun <L : MensaProvider.ApiLocation<*>> deserializeLocationList(
    json: String,
    serializer: KSerializer<L>,
  ): List<L> = safeJson.decodeFromString(ListSerializer(serializer), json)

  fun <R : MensaProvider.Api.Root> deserializeApiRoot(
    json: String,
    serializer: KSerializer<R>,
  ): R = safeJson.decodeFromString(serializer, json)

  inline fun <reified T> deserializeList(json: String): List<T> = safeJson.decodeFromString(json)
  inline fun <reified T : Any> serialize(request: T) = Json.encodeToString(request)
}
