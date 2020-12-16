package com.luanegra.blackmoonsocial.models

class Likes {
    private var uid: String = ""
    private var userID: String = ""

    constructor()

    constructor(uid: String, userID: String) {
        this.uid = uid
        this.userID = userID
    }

    fun getUid(): String {
        return uid
    }

    fun setUid(uid: String){
        this.uid = uid
    }

    fun getuserID(): String {
        return userID
    }

    fun setuserID(userID: String){
        this.userID = userID
    }
}