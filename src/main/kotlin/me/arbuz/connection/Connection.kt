package me.arbuz.connection

import me.arbuz.connection.packets.client.ClientFramePacket
import me.arbuz.connection.packets.server.ServerPacket
import java.nio.ByteBuffer

abstract class Connection {

    abstract fun connect(applicationId : String)
    abstract fun disconnect()

    abstract fun handlePacket(packet : ServerPacket)

    abstract fun handshake(applicationId : String)
    abstract fun sendPacket(packet : ClientFramePacket)

    abstract fun write(opCode: OpCode, bytes : ByteArray)
    abstract fun write(buffer : ByteBuffer)

    abstract fun waitResponse(nonce : String?, timeout : Double)
    abstract fun getResponse(nonce : String?, timeout : Double) : ServerPacket?

}