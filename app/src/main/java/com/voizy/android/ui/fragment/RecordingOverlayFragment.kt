package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.recording_overlay_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RecordingOverlayFragment : Fragment() {

    private val viewModel: RecordingOverlayViewModel by inject<RecordingOverlayViewModel>()
    private lateinit var playButton: View
    private var timer: Disposable? = null

    companion object {
        public val TAG = RecordingOverlayFragment::class.java.simpleName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recording_overlay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playButton = view.findViewById<View>(R.id.btn_play_preview)
        playButton.setOnClickListener {
            viewModel.playAudio()
        }

        btn_save_voizy.setOnClickListener {
            Timber.d("save-voizy-iss onClick()")
            viewModel.saveVoizy(et_voizy_name.text.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.recordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                Timber.d("recording event $it")
                when (it) {
                    VoizyRecorder.RecordingEvents.STARTED -> {
                        startTimer()
                        playButton.visibility = View.GONE
                        et_voizy_name.visibility = View.GONE
                        btn_save_voizy.visibility = View.GONE
                    }
                    VoizyRecorder.RecordingEvents.FINISHED -> {
                        stopTimer()
                        playButton.visibility = View.VISIBLE
                        et_voizy_name.visibility = View.VISIBLE
                        btn_save_voizy.visibility = View.VISIBLE
                    }
                    VoizyRecorder.RecordingEvents.START_FAILED -> {
                        Timber.e("Failed to start recording")
                    }
                    VoizyRecorder.RecordingEvents.CLOSE_FAILED -> {
                        Timber.e("Failed to close recording")
                    }
                }
            }

        viewModel.saveVoizyEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                Timber.d("save-voizy-iss save event received ${it.filePath}")
                if (it.filePath.isNotEmpty()) {
                    fragmentManager!!.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    Timber.d("save-voizy-iss not empty")
                    Snackbar.make(view!!, "Voizy saved. Share and let others enjoy!", Snackbar.LENGTH_LONG)
                } else {
                    Timber.d("save-voizy-iss empty")
                    Snackbar.make(view!!, "Saving Voizy failed", Snackbar.LENGTH_SHORT)
                }
            }
    }

    private fun startTimer() {
        tv_recording_time.text = "00:00 / 00:15"
        timer = Observable.intervalRange(
            1L, 15, 1L, 1L,
            TimeUnit.SECONDS, AndroidSchedulers.mainThread()
        )
            .map {
                if (it < 10) {
                    "0$it"
                } else {
                    it.toString()
                }
            }
            .map { "00:$it / 00:15" }
            .autoDisposable(getScopeProvider())
            .subscribe {
                tv_recording_time.text = it
            }
    }

    private fun stopTimer() {
        timer?.let { it.dispose() }
    }
}