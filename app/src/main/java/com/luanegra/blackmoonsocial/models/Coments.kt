package com.luanegra.blackmoonsocial.models

class Coments {
    private var uID: String = ""
    private var userComentID: String = ""
    private var coment: String = ""
    private var date: String = ""
    private var postID: String = ""

    constructor()

    constructor(uID: String, userComentID: String, coment: String, date: String, postID: String) {
        this.uID = uID
        this.userComentID = userComentID
        this.coment = coment
        this.date = date
        this.postID = postID
    }

    fun getuID(): String {
        return uID
    }

    fun setuID(uID: String){
        this.uID = uID
    }

    fun getuserComentID(): String {
        return userComentID
    }

    fun setuserComentID(userComentID: String){
        this.userComentID = userComentID
    }

    fun getcoment(): String {
        return coment
    }

    fun setcoment(coment: String){
        this.coment = coment
    }

    fun getdate(): String {
        return date
    }

    fun setdate(date: String){
        this.date = date
    }

    fun getpostID(): String {
        return postID
    }

    fun setpostID(postID: String){
        this.postID = postID
    }

}