package com.voizy.android.ui.listener

import com.voizy.android.ui.adapter.VoizyViewHolder
import com.voizy.android.ui.models.Voizy

interface VoizyActionListener {

    fun playVoizy(viewHolder: VoizyViewHolder, position: Int, voizy: Voizy)

    fun shareVoizy(voizy: Voizy)
}