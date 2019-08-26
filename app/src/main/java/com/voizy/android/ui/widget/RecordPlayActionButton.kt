package com.voizy.android.ui.widget

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.voizy.android.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class RecordPlayActionButton : FloatingActionButton {

    companion object {
        private const val ANIMATION_DELAY = 200L
    }

    enum class State { RECORD, PLAY }

    enum class Event { START_RECORD, STOP_RECORD, PLAY }

    private val events = PublishSubject.create<Event>()
    private val stopTimer = Handler()

    var state: State = State.RECORD
        set(value) {
            field = value
            if (value == State.RECORD) {
                setImageResource(R.drawable.sound_waves_orange_dark)
            } else {
                setImageResource(R.drawable.play_orange_dark)
            }
        }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttributeSet: Int) : super(
        context,
        attributeSet,
        defStyleAttributeSet
    ) {
        setOnTouchListener { view, event ->
            if (state == State.RECORD) {
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
            } else {
                false
                if (event.action == MotionEvent.ACTION_DOWN) {
                    events.onNext(Event.PLAY)
                    true
                }
            }
            false
        }
    }

    fun getButtonEvents(): Observable<Event> {
        return events
    }

    private fun handleStartRecording() {
        stopTimer.postDelayed({ handleStopRecording() }, 15500)
        delayedVibrate()
        animateButtonOnStart()
        events.onNext(Event.START_RECORD)
    }

    private fun handleStopRecording() {
        stopTimer.removeCallbacksAndMessages(null)
        delayedVibrate()
        animateButtonOnStop()
        events.onNext(Event.STOP_RECORD)
    }

    private fun animateButtonOnStart() {
        animate()
            .scaleY(1.75f)
            .scaleX(1.75f)
            .duration = ANIMATION_DELAY
    }

    private fun animateButtonOnStop() {
        animate()
            .scaleY(1f)
            .scaleX(1f)
            .duration = ANIMATION_DELAY
    }

    private fun delayedVibrate() {
        Handler().postDelayed(
            { performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) },
            ANIMATION_DELAY
        )
    }
}
