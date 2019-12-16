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
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.middleware.repositories.ShareRepository
import timber.log.Timber
import java.io.File

class ShareManager(private val shareRepository: ShareRepository) {

    companion object {
        private val TAG = ShareManager::class.java.simpleName
        private const val AUTHORITY = "com.voizy.android.fileprovider"
    }

    // TODO voizy-details, change file to path?
    fun startVoizyShare(context: Context, voizy: Voizy, file: File) {
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

        shareRepository.startVoizyShare(voizy)

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