package com.voizy.android

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.google.firebase.analytics.FirebaseAnalytics
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.utils.ShareController
import org.koin.core.KoinComponent
import org.koin.core.inject
import timber.log.Timber

class ShareBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    val shareController: ShareController by inject()

    override fun onReceive(context: Context?, intent: Intent) {
        Timber.d(
            "ShareBroadcastReceiver.onReceive() voizy name " +
                "${shareController.lastSharedVoizy}"
        )

        val clickedComponent: ComponentName? =
            intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT)

        if (clickedComponent != null) {
            val packageName = clickedComponent.packageName
            Timber.d("ShareBroadcastReceiver.onReceive() packageName $packageName")
            VoizyFirebaseAnalytics(FirebaseAnalytics.getInstance(context!!))
                .logChooseShareApplication(packageName)
        }
    }
}