package com.voizy.android.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.widget.PlaybackButton

class VoizyViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val btnPlayback: PlaybackButton = view.findViewById(R.id.btn_voizy_row_playback)
    val tvTitle: TextView = view.findViewById(R.id.tv_voizy_row_title)
    val tvTags: TextView = view.findViewById(R.id.tv_voizy_row_tags)
    val loadingProgressBar: ProgressBar = view.findViewById(R.id.pb_voizy_loading_progress)
    val playProgressBar: ProgressBar = view.findViewById(R.id.pb_voizy_play_progress)
    val btnShare: ImageButton = view.findViewById(R.id.btn_share)

    private var progressAnimator: ObjectAnimator? = null

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
        playProgressBar.progress = 0
        playProgressBar.max = 0
    }

    fun showLoadingProgress(visible: Boolean) {
        loadingProgressBar.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun animatePlayProgress(durationInMillis: Int) {
        playProgressBar.max = durationInMillis
        progressAnimator = ObjectAnimator.ofInt(
            playProgressBar,
            "progress",
            0,
            durationInMillis
        )
        progressAnimator?.duration = durationInMillis.toLong()
        progressAnimator?.interpolator = LinearInterpolator()
        progressAnimator?.start()
    }

    fun clearPlayProgress() {
        progressAnimator?.cancel()
        playProgressBar.progress = 0
    }
}