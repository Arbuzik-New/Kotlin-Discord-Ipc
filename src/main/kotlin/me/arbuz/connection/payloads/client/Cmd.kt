package me.arbuz.connection.payloads.client

enum class Cmd {
    READY,
    AUTHORIZE,
    AUTHENTICATE,
    SET_ACTIVITY,
    DISPATCH,
    GET_VOICE_SETTINGS,
    SUBSCRIBE
}