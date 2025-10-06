package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class TimestampsPayload(val start : Long, val end : Long? = null)