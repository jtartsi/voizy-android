package com.voizy.android.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.firebase.analytics.FirebaseAnalytics
import com.voizy.android.R
import com.voizy.android.ShareBroadcastReceiver
import com.voizy.android.middleware.firebase.VoizyFirebaseAnalytics
import com.voizy.android.ui.models.Voizy
import timber.log.Timber
import java.io.File

class ShareUtils {

    companion object {

        private const val AUTHORITY = "com.voizy.android.fileprovider"

        fun shareVoizy(voizy: Voizy, context: Context, file: File) {
            val fileUri: Uri? = getFileUri(context, file)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "audio/*"
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                Intent(context, ShareBroadcastReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            VoizyFirebaseAnalytics(FirebaseAnalytics.getInstance(context))
                .logShareVoizyEvent(voizy.id, voizy.name)

            context.startActivity(
                Intent.createChooser(
                    sendIntent,
                    context.getString(R.string.share_voizy),
                    pendingIntent.intentSender
                )
            )
        }

        private fun getFileUri(context: Context, file: File): Uri? {
            return try {
                FileProvider.getUriForFile(context, AUTHORITY, file)
            } catch (e: IllegalArgumentException) {
                Timber.e(
                    e, "File Selector. The selected file can't be shared: ${file.absolutePath}"
                )
                null
            }
        }
    }
}