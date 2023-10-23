package com.example.iuda

import android.content.Context

class Preferences(val context:Context) {
    val SHARED_INFO = "Mydtb"
    val SHARED_EMAIL = "email"
    val SHARED_ID = "id"
    val SHARED_USER_NAME = "fullName"
    val storage = context.getSharedPreferences(SHARED_INFO,0)

    //Guardar los valores de las variables
    fun saveEmail(email:String){
        storage.edit().putString(SHARED_EMAIL, email).apply()
    }

    fun saveId(id:String){
        storage.edit().putString(SHARED_ID, id).apply()
    }

    fun saveName(fullName:String){
        storage.edit().putString(SHARED_USER_NAME, fullName).apply()
    }

    //Obtener los valores de las variables
    fun getEmail():String{
        return storage.getString(SHARED_EMAIL,"")!!
    }

    fun getId():String{
        return storage.getString(SHARED_ID,"")!!
    }

    fun getName():String{
        return storage.getString(SHARED_USER_NAME,"")!!
    }

    fun wipe(){
        storage.edit().clear().apply()
    }
}