package com.example.gamestore

import android.app.Application
import com.example.gamestore.di.AppContainer

class GamestoreApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        container = AppContainer(this)
    }
}