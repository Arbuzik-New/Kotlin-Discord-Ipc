package me.arbuz

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URI
import java.net.URL

object User {
    var id : Long? = null
    var username : String? = null
    var globalName : String? = null
    var avatar : String? = null
    var premiumType : Int? = null
    var bytes : ByteArray? = null

    fun avatarLink() : String? {
        if (avatar == null) return null
        return "https://cdn.discordapp.com/avatars/$id/$avatar.png"
    }

    fun downloadAvatar() {
        val link = avatarLink() ?: throw RuntimeException("Failed to get avatar link!")
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