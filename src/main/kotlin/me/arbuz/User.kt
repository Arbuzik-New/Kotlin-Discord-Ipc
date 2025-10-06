package me.arbuz

object User {
    var id : Long? = null
    var username : String? = null
    var globalName : String? = null
    var avatar : String? = null
    var premiumType : Int? = null

    fun avatarLink() : String? {
        if (avatar == null) return null
        return "https://cdn.discordapp.com/avatars/$id/$avatar.png"
    }

    fun wait(timeout : Double) {
        val start = System.currentTimeMillis()
        while (
            (id == null || username == null || globalName == null || avatar == null || premiumType == null)
            && System.currentTimeMillis() - start < timeout * 1000
        ) Thread.sleep(1)
    }

}