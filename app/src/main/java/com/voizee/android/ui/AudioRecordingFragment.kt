package com.voizee.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.voizee.android.R
import kotlinx.android.synthetic.main.audio_recording_fragment.*

class AudioRecordingFragment: Fragment() {

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 100
        public val TAG = "AudioRecordingFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(
            activity!!,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
            grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            fragmentManager!!.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.audio_recording_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        record_button.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_BUTTON_PRESS -> {
                    // Start recording
                    true
                }
                MotionEvent.ACTION_BUTTON_RELEASE -> {
                    // Stop recording
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    private fun startRecording() {

    }

    private fun stopRecording() {

    }
}