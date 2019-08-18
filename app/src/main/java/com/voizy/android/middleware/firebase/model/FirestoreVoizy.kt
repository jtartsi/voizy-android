package com.voizy.android.middleware.firebase.model

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.PropertyName
import com.voizy.android.ui.model.Voizy

class FirestoreVoizy(
    @get:PropertyName(NAME_KEY) val name: String,
    @get:PropertyName(TAGS_KEY) val tags: Map<String, Boolean>,
    @get:PropertyName(FILE_PATH) val firebaseFilePath: String
) {

    companion object {
        private const val NAME_KEY = "name"
        private const val TAGS_KEY = "tags"
        private const val FILE_PATH = "file_path"

        fun from(documentSnapshot: DocumentSnapshot): FirestoreVoizy {
            var name = ""
            if (documentSnapshot.get(NAME_KEY) is String) {
                name = documentSnapshot.get(NAME_KEY) as String
            }

            var tags: Map<String, Boolean> = HashMap()
            if (documentSnapshot.get(TAGS_KEY) is HashMap<*, *>) {
                val inputMap = documentSnapshot.get(TAGS_KEY) as Map<*, *>
                inputMap.keys.filter { it is String }
                inputMap.entries.forEach { it.key to true }
                tags = inputMap as Map<String, Boolean>
            }

            var firebaseFilePath = ""
            if (documentSnapshot.get(FILE_PATH) is String) {
                firebaseFilePath = documentSnapshot.get(FILE_PATH) as String
            }
            return FirestoreVoizy(name!!, tags!!, firebaseFilePath!!)
        }
    }

    fun toVoizy(): Voizy {
        val voizy = Voizy(name, tags.keys.toList())
        voizy.firebaseFilePath = this.firebaseFilePath
        return voizy
    }
}