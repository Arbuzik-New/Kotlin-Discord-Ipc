package me.arbuz.connection

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.arbuz.User
import me.arbuz.connection.packets.client.ClientFramePacket
import me.arbuz.connection.packets.server.Evt
import me.arbuz.connection.packets.server.ServerPacket
import me.arbuz.connection.payloads.client.Cmd
import me.arbuz.connection.payloads.client.HandshakePayload
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class WindowsConnection(private val file : RandomAccessFile) : Connection() {

    private var connected = false

    private val packets = HashMap<String?, ServerPacket>()

    override fun connect(applicationId: String) {
        if (connected) throw RuntimeException("Already connected!")

        connected = true

        thread(name = "Discord IPC", start = true) {
            while (connected) {
                if (file.length() - file.filePointer < 8) continue

                val opCode = file.readIntLE()
                val length = file.readIntLE()

                if (length > 0) {
                    val responseBytes = ByteArray(length)
                    file.readFully(responseBytes)
                    val response = String(responseBytes, Charsets.UTF_8)

                    val nonce = try {
                        val element = Json.parseToJsonElement(response)
                        if (element is JsonObject) element["nonce"]?.jsonPrimitive?.content
                        else null
                    } catch (e: Exception) { null }

                    val evt = try {
                        val element = Json.parseToJsonElement(response)
                        if (element is JsonObject) element["evt"]?.jsonPrimitive?.content?.let { Evt.valueOf(it) }
                        else null
                    } catch (e: Exception) { null }

                    val cmd = try {
                        val element = Json.parseToJsonElement(response)
                        if (element is JsonObject) element["cmd"]?.jsonPrimitive?.content?.let { Cmd.valueOf(it) }
                        else null
                    } catch (e: Exception) { null }

                    val packet = ServerPacket(OpCode.entries[opCode], cmd, nonce, evt, response)
                    packets[nonce] = packet
                    handlePacket(packet)

                    thread(name = "Discord IPC Packet Remover") {
                        Thread.sleep(5000)
                        packets.remove(nonce)
                    }
                }
            }
        }

        handshake(applicationId)
    }

    private fun RandomAccessFile.readIntLE(): Int {
        val b1 = read().toByte()
        val b2 = read().toByte()
        val b3 = read().toByte()
        val b4 = read().toByte()
        return (b4.toInt() shl 24) or
                ((b3.toInt() and 0xFF) shl 16) or
                ((b2.toInt() and 0xFF) shl 8) or
                (b1.toInt() and 0xFF)
    }

    override fun disconnect() {
        if (!connected) throw RuntimeException("Already disconnected!")

        write(OpCode.CLOSE, "{}".toByteArray())
        connected = false
    }

    override fun handlePacket(packet: ServerPacket) {
        if (packet.nonce == "null" && packet.opCode == OpCode.FRAME && packet.cmd == Cmd.DISPATCH && packet.evt == Evt.READY) {
            val element = Json.parseToJsonElement(packet.response)
            if (element is JsonObject) {
                val data = element["data"]
                if (data is JsonObject) {
                    val user = data["user"]
                    if (user is JsonObject) {
                        val id = user["id"]
                        val username = user["username"]
                        val globalName = user["global_name"]
                        val avatar = user["avatar"]
                        val premiumType = user["premium_type"]

                        User.id = id!!.jsonPrimitive.content.toLong()
                        User.username = username!!.jsonPrimitive.content
                        User.globalName = globalName!!.jsonPrimitive.content
                        User.avatar = avatar!!.jsonPrimitive.content
                        User.premiumType = premiumType!!.jsonPrimitive.content.toInt()
                    }
                }
            }
        }
    }

    override fun handshake(applicationId: String) {
        val body = Json.encodeToString(HandshakePayload(1, applicationId))
        val bytes = body.toByteArray(Charsets.UTF_8)

        write(OpCode.HANDSHAKE, bytes)
    }

    override fun sendPacket(packet: ClientFramePacket) {
        val bytes = Json.encodeToString(packet.payload).toByteArray()
        write(packet.opCode, bytes)
    }

    override fun write(opCode: OpCode, bytes: ByteArray) {
        val buffer = ByteBuffer.allocate(8 + bytes.size)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(opCode.ordinal)
            .putInt(bytes.size)
            .put(bytes)
            .flip()

        write(buffer)
    }

    override fun write(buffer: ByteBuffer) {
        if (!connected) throw RuntimeException("Socket not connected!")

        file.write(buffer.array())
    }

    override fun waitResponse(nonce: String?, timeout: Double) {
        if (!connected) throw RuntimeException("Socket not connected!")
        val startTime = System.currentTimeMillis()
        while (packets.get(nonce) == null && System.currentTimeMillis() - startTime < timeout * 1000) Thread.sleep(1)
    }

    override fun getResponse(nonce: String?, timeout: Double): ServerPacket? {
        waitResponse(nonce, timeout);
        return packets.remove(nonce)
    }

}