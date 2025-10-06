package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class ActivityArgsPayload(val pid : Long, val activity: ActivityPayload? = null)