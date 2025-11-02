package me.arbuz.connection.payloads.client

import kotlinx.serialization.Serializable

@Serializable
class AuthenticateArgsPayload(val access_token : String) : ArgsPayload()