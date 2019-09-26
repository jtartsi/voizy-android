package com.voizy.android.ui.models

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
}