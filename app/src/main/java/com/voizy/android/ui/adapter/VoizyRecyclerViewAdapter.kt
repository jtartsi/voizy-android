package com.voizy.android.ui.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import com.voizy.android.middleware.model.Voizy
import com.voizy.android.ui.listener.OnItemClickListener
import timber.log.Timber
import java.util.Date
import kotlin.random.Random

class VoizyRecyclerViewAdapter(
    private val onItemClickListener: OnItemClickListener<VoizyViewHolder, Voizy>
) : RecyclerView.Adapter<VoizyRecyclerViewAdapter.VoizyViewHolder>() {

    private val dataset = mutableListOf<Voizy>()
    private var cancellableDeletedItem: Pair<Int, Voizy>? = null

    companion object {
        private val sports = listOf<String>("#football ", "#icehockey ", "#soccer ", "#rugby ")
        private val adjectives = listOf<String>("#best ", "#awesome ", "#superb ")
        private val actions = listOf<String>("#goal ", "#win ", "#champions ")

        private fun getRandomTags(): String {
            return sports.shuffled().take(1)[0]
                .plus(adjectives.shuffled().take(1)[0])
                .plus(actions.shuffled().take(1)[0])
        }
    }

    val items: List<Voizy>
        get() = dataset

    class VoizyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var tvTitle: TextView = view.findViewById(R.id.tv_voizy_row_title)
        var tvTags: TextView = view.findViewById(R.id.tv_voizy_row_tags)
        var tvShareCount: TextView = view.findViewById(R.id.tv_voizy_row_shares)
        var progressBar: ProgressBar = view.findViewById(R.id.pb_voizy_row_progress)

        fun animateProgress(durationInMillis: Int) {
            Timber.d("animateProgress duration $durationInMillis")
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
        holder.tvTitle.text = dataset[position].name
        val randomShareCount = Random(Date().time).nextInt(1700)
        holder.tvShareCount.text = "$randomShareCount"
        holder.tvTags.text = getRandomTags()
        holder.itemView.setOnClickListener {
            onItemClickListener.onClick(holder, position, items[position])
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

    /**
     * Remove, but do not delete from dataset
     */
    fun cancellableDelete(position: Int) {
        cancellableDeletedItem = Pair(position, items[position])
        dataset.removeAt(position)
        notifyItemRemoved(position)
    }

    fun cancelDelete() {
        cancellableDeletedItem?.let {
            dataset.add(it.first, it.second)
            notifyItemInserted(it.first)
        }
    }
}