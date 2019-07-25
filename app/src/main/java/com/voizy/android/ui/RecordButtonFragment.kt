package com.voizy.android.ui

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
import androidx.fragment.app.FragmentManager
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

    companion object {
        public val TAG = RecordButtonFragment::class.java.simpleName
        private const val ANIMATION_DELAY = 200L
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
        if (hasAudioRecordPermission()) {
            viewModel.startRecording()

            delayedVibrate(view)

            view.animate()
                .scaleY(2f)
                .scaleX(2f)
                .duration = ANIMATION_DELAY

            Handler().postDelayed({
                fragmentManager!!.beginTransaction()
                    .add(R.id.fragment_container, RecordingOverlayFragment())
                    .addToBackStack(RecordingOverlayFragment.TAG)
                    .commit()
            }, ANIMATION_DELAY)
        } else {
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

    private fun stopRecording(view: View) {
        viewModel.stopRecording()

        delayedVibrate(view)

        view.animate()
            .scaleY(1f)
            .scaleX(1f)
            .duration = ANIMATION_DELAY

        Handler().postDelayed({
            fragmentManager!!.popBackStack(
                RecordingOverlayFragment.TAG,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }, ANIMATION_DELAY)
    }

    private fun delayedVibrate(view: View) {
        Handler().postDelayed(
            { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }, ANIMATION_DELAY
        )
    }

    private fun hasAudioRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}