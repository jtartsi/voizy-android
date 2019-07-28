package com.voizy.android.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.model.Voizy
import timber.log.Timber

class VoizyListAdapter : RecyclerView.Adapter<VoizyListAdapter.VoizyViewHolder>() {

    private val dataset = mutableListOf<Voizy>()

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class VoizyViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VoizyViewHolder {
        // create a new view
        // TODO provide custom row layout
        val textView = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
        // set the view's size, margins, paddings and layout parameters
        return VoizyViewHolder(textView)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: VoizyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Timber.d("vzy-list ${dataset[position].name}")
        holder.textView.text = dataset[position].name
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int = dataset.size

    public fun addAll(voizys: List<Voizy>) {
        dataset.addAll(voizys)
        notifyDataSetChanged()
    }
}