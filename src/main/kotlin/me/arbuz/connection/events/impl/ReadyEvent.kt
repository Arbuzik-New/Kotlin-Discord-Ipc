package me.arbuz.connection.events.impl

import me.arbuz.User
import me.arbuz.connection.events.Event

class ReadyEvent(val user : User) : Event()