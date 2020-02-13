package com.voizy.android.ui.adapter

import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.widget.PlaybackButton
import com.voizy.android.utils.NetworkState

class VoizyListAdapter : PagedListAdapter<Voizy, RecyclerView.ViewHolder>(
    Voizy.DIFF_CALLBACK
) {

    companion object {
        private const val TYPE_PROGRESS = 0
        private const val TYPE_ITEM = 1
    }

    lateinit var onPlayEvent: (
        viewHolder: VoizyViewHolder,
        position: Int,
        voizy: Voizy
    ) -> Unit

    lateinit var onShareEvent: (
        viewHolder: VoizyViewHolder,
        position: Int,
        voizy: Voizy
    ) -> Unit

    lateinit var onLongPress: (
        viewHolder: VoizyViewHolder,
        position: Int,
        voizy: Voizy
    ) -> Unit

    private var playingViewHolder: VoizyViewHolder? = null
    private var loadingViewHolder: VoizyViewHolder? = null

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
                holder.btnPlayback.setOnClickListener {
                    onPlayEvent(holder, position, getItem(position)!!)
                }
                holder.btnShare.setOnClickListener {
                    onShareEvent(holder, position, getItem(position)!!)
                }
                holder.itemView.setOnLongClickListener {
                    onLongPress(holder, position, getItem(position)!!)
                    true
                }
                holder.tvTitle.setOnLongClickListener {
                    onLongPress(holder, position, getItem(position)!!)
                    true
                }
                holder.tvTags.setOnLongClickListener {
                    onLongPress(holder, position, getItem(position)!!)
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

    fun showPlayingIndicator(viewHolder: VoizyViewHolder, audioLengthInMillis: Int) {
        viewHolder.animatePlayProgress(audioLengthInMillis)
        playingViewHolder = viewHolder
        playingViewHolder?.let {
            it.btnPlayback.state = PlaybackButton.State.STOP_ICON
        }
    }

    fun showLoadingIndicator(viewHolder: VoizyViewHolder, visible: Boolean) {
        viewHolder.showLoadingProgress(visible)
        loadingViewHolder = viewHolder
    }

    fun clearLoadingState() {
        loadingViewHolder?.let {
            it.showLoadingProgress(false)
        }
    }

    fun clearPlayingState() {
        playingViewHolder?.let {
            it.clearPlayProgress()
            it.btnPlayback.state = PlaybackButton.State.PLAY_ICON
        }
    }

    private fun hasExtraRow(): Boolean {
        return networkState != null && networkState !== NetworkState.LOADED && itemCount >= 10
    }
}