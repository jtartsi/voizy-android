package com.voizy.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import com.voizy.android.model.Voizy

class VoizyListAdapter : RecyclerView.Adapter<VoizyListAdapter.VoizyViewHolder>() {

    private val dataset = mutableListOf<Voizy>()

    class VoizyViewHolder(root: View) : RecyclerView.ViewHolder(root) {
        var tvTitle: TextView = root.findViewById(R.id.tv_voizy_row_title)
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
    }

    override fun getItemCount(): Int = dataset.size

    public fun addAll(voizys: List<Voizy>) {
        dataset.addAll(voizys)
        notifyDataSetChanged()
    }
}