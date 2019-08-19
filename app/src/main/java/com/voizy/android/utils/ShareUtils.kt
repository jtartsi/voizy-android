package com.voizy.android.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.voizy.android.R
import timber.log.Timber
import java.io.File

class ShareUtils {

    companion object {

        private const val AUTHORITY = "com.voizy.android.fileprovider"

        fun shareVoizy(context: Context, file: File) {
            val fileUri: Uri? = getFileUri(context, file)
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                this.putExtra(Intent.EXTRA_STREAM, fileUri)
                type = "audio/*"
            }
            context.startActivity(Intent.createChooser(sendIntent, context.getString(R.string.share_voizy)))
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