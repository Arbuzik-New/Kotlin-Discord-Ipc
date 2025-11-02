package me.arbuz

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.arbuz.connection.Connection
import me.arbuz.connection.UnixConnection
import me.arbuz.connection.WindowsConnection
import me.arbuz.connection.events.Event
import me.arbuz.connection.events.impl.ReadyEvent
import me.arbuz.connection.packets.server.ServerPacket
import me.arbuz.connection.payloads.client.ActivityArgsPayload
import me.arbuz.connection.payloads.client.ActivityPayload
import me.arbuz.connection.payloads.client.ClientFramePayload
import me.arbuz.connection.payloads.client.Cmd
import java.io.RandomAccessFile
import java.nio.file.Files
import java.nio.file.Path

class DiscordIPC(val appId : String) {

    private val unixTempPaths = arrayOf("XDG_RUNTIME_DIR", "TMPDIR", "TMP", "TEMP")
    val registeredEvents = HashMap<Class<Event>, Runnable>()
    val registeredPackets = HashMap<Class<ServerPacket>, Runnable>()
    val connection : Connection = getConnectionForCurrentSystem()

    var user : User? = null
        private set

    init {
        connection.connect(appId)
    }

    private fun getConnectionForCurrentSystem() : Connection {
        val system = System.getProperty("os.name")

        if (system.lowercase().contains("windows")) {
            for (i in 0..9) {
                val pipePath = "\\\\.\\pipe\\discord-ipc-$i"
                try {
                    val file = RandomAccessFile(pipePath, "rw")
                    return WindowsConnection(file, this)
                } catch (e : Exception) { }
            }
        } else {
            val name = unixTempPaths.firstNotNullOfOrNull { System.getenv(it) }
            val tmp = name ?: "/tmp"

            for (i in 0..9) {
                val path = Path.of("$tmp/discord-ipc-$i")
                if (Files.exists(path)) return UnixConnection(path, this)
            }
        }

        throw RuntimeException("Failed to find socket!")
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Event> onEvent(runnable : Runnable) {
        registeredEvents[T::class.java as Class<Event>] = runnable
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : ServerPacket> onPacket(runnable : Runnable) {
        registeredPackets[T::class.java as Class<ServerPacket>] = runnable
    }

    fun postEvent(event : Event) {
        if (event is ReadyEvent) user = event.user
        CoroutineScope(Dispatchers.Default).run{ registeredEvents[event.javaClass]?.run() }
    }

    fun postPacket(packet: ServerPacket) {
        CoroutineScope(Dispatchers.Default).run{ registeredPackets[packet.javaClass]?.run() }
    }

    fun waitHandshake(timeout : Double) {
        val start = System.currentTimeMillis()
        while (user == null && System.currentTimeMillis() - start < timeout * 1000) Thread.sleep(1)
    }

    fun stop() {
        connection.disconnect()
        user = null
    }

    fun sendPayload(payload : ClientFramePayload) {
        connection.sendPayload(payload)
    }

    fun setRPC(activity : ActivityPayload?) {
        connection.sendPayload(
            ClientFramePayload(
                Cmd.SET_ACTIVITY, ActivityArgsPayload(
                    ProcessHandle.current().pid(), activity
                ), "SetRPC"
        ))
    }

}