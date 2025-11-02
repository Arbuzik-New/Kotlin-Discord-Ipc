package me.arbuz.connection.payloads.client

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable(with = ArgsPayloadSerializer::class)
sealed class ArgsPayload

object ArgsPayloadSerializer : JsonContentPolymorphicSerializer<ArgsPayload>(ArgsPayload::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ArgsPayload> {
        throw SerializationException("Deserialization not supported for ArgsPayload!")
    }
}