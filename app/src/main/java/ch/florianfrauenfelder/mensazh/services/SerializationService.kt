package ch.florianfrauenfelder.mensazh.services

import kotlinx.serialization.json.Json

object SerializationService {
  val safeJson = Json { ignoreUnknownKeys = true }
  inline fun <reified T> deserializeList(json: String): List<T> = safeJson.decodeFromString(json)
  inline fun <reified T> deserialize(json: String): T = safeJson.decodeFromString(json)
  inline fun <reified T : Any> serialize(request: T) = Json.encodeToString(request)
}
