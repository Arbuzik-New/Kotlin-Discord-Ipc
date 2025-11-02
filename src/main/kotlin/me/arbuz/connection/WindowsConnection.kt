package me.arbuz.connection

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.arbuz.DiscordIPC
import me.arbuz.User
import me.arbuz.connection.events.impl.ReadyEvent
import me.arbuz.connection.packets.client.ClientFramePacket
import me.arbuz.connection.packets.server.Evt
import me.arbuz.connection.packets.server.ServerPacket
import me.arbuz.connection.payloads.client.ClientFramePayload
import me.arbuz.connection.payloads.client.Cmd
import me.arbuz.connection.payloads.client.HandshakePayload
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class WindowsConnection(private val file : RandomAccessFile, private val ipc : DiscordIPC) : Connection() {

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

                println("OpCode: $opCode, Length: $length")

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

                    val data = try {
                        val element = Json.parseToJsonElement(response)
                        if (element is JsonObject) element["data"]?.jsonObject
                        else null
                    } catch (e : Exception) {null}

                    val packet = ServerPacket(OpCode.entries[opCode], cmd, nonce, evt, data, response)
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
            if (packet.data is JsonObject) {
                val user = packet.data["user"]
                if (user is JsonObject) {
                    val id = user["id"]!!.jsonPrimitive.content.toLong()
                    val username = user["username"]!!.jsonPrimitive.content
                    val globalName = user["global_name"]!!.jsonPrimitive.content
                    val avatar = user["avatar"]!!.jsonPrimitive.content
                    val premiumType = user["premium_type"]!!.jsonPrimitive.content.toInt()

                    ipc.postEvent(ReadyEvent(User(id, username, globalName, avatar, premiumType)))
                }
            }
        }
    }

    override fun handshake(applicationId: String) {
        val body = Json.encodeToString(HandshakePayload(1, applicationId))
        val bytes = body.toByteArray(Charsets.UTF_8)

        write(OpCode.HANDSHAKE, bytes)
    }

    override fun sendPayload(payload: ClientFramePayload) {
        val bytes = Json.encodeToString(payload).toByteArray()
        write(OpCode.FRAME, bytes)
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
        waitResponse(nonce, timeout)
        return packets.remove(nonce)
    }

}