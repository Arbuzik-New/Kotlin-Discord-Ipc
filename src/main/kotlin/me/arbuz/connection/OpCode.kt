package me.arbuz.connection

enum class OpCode {
    HANDSHAKE,
    FRAME,
    CLOSE,
    PING,
    PONG
}