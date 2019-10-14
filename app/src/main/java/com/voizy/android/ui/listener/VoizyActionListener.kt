package com.voizy.android.ui.listener

import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.adapter.VoizyViewHolder

interface VoizyActionListener {

    fun playVoizy(viewHolder: VoizyViewHolder, position: Int, voizy: Voizy)

    fun shareVoizy(voizy: Voizy)
}