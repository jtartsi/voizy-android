package com.voizy.android.middleware.firebase.model

class FirebaseVoizy(
    val name: String,
    val tags: HashMap<String, Boolean>
) {

    constructor() : this(name = "", tags = hashMapOf())
}