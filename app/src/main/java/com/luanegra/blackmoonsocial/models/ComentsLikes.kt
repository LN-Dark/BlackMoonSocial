package com.luanegra.blackmoonsocial.models

class ComentsLikes {
    private var uid: String = ""
    private var userID: String = ""
    private var postID: String = ""
    private var ComentID: String = ""

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

    fun getpostID(): String {
        return postID
    }

    fun setpostID(postID: String){
        this.postID = postID
    }

    fun getComentID(): String {
        return ComentID
    }

    fun setComentID(ComentID: String){
        this.ComentID = ComentID
    }
}