package com.voizy.android.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.listener.VoizyActionListener
import com.voizy.android.utils.NetworkState

class VoizyListAdapter(
    private val listener: VoizyActionListener
) :
    PagedListAdapter<Voizy, RecyclerView.ViewHolder>(
        Voizy.DIFF_CALLBACK
    ) {

    companion object {
        private const val TYPE_PROGRESS = 0
        private const val TYPE_ITEM = 1
    }

    var networkState: NetworkState? = null
        set(value) {
            val previousState = field
            val previousExtraRow = hasExtraRow()
            field = value
            val newExtraRow = hasExtraRow()
            if (previousExtraRow != newExtraRow) {
                if (previousExtraRow) {
                    notifyItemRemoved(itemCount)
                } else {
                    notifyItemInserted(itemCount)
                }
            } else if (newExtraRow && previousState != networkState) {
                notifyItemChanged(itemCount - 1)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_ITEM) {
            return VoizyViewHolder.create(parent)
        } else {
            return LoadingViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VoizyViewHolder -> {

                holder.bindTo(getItem(position)!!)
                holder.itemView.setOnClickListener {
                    listener.playVoizy(holder, position, getItem(position)!!)
                }
                holder.btnShare.setOnClickListener {
                    listener.shareVoizy(getItem(position)!!)
                }
                holder.itemView.setOnLongClickListener {
                    listener.onVoizyLongPress(getItem(position)!!)
                    true
                }
            }
            is LoadingViewHolder -> holder.bindTo(networkState!!)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (hasExtraRow() && position == itemCount - 1) {
            TYPE_PROGRESS
        } else {
            TYPE_ITEM
        }
    }

    private fun hasExtraRow(): Boolean {
        return networkState != null && networkState !== NetworkState.LOADED && itemCount >= 10
    }
}