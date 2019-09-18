package com.voizy.android.middleware.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp
import com.voizy.android.ui.model.Voizy
import timber.log.Timber

class FirestoreVoizy() {

    constructor(name: String, tags: List<String>, firebaseFilePath: String) : this() {
        this.mName = name
        this.tags = tags
        this.firebaseFilePath = firebaseFilePath
    }

    @get:PropertyName(NAME_KEY)
    @set:PropertyName(NAME_KEY)
    var mName: String = ""
    @get:PropertyName(TAGS_KEY)
    @set:PropertyName(TAGS_KEY)
    var tags: List<String> = emptyList()
    @set:PropertyName(FILE_PATH)
    @get:PropertyName(FILE_PATH)
    var firebaseFilePath: String = ""

    @ServerTimestamp
    @set:PropertyName(CREATED_AT)
    @get:PropertyName(CREATED_AT)
    var createdAt: Timestamp? = null

    companion object {
        private const val NAME_KEY = "name"
        private const val TAGS_KEY = "tags"
        private const val FILE_PATH = "file_path"
        private const val CREATED_AT = "created_at"
    }

    fun toVoizy(): Voizy {
        Timber.d("npe-error toVoizy() mName $mName")
        Timber.d("npe-error toVoizy() tags $tags")
        Timber.d("npe-error toVoizy() file_url $firebaseFilePath")
        Timber.d("npe-error toVoizy() this.mName ${this.mName}")
        Timber.d("npe-error toVoizy() this.file_url ${this.firebaseFilePath}")
        val voizy = Voizy(mName!!, tags!!)
        voizy.firebaseFilePath = firebaseFilePath
        return voizy
    }
}