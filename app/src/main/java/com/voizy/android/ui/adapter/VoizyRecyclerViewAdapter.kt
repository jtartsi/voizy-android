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
import com.voizy.android.ui.listener.VoizyActionListener
import com.voizy.android.ui.models.Voizy

class VoizyRecyclerViewAdapter(
    private val actionListener: VoizyActionListener
) : RecyclerView.Adapter<VoizyRecyclerViewAdapter.VoizyViewHolder>() {

    private val dataset = mutableListOf<Voizy>()

    val items: List<Voizy>
        get() = dataset

    class VoizyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tv_voizy_row_title)
        var tvTags: TextView = view.findViewById(R.id.tv_voizy_row_tags)
        var progressBar: ProgressBar = view.findViewById(R.id.pb_voizy_row_progress)
        var btnShare: ImageButton = view.findViewById(R.id.btn_share)

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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VoizyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.voizy_row_view, parent, false)
        return VoizyViewHolder(view)
    }

    override fun onBindViewHolder(holder: VoizyViewHolder, position: Int) {
        val voizy = dataset[position]
        holder.tvTitle.text = voizy.name
        holder.tvTags.text = getHashTags(voizy.tags)
        holder.progressBar.progress = 0
        holder.progressBar.max = 0
        holder.itemView.setOnClickListener {
            actionListener.playVoizy(holder, position, items[position])
        }
        holder.btnShare.setOnClickListener {
            actionListener.shareVoizy(items[position])
        }
    }

    override fun getItemCount(): Int = dataset.size

    fun addAll(voizys: List<Voizy>) {
        dataset.addAll(voizys)
        notifyDataSetChanged()
    }

    fun clear() {
        dataset.clear()
        notifyDataSetChanged()
    }

    private fun getHashTags(tags: List<String>): String {
        return tags.joinToString(separator = " #", prefix = "#")
    }
}