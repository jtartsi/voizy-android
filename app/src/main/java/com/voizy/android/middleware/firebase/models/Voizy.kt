package com.voizy.android.middleware.firebase.models

import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class Voizy(
    val id: String = "",
    val name: String = "",
    val tags: List<String> = emptyList(),
    val filePath: String = "",
    @ServerTimestamp
    val createdAt: Timestamp = Timestamp.now(), // For uploading to Firestore
    val creationTimeInSec: Long = 0 // For retrieving data
) {
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

    fun getHashTags(): String {
        return tags.joinToString(separator = " #", prefix = "#")
    }
}