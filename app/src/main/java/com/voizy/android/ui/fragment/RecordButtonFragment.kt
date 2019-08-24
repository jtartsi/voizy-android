package com.voizy.android.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordButtonViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject

@SuppressWarnings("ClickableViewAccessibility")
class RecordButtonFragment : Fragment() {

    private val viewModel: RecordButtonViewModel by inject<RecordButtonViewModel>()
    private lateinit var recordButton: ImageButton
    private val stopTimer = Handler()

    companion object {
        public val TAG = RecordButtonFragment::class.java.simpleName
        private const val ANIMATION_DELAY = 200L
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.record_button_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordButton = view.findViewById<FloatingActionButton>(R.id.button_record)
        recordButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startRecording()
                    false
                }
                MotionEvent.ACTION_UP -> {
                    stopRecording()
                    false
                }
                else -> {
                    false
                }
            }
        }
    }

    private fun startRecording() {
        if (hasAudioRecordPermission()) {
            stopTimer.postDelayed({ stopRecording() }, 15500)
            viewModel.startRecording()

            delayedVibrate(recordButton)
            animateButtonOnStart()

            var recFragment = fragmentManager!!.findFragmentByTag(RecordingFragment.TAG)
            if (recFragment == null) {
                addRecordingFragment()
            }
        } else {
            requestAudioRecordingPermission()
        }
    }

    private fun addRecordingFragment() {
        Handler().postDelayed({
            fragmentManager!!.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    RecordingFragment(),
                    RecordingFragment.TAG
                )
                .addToBackStack(RecordingFragment.TAG)
                .commit()
        }, ANIMATION_DELAY)
    }

    private fun stopRecording() {
        stopTimer.removeCallbacksAndMessages(null)
        viewModel.stopRecording()

        delayedVibrate(recordButton)
        animateButtonOnStop()
    }

    private fun animateButtonOnStart() {
        recordButton.animate()
            .scaleY(1.75f)
            .scaleX(1.75f)
            .duration = ANIMATION_DELAY
    }

    private fun animateButtonOnStop() {
        recordButton.animate()
            .scaleY(1f)
            .scaleX(1f)
            .duration = ANIMATION_DELAY
    }

    private fun delayedVibrate(view: View) {
        Handler().postDelayed(
            { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) },
            ANIMATION_DELAY
        )
    }

    private fun hasAudioRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioRecordingPermission() {
        Completable
            .fromAction {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    100
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe()
    }
}