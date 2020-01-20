package com.voizy.android.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.ui.MainActivity
import com.voizy.android.utils.SupportedFileTypes
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.showTimeSecondsAndTenths
import com.voizy.android.viewmodels.RecordVoizyViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.record_voizy_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RecordVoizyFragment : BaseFragment() {

    private val viewModel: RecordVoizyViewModel by inject()

    private lateinit var recButtonEvents: Observable<MotionEvent>
    private lateinit var recStartAction: Observable<MotionEvent>
    private lateinit var recStopAction: Observable<MotionEvent>

    companion object {
        val TAG = RecordVoizyFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.record_voizy_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recButtonEvents = RxView.touches(btn_rec_voizy)
            .toFlowable(BackpressureStrategy.LATEST)
            .toObservable()
            .share()

        recStartAction = recButtonEvents
            .filter { hasMicrophonePermission() }
            .filter { it.action == MotionEvent.ACTION_DOWN }
            .share()

        recStopAction = recButtonEvents
            .filter { hasMicrophonePermission() }
            .filter { it.action == MotionEvent.ACTION_UP }
            .share()
    }

    override fun onStart() {
        super.onStart()
        initRequestMicrophonePermission()
        initRecordingStart()
        initRecordingTime()
        initRecDotAnimation()
        initStopRecording()
        initOpenYoutubeDL()
        initOpenFileImport()
    }

    private fun initRequestMicrophonePermission() {
        recButtonEvents
            .filter { !hasMicrophonePermission() }
            .doOnNext {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    100
                )
            }
            .autoDisposable(getScopeProvider())
            .subscribe()
    }

    private fun initRecordingStart() {
        recStartAction
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.startRecording() }

        recStopAction
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.stopRecording() }
    }

    private fun initRecordingTime() {
        tv_recording_time.showTimeSecondsAndTenths(0)

        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STARTED }
            .switchMap { timerObservable() }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { tv_recording_time.showTimeSecondsAndTenths(it) }

        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STOP_UNDER_MINIMUM_TIME }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { tv_recording_time.showTimeSecondsAndTenths(0) }
    }

    private fun timerObservable(): Observable<Long> {
        return Observable
            .intervalRange(
                0L, AudioRecorder.MAX_RECORDING_TIME_MS, 0L, 100,
                TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()
            )
            .takeWhile { viewModel.recording.get() }
            .map { it * 100 }
    }

    private fun initRecDotAnimation() {
        val blinkingAnimation = AlphaAnimation(1f, 0f)
        blinkingAnimation.repeatMode = Animation.REVERSE
        blinkingAnimation.repeatCount = Animation.INFINITE
        blinkingAnimation.duration = 500

        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STARTED }
            .doOnNext { Timber.d("rec-iss initRecDotAnimation $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                iv_recording_indicator.animation = blinkingAnimation
                iv_recording_indicator.visibility = View.VISIBLE
                blinkingAnimation.reset()
                blinkingAnimation.start()
            }

        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STOP_UNDER_MINIMUM_TIME }
            .doOnNext { Timber.d("rec-iss initRecDotAnimation $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                blinkingAnimation.cancel()
                iv_recording_indicator.visibility = View.GONE
            }
    }

    private fun initStopRecording() {
        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STOP }
            .autoDisposable(getScopeProvider())
            .subscribe { navigateToSaveLayoutFragment() }

        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STOP_UNDER_MINIMUM_TIME }
            .autoDisposable(getScopeProvider())
            .subscribe {
                Snackbar.make(
                    this.view!!,
                    R.string.hold_to_record_guide,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun hasMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context!!,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun navigateToSaveLayoutFragment() {
        fragmentManager!!.beginTransaction()
            .replace(
                R.id.fragment_container,
                SaveVoizyFragment(),
                SaveVoizyFragment.TAG
            )
            .addToBackStack(SaveVoizyFragment.TAG)
            .commit()
    }

    private fun initOpenFileImport() {
        RxView.clicks(btn_open_file_import)
            .autoDisposable(getScopeProvider())
            .subscribe {
                viewModel.logFileImportSelected()
                val pickFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, SupportedFileTypes.toArray())
                }
                activity!!.startActivityForResult(
                    pickFileIntent,
                    MainActivity.PICK_FILE_REQUEST_CODE
                )
            }
    }

    private fun initOpenYoutubeDL() {
        RxView.clicks(btn_open_youtube_dl)
            .autoDisposable(getScopeProvider())
            .subscribe {
                viewModel.logYoutubeDLSelected()
                fragmentManager!!.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        YoutubeDownloadFragment(),
                        YoutubeDownloadFragment.TAG
                    )
                    .addToBackStack(YoutubeDownloadFragment.TAG)
                    .commit()
            }
    }
}