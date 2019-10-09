package com.voizy.android.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.R
import com.voizy.android.utils.NetworkState

class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    companion object {
        fun create(parent: ViewGroup): LoadingViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_item_loading, parent, false)
            return LoadingViewHolder(view)
        }
    }

    val progressBar: ProgressBar = itemView.findViewById(R.id.progress_bar)

    fun bindTo(networkState: NetworkState) {
        progressBar.visibility =
            if (networkState == NetworkState.LOADING) View.VISIBLE else View.GONE
    }
}