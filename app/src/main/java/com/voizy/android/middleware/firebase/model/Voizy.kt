package com.voizy.android.middleware.firebase.model

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.PropertyName
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

    @get:PropertyName(NAME_KEY)
    @set:PropertyName(NAME_KEY)
    var name: String = ""
    @get:PropertyName(TAGS_KEY)
    @set:PropertyName(TAGS_KEY)
    var tags: List<String> = emptyList()
    @set:PropertyName(FILE_PATH)
    @get:PropertyName(FILE_PATH)
    var filePath: String = ""

    @ServerTimestamp
    @get:PropertyName(CREATED_AT)
    var createdAt: FieldValue? = FieldValue.serverTimestamp()

    companion object {
        private const val NAME_KEY = "name"
        private const val TAGS_KEY = "tags"
        private const val FILE_PATH = "file_path"
        private const val CREATED_AT = "created_at"
    }
}