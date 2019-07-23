package com.voizy.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.voizy.android.R
import kotlinx.android.synthetic.main.record_button_fragment.*
import timber.log.Timber

class RecordButtonFragment : Fragment() {

    companion object {
        public val TAG = RecordButtonFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.record_button_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_record.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_BUTTON_PRESS -> {
                    Timber.d("Press down")
                    view.animate()
                        .scaleX(2f)
                        .scaleY(2f).duration = 200
                    true
                }
                MotionEvent.ACTION_BUTTON_RELEASE -> {
                    Timber.d("Press up")
                    view.animate()
                        .scaleX(0.5f)
                        .scaleY(0.5f).duration = 200
                    true
                }
                else -> {
                    false
                }
            }
        }
        Timber.d("onViewCreated()")
        button_record.setOnLongClickListener {
            it.animate()
                .scaleX(2f)
                .scaleY(2f).duration = 200
            Timber.d("Long press")
            true
        }
    }
}