package com.voizy.android

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.yausername.youtubedl_android.YoutubeDL
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber

class VoizyApp : Application() {

    companion object {
        const val KEY_ACTION = "action"
        const val KEY_DATA = "data"
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        FirebaseAnalytics.getInstance(this)
            .logEvent(FirebaseAnalytics.Event.APP_OPEN, null)

        Timber.d("launching application")

        initAndUpdateYoutubeDl()

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
    }

    private fun initAndUpdateYoutubeDl() {
        val youtubeDL = YoutubeDL.getInstance()
        youtubeDL.init(this)

        Completable.defer {
            Completable.fromCallable {
                Timber.d("YoutubeDL updating...")
                val updateStatus = youtubeDL
                    .updateYoutubeDL(this)
                Timber.d("YoutubeDL update status $updateStatus")
            }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }
}