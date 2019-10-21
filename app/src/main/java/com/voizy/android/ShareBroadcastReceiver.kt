package com.voizy.android

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.voizy.android.middleware.repositories.ShareRepository
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class ShareBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    val shareRepository: ShareRepository by inject()

    override fun onReceive(context: Context?, intent: Intent) {

        val clickedComponent: ComponentName? =
            intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT)

        var packageName: String? = null
        if (clickedComponent != null) {
            packageName = clickedComponent.packageName
        }
        shareRepository.finishShare(packageName)
        Timber.d("ShareBroadcastReceiver.onReceive() packageName $packageName")
    }
}