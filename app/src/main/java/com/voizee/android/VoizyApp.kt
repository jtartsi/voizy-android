package com.voizee.android

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class VoizyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@VoizyApp)
            modules(allModules)
        }
    }
}