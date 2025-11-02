package me.arbuz

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.jsonPrimitive
import me.arbuz.connection.events.impl.ReadyEvent
import me.arbuz.connection.payloads.client.ActivityPayload
import me.arbuz.connection.payloads.client.AuthenticateArgsPayload
import me.arbuz.connection.payloads.client.AuthorizeArgsPayload
import me.arbuz.connection.payloads.client.ClientFramePayload
import me.arbuz.connection.payloads.client.Cmd
import me.arbuz.connection.payloads.client.Scope

fun main() {

    val ipc = DiscordIPC("1424491012100849725")

    ipc.onEvent<ReadyEvent> {
//        ipc.sendPayload(ClientFramePayload(Cmd.SUBSCRIBE, null, "Hello"))
////        ipc.sendPayload(ClientFramePayload(Cmd.AUTHORIZE, AuthorizeArgsPayload("1424491012100849725", listOf(Scope.RPC, Scope.RPC_VOICE_READ)), "Authorize"))
////        ipc.setRPC(ActivityPayload(details = "World!"))
//        Thread.sleep(10000)
//        ipc.stop()
        // Простая команда которая точно работает
        ipc.setRPC(ActivityPayload(details = "Testing RPC"))

        // Ждем ответ на SET_ACTIVITY (он должен прийти)
        val response = ipc.connection.getResponse("SetRPC", 5.0)
        if (response != null) {
            println("SET_ACTIVITY RESPONSE: ${response.response}")
        } else {
            println("NO RESPONSE FOR SET_ACTIVITY")
        }

        Thread.sleep(5000)
        ipc.stop()
    }

//    DiscordIPC.start("1424491012100849725")
//    DiscordIPC.wait(5.0)
//
//    DiscordIPC.connection.sendPayload(ClientFramePayload(Cmd.AUTHORIZE, AuthorizeArgsPayload("1424491012100849725", listOf(Scope.RPC, Scope.RPC_VOICE_READ)), "Authorize"))
//    val response = DiscordIPC.connection.getResponse("Authorize", 60.0)
//
//    if (response != null) {
//        println(response.response)
//
//        if (response.cmd == Cmd.AUTHORIZE) {
//
//            val code = try {
//                if (response.data != null) response.data["code"]!!.jsonPrimitive.content
//                else null
//            } catch (e : Exception) {null}
//
//            println(code)
//
//            Thread.sleep(60_000)
//
//            if (code != null) {
//                DiscordIPC.connection.sendPayload(ClientFramePayload(Cmd.AUTHENTICATE, AuthenticateArgsPayload(code), "Authenticate"))
//                println(DiscordIPC.connection.getResponse("Authenticate", 5.0)?.response)
//
//                DiscordIPC.connection.sendPayload(ClientFramePayload(Cmd.GET_VOICE_SETTINGS, null, "Hello"))
//                println(DiscordIPC.connection.getResponse("Hello", 5.0)?.response)
//            }
//
//        }
//
//    }
//
//    DiscordIPC.stop()
}