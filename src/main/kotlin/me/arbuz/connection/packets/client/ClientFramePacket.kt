package me.arbuz.connection.packets.client

import me.arbuz.connection.OpCode
import me.arbuz.connection.packets.Packet
import me.arbuz.connection.payloads.client.ClientFramePayload

data class ClientFramePacket(val payload : ClientFramePayload) : Packet(OpCode.FRAME)