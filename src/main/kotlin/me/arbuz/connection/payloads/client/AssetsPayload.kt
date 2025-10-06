package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
@JvmRecord
data class AssetsPayload(
    val large_image : String? = null, val large_text : String? = null, val large_url : String? = null,
    val small_image : String? = null, val small_text : String? = null, val small_url : String? = null
)