package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

//@Serializable
//data class ActivityButton(val label : String, val url : String)

@Serializable
class ActivityPayload(
    val state : String? = null, val state_url : String? = null, val details : String? = null, val details_url : String? = null,
    val timestamps : TimestampsPayload? = null, val assets : AssetsPayload? = null,
//    val buttons : List<ActivityButton>? = null
) : ClientPayload()