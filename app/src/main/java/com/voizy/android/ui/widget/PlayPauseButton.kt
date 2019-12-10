package com.voizy.android.ui.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageButton

class PlayPauseButton : ImageButton {

    private val playDrawable: Drawable
    private val stopDrawable: Drawable

    enum class State { PLAY_ICON, STOP_ICON }

    var state: State = State.PLAY_ICON
        set(value) {
            field = value
            if (value == State.PLAY_ICON) {
                setImageDrawable(playDrawable)
            } else {
                setImageDrawable(stopDrawable)
            }
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(
        context, attributeSet, 0
    )

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttributeSet: Int) : super(
        context,
        attributeSet,
        defStyleAttributeSet
    ) {
        playDrawable = context.getDrawable(android.R.drawable.ic_media_play)!!
        stopDrawable = context.getDrawable(android.R.drawable.ic_media_pause)!!
        state = State.PLAY_ICON
    }
}