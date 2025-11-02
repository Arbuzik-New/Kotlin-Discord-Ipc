package me.arbuz.connection.packets.server

import kotlinx.serialization.json.JsonObject
import me.arbuz.connection.OpCode
import me.arbuz.connection.packets.Packet
import me.arbuz.connection.payloads.client.Cmd

open class ServerPacket(opCode: OpCode, val cmd : Cmd?, val nonce : String?, val evt : Evt?, val data : JsonObject?, val response : String) : Packet(opCode)

enum class Evt {
    READY,
    ERROR
}