package com.voizy.android.middleware.firebase.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

class Voizy() {

    constructor(name: String, tags: List<String>) : this() {
        this.name = name
        this.tags = tags
    }

    constructor(name: String, tags: List<String>, filePath: String) : this() {
        this.name = name
        this.tags = tags
        this.filePath = filePath
    }

    var name: String = ""
    var tags: List<String> = emptyList()
    var filePath: String = ""
    @ServerTimestamp
    var createdAt: Timestamp? = Timestamp.now()

    companion object {
        private const val NAME_KEY = "name"
        private const val TAGS_KEY = "tags"
        private const val FILE_PATH = "filePath"
        private const val CREATED_AT = "createdAt"
    }
}