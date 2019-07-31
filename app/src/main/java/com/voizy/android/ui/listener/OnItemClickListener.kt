package com.voizy.android.ui.listener

interface OnItemClickListener<T> {
    fun onClick(position: Int, item: T)
}