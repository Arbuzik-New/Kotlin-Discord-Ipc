package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class ClientFramePayload(val cmd : Cmd, val args : ArgsPayload? = null, val nonce : String)