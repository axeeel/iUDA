package com.example.iuda

import android.app.Application

class iuda:Application() {
    companion object{
        lateinit var prefs:Preferences
    }
    override fun onCreate() {
        super.onCreate()
        prefs = Preferences(applicationContext)
    }
}