package com.example.iuda

class DataClass {
    var id: String? = null
    var username: String? = null
    var fullName: String? = null
    var gender: String? = null
    var imgUrl:String? = null  // Ruta o URL de la imagen de perfil

    constructor(id:String?, username:String?,fullName:String?,gender:String?, imgUrl:String?){

        this.id = id
        this.username = username
        this.fullName = fullName
        this.gender = gender
        this.imgUrl = imgUrl
    }

    constructor(){}
}