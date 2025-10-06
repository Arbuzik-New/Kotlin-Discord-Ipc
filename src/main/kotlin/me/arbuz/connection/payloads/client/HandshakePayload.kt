package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class HandshakePayload(val v : Int, val client_id : String)