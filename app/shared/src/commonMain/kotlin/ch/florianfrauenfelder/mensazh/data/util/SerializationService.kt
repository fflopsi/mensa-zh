package ch.florianfrauenfelder.mensazh.data.util

import kotlinx.serialization.json.Json

object SerializationService {
  val safeJson = Json { ignoreUnknownKeys = true }
  inline fun <reified T : Any> deserializeList(json: String): List<T> = safeJson.decodeFromString(json)
  inline fun <reified T : Any> serialize(request: T) = Json.encodeToString(request)
}
