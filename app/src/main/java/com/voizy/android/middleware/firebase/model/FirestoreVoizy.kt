package com.voizy.android.middleware.firebase.model

import com.google.firebase.firestore.DocumentSnapshot

class FirestoreVoizy(val name: String, val tags: Map<String, Boolean>) {

    companion object {
        private const val NAME_KEY = "name"
        private const val TAGS_KEY = "tags"

        fun from(documentSnapshot: DocumentSnapshot): FirestoreVoizy {
            var name: String = ""
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
            return FirestoreVoizy(name!!, tags!!)
        }
    }
}