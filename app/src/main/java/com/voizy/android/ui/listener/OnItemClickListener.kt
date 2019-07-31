package com.voizy.android.ui.listener

interface OnItemClickListener<V, T> {
    fun onClick(viewHolder: V, position: Int, item: T)
}