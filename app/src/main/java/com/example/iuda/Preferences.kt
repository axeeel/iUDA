package com.example.iuda

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Preferences(val context:Context) {
    val SHARED_INFO = "Mydtb"
    val SHARED_EMAIL = "email"
    val SHARED_ID = "id"
    val SHARED_USER_NAME = "fullName"
    val SHARED_USERNAME = "username"
    val SHARED_AMIGOS_LIST = "amigosList"
    val SHARED_NAME_CONTACT = "name"
    val SHARED_PHONE_CONTACT = "phone"
    private val SHARED_SWITCH_ALL_NOTIFICATIONS = "all_notifications"
    private val SHARED_SWITCH_FRIEND_REQUEST = "friend_request"
    private val SHARED_SWITCH_ALERT_NOTIFICATION = "alert_notification"
    private val SHARED_SWITCH_ALARM_NOTIFICATION = "alarm_notification"
    val storage = context.getSharedPreferences(SHARED_INFO,0)
    private val gson = Gson()

    //Guardar los valores de las variables
    fun saveAmigosList(amigosList: List<AmigosDataClass>) {
        val json = gson.toJson(amigosList)
        storage.edit().putString(SHARED_AMIGOS_LIST, json).apply()
    }

    // Método para guardar el estado de un switch específico
    fun saveSwitchState(switchId: String, isChecked: Boolean) {
        storage.edit().putBoolean(switchId, isChecked).apply()
    }

    // Método para obtener el estado de un switch específico
    fun getSwitchState(switchId: String): Boolean {
        return storage.getBoolean(switchId, false)
    }

    // Métodos convenientes para cada switch específico
    fun saveAllNotificationsSwitchState(isChecked: Boolean) {
        saveSwitchState(SHARED_SWITCH_ALL_NOTIFICATIONS, isChecked)
    }

    fun getAllNotificationsSwitchState(): Boolean {
        return getSwitchState(SHARED_SWITCH_ALL_NOTIFICATIONS)
    }

    fun saveFriendRequestSwitchState(isChecked: Boolean) {
        saveSwitchState(SHARED_SWITCH_FRIEND_REQUEST, isChecked)
    }

    fun getFriendRequestSwitchState(): Boolean {
        return getSwitchState(SHARED_SWITCH_FRIEND_REQUEST)
    }

    fun saveAlertNotificationSwitchState(isChecked: Boolean) {
        saveSwitchState(SHARED_SWITCH_ALERT_NOTIFICATION, isChecked)
    }

    fun getAlertNotificationSwitchState(): Boolean {
        return getSwitchState(SHARED_SWITCH_ALERT_NOTIFICATION)
    }

    fun saveAlarmNotificationSwitchState(isChecked: Boolean) {
        saveSwitchState(SHARED_SWITCH_ALARM_NOTIFICATION, isChecked)
    }

    fun getAlarmNotificationSwitchState(): Boolean {
        return getSwitchState(SHARED_SWITCH_ALARM_NOTIFICATION)
    }

    fun saveNameContact(name: String){
        storage.edit().putString(SHARED_NAME_CONTACT, name).apply()
    }

    fun saveNumberContact(phone: String){
        storage.edit().putString(SHARED_PHONE_CONTACT, phone).apply()
    }


    fun saveEmail(email:String){
        storage.edit().putString(SHARED_EMAIL, email).apply()
    }
    fun saveId(id:String){
        storage.edit().putString(SHARED_ID, id).apply()
    }

    fun saveName(fullName:String){
        storage.edit().putString(SHARED_USER_NAME, fullName).apply()
    }

    fun saveUserName (username:String){
        storage.edit().putString(SHARED_USERNAME, username).apply()
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

    fun getUsername():String{
        return storage.getString(SHARED_USERNAME,"")!!
    }

    fun getNameContact():String{
        return storage.getString(SHARED_NAME_CONTACT,"")!!
    }

    fun getPhoneContact():String{
        return storage.getString(SHARED_PHONE_CONTACT,"")!!
    }
    fun getAmigosList(): MutableList<AmigosDataClass>? {
        val json = storage.getString(SHARED_AMIGOS_LIST, null)
        if (json != null) {
            val type = object : TypeToken<List<AmigosDataClass>>() {}.type
            return gson.fromJson(json, type)
        }
        return null
    }


    fun wipe(){
        storage.edit().clear().apply()
    }
}