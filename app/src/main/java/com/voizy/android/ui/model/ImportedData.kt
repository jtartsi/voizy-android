package com.voizy.android.ui.model

import android.net.Uri

data class ImportedData(val uri: Uri) {

    companion object {
        const val TYPE_VIDEO = "video"
        const val TYPE_AUDIO = "audio"
    }

    val contentType: String
        get() {
            return if (uri.toString().contains(TYPE_VIDEO)) {
                TYPE_VIDEO
            } else {
                TYPE_AUDIO
            }
        }

    val fileExtension: String
        get() = uri.path!!.split(".")!!.last()

    var durationInMillis: Long = -1

    val durationInSecods: Long
        get() = durationInMillis / 1000

    var accessibleFilePath: String = ""
}