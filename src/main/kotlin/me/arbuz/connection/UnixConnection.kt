package me.arbuz.connection

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.arbuz.User
import me.arbuz.connection.packets.client.ClientFramePacket
import me.arbuz.connection.packets.server.Evt
import me.arbuz.connection.packets.server.ServerPacket
import me.arbuz.connection.payloads.client.Cmd
import me.arbuz.connection.payloads.client.HandshakePayload
import java.net.UnixDomainSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import java.nio.file.Path
import kotlin.collections.set
import kotlin.concurrent.thread

class UnixConnection(val path : Path) : Connection() {

    private var readSelector : Selector? = null
    private var channel : SocketChannel? = null
    private var connected = false

    private val packets = HashMap<String?, ServerPacket>()

    override fun connect(applicationId : String) {
        if (connected) throw RuntimeException("Already connected!")

        readSelector = Selector.open()
        channel = SocketChannel.open(UnixDomainSocketAddress.of(path))

        channel!!.configureBlocking(false)

        channel!!.register(readSelector, SelectionKey.OP_READ)

        connected = true

        thread(name = "Discord IPC", start = true) {
            while (connected) {

                readSelector!!.select()

                if (!connected) break

                val keys = readSelector!!.selectedKeys()
                val iterator = keys.iterator()

                while (iterator.hasNext()) {
                    val key = iterator.next()
                    iterator.remove()

                    if (key.isReadable) {
                        val headBuffer = ByteBuffer.allocate(8)
                            .order(ByteOrder.LITTLE_ENDIAN)

                        while (headBuffer.hasRemaining()) {
                            val read = channel!!.read(headBuffer)
                            if (read == -1) return@thread
                        }

                        headBuffer.flip()

                        val opCode = headBuffer.int
                        val length = headBuffer.int

                        if (length > 0) {
                            val responseBuffer = ByteBuffer.allocate(length)
                                .order(ByteOrder.LITTLE_ENDIAN)

                            while (responseBuffer.hasRemaining()) {
                                val read = channel!!.read(responseBuffer)
                                if (read == -1) return@thread
                            }

                            responseBuffer.flip()

                            val response = String(responseBuffer.array())

                            val nonce = try {
                                val element = Json.parseToJsonElement(response)
                                if (element is JsonObject) element["nonce"]?.jsonPrimitive?.content
                                else null
                            } catch (e : Exception) {null}

                            val evt = try {
                                val element = Json.parseToJsonElement(response)
                                if (element is JsonObject) element["evt"]?.jsonPrimitive?.content!!
                                else "null"
                            } catch (e : Exception) {"null"}

                            val cmd = try {
                                val element = Json.parseToJsonElement(response)
                                if (element is JsonObject) element["cmd"]?.jsonPrimitive?.content!!
                                else "null"
                            } catch (e : Exception) {"null"}

                            val packet = ServerPacket(OpCode.entries[opCode], Cmd.valueOf(cmd), nonce, Evt.valueOf(evt), response)

                            packets[nonce] = packet

                            handlePacket(packet)

                            thread(name = "Discord IPC Packet Remover", start = true) {
                                Thread.sleep(5000)
                                packets.remove(nonce)
                            }
                        }
                    }
                }
            }
        }

        handshake(applicationId)
    }

    override fun disconnect() {
        if (!connected) throw RuntimeException("Already disconnected!")

        write(OpCode.CLOSE, "{}".toByteArray())

        connected = false

        readSelector!!.close()
        channel!!.close()

        readSelector = null
        channel = null
    }

    override fun handlePacket(packet : ServerPacket) {
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

    override fun sendPacket(packet : ClientFramePacket) {
        val bytes = Json.encodeToString(packet.payload).toByteArray()

        write(packet.opCode, bytes)
    }

    override fun handshake(applicationId : String) {
        val body = Json.encodeToString(HandshakePayload(1, applicationId))
        val bytes = body.toByteArray(Charsets.UTF_8)

        write(OpCode.HANDSHAKE, bytes)
    }

    override fun write(opCode : OpCode, bytes : ByteArray) {
        val buffer = ByteBuffer.allocate(8 + bytes.size)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(opCode.ordinal)
            .putInt(bytes.size)
            .put(bytes)
            .flip()

        write(buffer)
    }

    override fun write(buffer : ByteBuffer) {
        if (!connected) throw RuntimeException("Socket not connected!")

        channel!!.write(buffer)
    }

    override fun waitResponse(nonce : String?, timeout : Double) {
        if (!connected) throw RuntimeException("Socket not connected!")
        val startTime = System.currentTimeMillis()
        while (packets.get(nonce) == null && System.currentTimeMillis() - startTime < timeout * 1000) Thread.sleep(1)
    }

    override fun getResponse(nonce : String?, timeout : Double) : ServerPacket? {
        waitResponse(nonce, timeout)
        return packets.remove(nonce)
    }

}