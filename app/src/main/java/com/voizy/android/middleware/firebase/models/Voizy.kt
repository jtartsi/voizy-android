package com.voizy.android.middleware.firebase.models

import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Voizy(
    val id: String = "",
    val name: String = "",
    val tags: List<String> = emptyList(),
    @get:Exclude
    val localPath: String = "",
    @get:PropertyName("filePath")
    @set:PropertyName("filePath")
    var remoteUrl: String = "",
    var duration: Long = -1,
    @ServerTimestamp val createdAt: Timestamp = Timestamp.now(), // For uploading to Firestore
    val locale: String = "",
    val localeLang: String = "",
    val localeCountry: String = "",
    val playbackUrl: String = ""
) {

    @get:Exclude
    var hashTags: String = ""
        get() {
            return tags.joinToString(separator = " #", prefix = "#")
        }

    companion object {
        var DIFF_CALLBACK: DiffUtil.ItemCallback<Voizy> = object : DiffUtil.ItemCallback<Voizy>() {
            override fun areItemsTheSame(oldItem: Voizy, newItem: Voizy): Boolean {
                return oldItem.id === newItem.id
            }

            override fun areContentsTheSame(oldItem: Voizy, newItem: Voizy): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this)
            return true

        val article = obj as Voizy?
        return article!!.id === this.id
    }
}