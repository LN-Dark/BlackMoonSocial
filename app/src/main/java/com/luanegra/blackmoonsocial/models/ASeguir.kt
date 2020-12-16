package com.luanegra.blackmoonsocial.models

class ASeguir {
    private var uid: String = ""
    private var followingID: String = ""

    constructor()

    constructor(uid: String, userID: String, followingID: String) {
        this.uid = uid
        this.followingID = followingID
    }

    fun getuid(): String {
        return uid
    }

    fun setuid(uid: String){
        this.uid = uid
    }


    fun getfollowingID(): String {
        return followingID
    }

    fun setfollowingID(followingID: String){
        this.followingID = followingID
    }

}