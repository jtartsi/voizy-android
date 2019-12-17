package com.voizy.android.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import com.voizy.android.middleware.firebase.models.Voizy

class VoizyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val tvTitle: TextView = view.findViewById(R.id.tv_voizy_row_title)
    val tvTags: TextView = view.findViewById(R.id.tv_voizy_row_tags)
    val progressBar: ProgressBar = view.findViewById(R.id.pb_voizy_row_progress)
    val btnShare: ImageButton = view.findViewById(R.id.btn_share)

    companion object {
        fun create(parent: ViewGroup): VoizyViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_item_voizy, parent, false)
            return VoizyViewHolder(view)
        }
    }

    fun bindTo(voizy: Voizy) {
        tvTitle.text = voizy.name
        tvTags.text = voizy.hashTags
        progressBar.progress = 0
        progressBar.max = 0
    }

    fun animateProgress(durationInMillis: Int) {
        progressBar.max = durationInMillis
        val progressAnimator = ObjectAnimator.ofInt(
            progressBar,
            "progress",
            0,
            durationInMillis
        )
        progressAnimator.duration = durationInMillis.toLong()
        progressAnimator.interpolator = LinearInterpolator()
        progressAnimator.start()
        progressAnimator.addListener(
            onEnd = {
                progressBar.progress = 0
                progressBar.max = 0
            }
        )
    }
}