package me.arbuz.connection.payloads.client

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class AuthorizeArgsPayload(val client_id : String, val scopes : List<Scope>) : ArgsPayload()

object ScopeSerializer : KSerializer<Scope> {
    override val descriptor = PrimitiveSerialDescriptor("Scope", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Scope) = encoder.encodeString(value.scopeName)
    override fun deserialize(decoder: Decoder): Scope = Scope.valueOf(decoder.decodeString().uppercase().replace('.', '_'))
}

object PromptSerializer : KSerializer<Prompt> {
    override val descriptor = PrimitiveSerialDescriptor("Prompt", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Prompt) = encoder.encodeString(value.promptName)
    override fun deserialize(decoder: Decoder): Prompt = Prompt.valueOf(decoder.decodeString().uppercase())
}

@Serializable(with = ScopeSerializer::class)
enum class Scope(val scopeName : String) {
    RPC("rpc"),
    RPC_VOICE_READ("rpc.voice.read"),
}

@Serializable(with = PromptSerializer::class)
enum class Prompt(val promptName : String) {
    NONE("none"),
    CONSENT("consent")
}