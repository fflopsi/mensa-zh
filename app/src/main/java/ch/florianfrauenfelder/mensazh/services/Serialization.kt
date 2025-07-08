package ch.florianfrauenfelder.mensazh.services

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.net.URI
import java.util.UUID

object UUIDSerializer : KSerializer<UUID> {
  override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
  override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
  override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

object URISerializer : KSerializer<URI> {
  override val descriptor = PrimitiveSerialDescriptor("URI", PrimitiveKind.STRING)
  override fun deserialize(decoder: Decoder): URI = URI.create(decoder.decodeString())
  override fun serialize(encoder: Encoder, value: URI) = encoder.encodeString(value.toString())
}

object SerializationService {
  val safeJson = Json { ignoreUnknownKeys = true }
  inline fun <reified T> deserializeList(json: String): List<T> = safeJson.decodeFromString(json)
  inline fun <reified T> deserialize(json: String): T = safeJson.decodeFromString(json)
  inline fun <reified T : Any> serialize(request: T) = Json.encodeToString(request)
}
