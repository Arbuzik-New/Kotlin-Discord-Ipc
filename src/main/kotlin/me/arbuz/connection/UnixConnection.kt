package me.arbuz.connection

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.arbuz.DiscordIPC
import me.arbuz.User
import me.arbuz.connection.events.impl.ReadyEvent
import me.arbuz.connection.packets.server.Evt
import me.arbuz.connection.packets.server.ServerPacket
import me.arbuz.connection.payloads.client.ClientFramePayload
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

class UnixConnection(private val path : Path, private val ipc : DiscordIPC) : Connection() {

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
                        println("Received...")
                        val headBuffer = ByteBuffer.allocate(8)
                            .order(ByteOrder.LITTLE_ENDIAN)

                        while (headBuffer.hasRemaining()) {
                            val read = channel!!.read(headBuffer)
                            if (read == -1) return@thread
                        }

                        headBuffer.flip()

                        val opCode = headBuffer.int
                        val length = headBuffer.int

                        println("OpCode: $opCode, Length: $length")

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
                                if (element is JsonObject) Evt.valueOf(element["evt"]?.jsonPrimitive?.content!!)
                                else null
                            } catch (e : Exception) {null}

                            val cmd = try {
                                val element = Json.parseToJsonElement(response)
                                if (element is JsonObject) Cmd.valueOf(element["cmd"]?.jsonPrimitive?.content!!)
                                else null
                            } catch (e : Exception) {null}

                            val data = try {
                                val element = Json.parseToJsonElement(response)
                                if (element is JsonObject) element["data"]?.jsonObject
                                else null
                            } catch (e : Exception) {null}

                            val packet = ServerPacket(OpCode.entries[opCode], cmd, nonce, evt, data, response)

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
        if (!connected || channel == null || readSelector == null) throw RuntimeException("Already disconnected!")

        write(OpCode.CLOSE, "{}".toByteArray())

        connected = false

        readSelector!!.close()
        channel!!.close()

        readSelector = null
        channel = null
    }

    override fun handlePacket(packet : ServerPacket) {
        println(packet.response)
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

    override fun sendPayload(payload : ClientFramePayload) {
        val bytes = Json.encodeToString(payload).toByteArray(Charsets.UTF_8)
        println(String(bytes))
        write(OpCode.FRAME, bytes)
    }

    override fun handshake(applicationId : String) {
        val bytes = Json.encodeToString(HandshakePayload(1, applicationId)).toByteArray(Charsets.UTF_8)

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