package com.voizy.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.voizy.android.R
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

        Timber.d("long-click")
        val recordButton = view.findViewById<ImageButton>(R.id.button_record)

        // recordButton.setOnGenericMotionListener { view, event ->
        //     when (event.action) {
        //         MotionEvent.ACTION_BUTTON_PRESS -> {
        //             Timber.d("long-click Press down")
        //             view.animate()
        //                 .scaleX(2f)
        //                 .scaleY(2f).duration = 200
        //             true
        //         }
        //         MotionEvent.ACTION_BUTTON_RELEASE -> {
        //             Timber.d("long-click Press up")
        //             view.animate()
        //                 .scaleX(0.5f)
        //                 .scaleY(0.5f).duration = 200
        //             true
        //         }
        //         else -> {
        //             Timber.d(TAG, "long-click else")
        //             true
        //         }
        //     }
        // }

        // Timber.d("onViewCreated()")
        // button_record.setOnLongClickListener {
        //     it.animate()
        //         .scaleX(2f)
        //         .scaleY(2f).duration = 200
        //     Timber.d("Long press")
        //     true
        // }
    }
}