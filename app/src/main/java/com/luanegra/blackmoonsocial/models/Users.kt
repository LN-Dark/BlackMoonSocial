package com.luanegra.blackmoonsocial.models

class Users {
    private var uid: String = ""
    private var username: String = ""
    private var email: String = ""
    private var profile: String = ""
    private var cover: String = ""
    private var status: String = ""
    private var search: String = ""
    private var publicKey: String = ""
    private var aboutMe: String = ""
    private var notificationsShow: Boolean = true
    private var pessoasASeguir: List<ASeguir>? = null



    constructor(
        uid: String,
        username: String,
        email: String,
        profile: String,
        cover: String,
        status: String,
        search: String,
        publicKey: String,
        aboutMe: String,
        notificationsShow: Boolean,
        pessoasASeguir: List<ASeguir>
    ) {
        this.uid = uid
        this.username = username
        this.email = email
        this.profile = profile
        this.cover = cover
        this.status = status
        this.search = search
        this.publicKey = publicKey
        this.aboutMe = aboutMe
        this.notificationsShow = notificationsShow
        this.pessoasASeguir = pessoasASeguir
    }

    constructor()

    fun getUid(): String {
        return uid
    }

    fun setUid(uid: String){
        this.uid = uid
    }

    fun getusername(): String {
        return username
    }

    fun setusername(username: String){
        this.username = username
    }
    fun getemail(): String {
        return email
    }

    fun setemail(email: String){
        this.email = email
    }
    fun getprofile(): String {
        return profile
    }

    fun setprofile(profile: String){
        this.profile = profile
    }
    fun getcover(): String {
        return cover
    }

    fun setcover(cover: String){
        this.cover = cover
    }
    fun getstatus(): String {
        return status
    }

    fun setstatus(status: String){
        this.status = status
    }
    fun getsearch(): String {
        return search
    }

    fun setsearch(search: String){
        this.search = search
    }

    fun getpublicKey(): String {
        return publicKey
    }

    fun setpublicKey(publicKey: String){
        this.publicKey = publicKey
    }

    fun getaboutMe(): String {
        return aboutMe
    }

    fun setaboutMe(aboutMe: String){
        this.aboutMe = aboutMe
    }

    fun getnotificationsShow(): Boolean {
        return notificationsShow
    }

    fun setnotificationsShow(notificationsShow: Boolean){
        this.notificationsShow = notificationsShow
    }

    fun getpessoasASeguir(): List<ASeguir>? {
        return pessoasASeguir
    }

    fun setpessoasASeguir(pessoasASeguir: List<ASeguir>){
        this.pessoasASeguir = pessoasASeguir
    }

}