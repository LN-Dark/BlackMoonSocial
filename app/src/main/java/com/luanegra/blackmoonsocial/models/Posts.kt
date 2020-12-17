package com.luanegra.blackmoonsocial.models

class Posts {
    private var uid: String = ""
    private var userID: String = ""
    private var photoURL: String = ""
    private var likes: List<Likes>? = null
    private var date: String = ""
    private var coments: List<Coments>? = null
    private var description: String = ""

    constructor()

    constructor(uid: String, userID: String, photoURL: String, likes: List<Likes>?, date: String, coments: List<Coments>?, description: String) {
        this.uid = uid
        this.userID = userID
        this.photoURL = photoURL
        this.likes = likes
        this.date = date
        this.coments = coments
        this.description = description
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

    fun getphotoURL(): String {
        return photoURL
    }

    fun setphotoURL(photoURL: String){
        this.photoURL = photoURL
    }

    fun getlikes(): List<Likes>? {
        return likes
    }

    fun setlikes(likes: List<Likes>?){
        this.likes = likes
    }

    fun getdate(): String {
        return date
    }

    fun setdate(date: String){
        this.date = date
    }

    fun getcoments(): List<Coments>? {
        return coments
    }

    fun setcoments(coments: List<Coments>?){
        this.coments = coments
    }

    fun getdescription(): String {
        return description
    }

    fun setdescription(description: String){
        this.description = description
    }


}