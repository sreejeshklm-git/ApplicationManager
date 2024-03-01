package io.born.applicationmanager.firestore

data class User(
    var email: String?,
    var user_name: String?,
    var password: String?,
    var user_type: String?,
    var android_id: String?
) {
    constructor() : this("", "", "", "", "")

    fun assignUserType(): String {
        var type: String? = ""
        if (user_type.equals("Admin"))
            type = "1"
        else if (user_type.equals("User"))
            type = "2"
        return type!!
    }
}
