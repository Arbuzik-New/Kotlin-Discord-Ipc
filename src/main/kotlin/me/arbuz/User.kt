package me.arbuz

import java.io.ByteArrayOutputStream
import java.net.URI

class User(val id : Long, val username : String, val globalName : String, val avatar : String, val premiumType : Int) {
    var bytes : ByteArray? = byteArrayOf()

    fun avatarLink() : String {
        return "https://cdn.discordapp.com/avatars/$id/$avatar.png"
    }

    fun downloadAvatar() {
        val link = avatarLink()
        val url = URI.create(link).toURL()

        url.openStream().use{ stream ->
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            var read : Int = stream.read(buffer)

            while (read != -1) {
                output.write(buffer, 0, read)
                read = stream.read(buffer)
            }

            bytes = output.toByteArray()
        }
    }

}