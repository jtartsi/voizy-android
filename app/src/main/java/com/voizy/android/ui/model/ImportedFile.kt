package com.voizy.android.ui.model

import android.content.ClipData
import android.net.Uri

data class ImportedFile(private val clipData: ClipData) {

    companion object {
        const val TYPE_VIDEO = "video"
        const val TYPE_AUDIO = "audio"
    }

    val uri: Uri
        get() = clipData.getItemAt(0).uri

    val mimeType: String
        get() = clipData.description.getMimeType(0)

    val contentType: String
        get() = clipData.description.getMimeType(0).split("/").first()

    val fileExtension: String
        get() = clipData.description.getMimeType(0).split("/").last()

    var lengthInMillis: Long = -1

    var filePath: String = ""
}