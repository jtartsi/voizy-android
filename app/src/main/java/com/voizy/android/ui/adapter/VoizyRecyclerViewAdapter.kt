package com.voizy.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import com.voizy.android.model.Voizy
import java.util.Date
import kotlin.random.Random

class VoizyRecyclerViewAdapter : RecyclerView.Adapter<VoizyRecyclerViewAdapter.VoizyViewHolder>() {

    private val dataset = mutableListOf<Voizy>()

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

    class VoizyViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var tvTitle: TextView = root.findViewById(R.id.tv_voizy_row_title)
        var tvTags: TextView = root.findViewById(R.id.tv_voizy_row_tags)
        var tvShareCount: TextView = root.findViewById(R.id.tv_voizy_row_shares)
    }

    // Create new views (invoked by the layout manager)
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
    }

    override fun getItemCount(): Int = dataset.size

    public fun addAll(voizys: List<Voizy>) {
        dataset.addAll(voizys)
        notifyDataSetChanged()
    }
}