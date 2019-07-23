package com.voizy.android.ui

import android.os.Bundle
import android.os.Handler
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.voizy.android.R

@SuppressWarnings("ClickableViewAccessibility")
class RecordButtonFragment : Fragment() {

    companion object {
        public val TAG = RecordButtonFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.record_button_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recordButton = view.findViewById<ImageButton>(R.id.button_record)

        recordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording(view)
                    false
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording(view)
                    false
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun startRecording(view: View) {
        delayedVibrate(view)

        view.animate()
            .scaleY(2f)
            .scaleX(2f)
            .duration = 200

        Handler().postDelayed({
            fragmentManager!!.beginTransaction()
                .add(R.id.fragment_container, RecordingOverlayFragment())
                .addToBackStack(RecordingOverlayFragment.TAG)
                .commit()
        }, 200)
    }

    private fun stopRecording(view: View) {
        delayedVibrate(view)

        view.animate()
            .scaleY(1f)
            .scaleX(1f)
            .duration = 200

        Handler().postDelayed({
            fragmentManager!!.popBackStack(
                RecordingOverlayFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }, 200)
    }

    private fun delayedVibrate(view: View) {
        Handler().postDelayed(
            { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }, 200
        )
    }
}