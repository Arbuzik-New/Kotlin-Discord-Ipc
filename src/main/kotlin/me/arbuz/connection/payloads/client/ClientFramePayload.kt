package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class ClientFramePayload(val cmd : Cmd, val args : ActivityArgsPayload, val nonce : String)

enum class Cmd {
    READY,
    SET_ACTIVITY,
    DISPATCH
}