package me.arbuz

import me.arbuz.connection.Connection
import me.arbuz.connection.UnixConnection
import me.arbuz.connection.packets.client.ClientFramePacket
import me.arbuz.connection.payloads.client.ActivityArgsPayload
import me.arbuz.connection.payloads.client.ActivityPayload
import me.arbuz.connection.payloads.client.AssetsPayload
import me.arbuz.connection.payloads.client.ClientFramePayload
import me.arbuz.connection.payloads.client.Cmd
import me.arbuz.connection.payloads.client.TimestampsPayload
import java.nio.file.Files
import java.nio.file.Path

object DiscordIPC {

    private val unixTempPaths = arrayOf("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")
    private val connection = getConnection()

    private fun getConnection() : Connection {
        val system = System.getProperty("os.name")

        if (system.lowercase().contains("win")) {
            for (i in 0..9) {
                val pipePath = "\\\\?\\pipe\\discord-ipc-$i"

                throw RuntimeException("Windows not supported!")
            }
        } else {
            val name = unixTempPaths.firstNotNullOfOrNull { System.getenv(it) }
            val tmp = name ?: "/tmp"

            for (i in 0..9) {
                val path = Path.of("$tmp/discord-ipc-$i")
                if (Files.exists(path)) return UnixConnection(path)
            }
        }

        throw RuntimeException("Failed to find socket!")
    }

    fun wait(timeout : Double) {
        User.wait(timeout)
    }

    fun start(applicationId : String) {
        connection.connect(applicationId)
    }

    fun stop() {
        connection.disconnect()
        User.id = null
        User.username = null
        User.globalName = null
        User.avatar = null
        User.premiumType = null
    }

    fun sendPacket(packet : ClientFramePacket) {
        connection.sendPacket(packet)
    }

    fun setRPC(activity : ActivityPayload?) {
        connection.sendPacket(ClientFramePacket(
            ClientFramePayload(
                Cmd.SET_ACTIVITY, ActivityArgsPayload(
                    ProcessHandle.current().pid(), activity
                ), "SetRPC"
            )
        ))

        println(connection.getResponse("SetRPC", 25.0))
    }

}