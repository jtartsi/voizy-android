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
                    delayedVibrate(view)

                    fragmentManager!!.beginTransaction()
                        .add(R.id.fragment_container, RecordingOverlayFragment())
                        .addToBackStack(RecordingOverlayFragment.TAG)
                        .commit()

                    false
                }
                MotionEvent.ACTION_UP -> {
                    delayedVibrate(view)
                    // TODO recording change to remove this and add the save fragment
                    fragmentManager!!.popBackStack(
                        RecordingOverlayFragment.TAG,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    false
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun delayedVibrate(view: View) {
        Handler().postDelayed(
            { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }, 200
        )
    }
}