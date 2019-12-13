package com.voizy.android.ui.widget.createoptions

import android.content.Context
import android.content.res.ColorStateList
import android.os.Handler
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.voizy.android.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RecordButton : FloatingActionButton {

    companion object {
        private const val ANIMATION_DELAY = 200L
    }

    private val events = PublishSubject.create<CreateEvent>()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttributeSet: Int) : super(
        context,
        attributeSet,
        defStyleAttributeSet
    ) {
        setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    handleStartRecording()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    handleStopRecording()
                    true
                }
            }
            false
        }
    }

    fun getButtonEvents(): Observable<CreateEvent> {
        return events
    }

    fun handleStartRecording() {
        setImageResource(R.drawable.round_mic_black_48)
        val colorStateList =
            ColorStateList.valueOf(context.resources.getColor(R.color.voizy_orange))
        backgroundTintList = colorStateList

        delayedVibrate()
        animateButtonOnStart()
        events.onNext(CreateEvent.START_REC_MIC)
    }

    fun handleStopRecording() {
        setImageResource(R.drawable.round_mic_black_48)
        val colorStateList =
            ColorStateList.valueOf(context.resources.getColor(android.R.color.white))
        backgroundTintList = colorStateList

        delayedVibrate()
        animateButtonOnStop()
        events.onNext(CreateEvent.STOP_REC_MIC)
    }

    private fun animateButtonOnStart() {
        animate()
            .scaleY(1.25f)
            .scaleX(1.25f)
            .duration =
            ANIMATION_DELAY
    }

    private fun animateButtonOnStop() {
        animate()
            .scaleY(1f)
            .scaleX(1f)
            .duration =
            ANIMATION_DELAY
    }

    private fun delayedVibrate() {
        Handler().postDelayed(
            { performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) },
            ANIMATION_DELAY
        )
    }
}
