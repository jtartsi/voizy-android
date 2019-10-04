package com.voizy.android

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class VoizyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseAnalytics.getInstance(this)
            .logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        startKoin {
            androidContext(this@VoizyApp)
            modules(allModules)
        }

        // if (BuildConfig.DEBUG) {
        //     StrictMode.setThreadPolicy(
        //         StrictMode.ThreadPolicy.Builder()
        //             .detectAll()
        //             .penaltyLog()
        //             .build()
        //     )
        //     StrictMode.setVmPolicy(
        //         StrictMode.VmPolicy.Builder()
        //             .detectAll()
        //             .penaltyLog()
        //             .build()
        //     )
        // }
        Timber.plant(Timber.DebugTree())
    }
}