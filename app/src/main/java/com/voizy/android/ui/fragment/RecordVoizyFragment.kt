package com.voizy.android.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
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
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.showTimeSecondsAndTenths
import com.voizy.android.viewmodels.RecordVoizyViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.record_voizy_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RecordVoizyFragment : BaseFragment() {

    private val viewModel: RecordVoizyViewModel by inject()
    private val stopTimer = Handler()
    private var timerDisposable: Disposable? = null
    private var timerSubject = PublishSubject.create<Long>()

    private lateinit var recButtonEvents: Observable<MotionEvent>
    private lateinit var recStartEvents: Observable<MotionEvent>
    private lateinit var recStopEvents: Observable<MotionEvent>

    companion object {
        val TAG = RecordVoizyFragment::class.java.simpleName
        private const val MAX_RECORDING_TIME_MS = 15000L
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

        recStartEvents = recButtonEvents
            .filter { hasMicrophonePermission() }
            .filter { it.action == MotionEvent.ACTION_DOWN }
            .share()

        recStopEvents = recButtonEvents
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
        initAutoStop()
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
        recStartEvents
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.startRecording() }

        recStopEvents
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.stopRecording() }
    }

    private fun initAutoStop() {
        Observable.timer(MAX_RECORDING_TIME_MS, TimeUnit.MILLISECONDS)
            .autoDisposable(getScopeProvider())
            .subscribe { navigateToSaveLayoutFragment() }
    }

    private fun initRecordingTime() {
        val updatePeriodInMs = 100L
        tv_recording_time.showTimeSecondsAndTenths(0)

        viewModel.recordingEvents
            .filter { it == AudioRecorder.RecordingEvent.STARTED }
            .switchMap { timerObservable() }
            .doOnNext { Timber.d("recordingEvents.doOnNext() $it") }
            .takeWhile { viewModel.recording.get() }
            .doOnNext { Timber.d("recordingEvents.doOnNext2() $it") }
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { tv_recording_time.showTimeSecondsAndTenths(it) }

        // viewModel.recordingEvents
        //     .filter { it == AudioRecorder.RecordingEvent.STARTED }
        //     .withLatestFrom(timerObservable(), toPair())
        //     .doOnNext { Timber.d("recordingEvents.doOnNext() ${it.first}, ${it.second}") }
        //     .takeWhile { it.first == AudioRecorder.RecordingEvent.STARTED }
        //     .observeOn(AndroidSchedulers.mainThread())
        //     .autoDisposable(getScopeProvider())
        //     .subscribe {
        //         tv_recording_time.showTimeSecondsAndTenths(it.second)
        //     }

        // recStartEvents
        //     .doOnNext { tv_recording_time.visibility = View.VISIBLE }
        //     .switchMap {
        //         Observable.intervalRange(
        //             0L, MAX_RECORDING_TIME_MS, 0L, updatePeriodInMs,
        //             TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()
        //         )
        //     }
        //     .withLatestFrom(viewModel.recordingEvents, toPair())
        //     .takeWhile { it.second == AudioRecorder.RecordingEvent.STARTED }
        //     .map { it.first * 100 }
        //     .autoDisposable(getScopeProvider())
        //     .subscribe { tv_recording_time.showTimeSecondsAndTenths(it) }

        recStopEvents
            .autoDisposable(getScopeProvider())
            .subscribe { stopTimer.removeCallbacksAndMessages(null) }
    }

    private fun timerObservable(): Observable<Long> {
        return Observable.intervalRange(
            0L, MAX_RECORDING_TIME_MS, 0L, 100,
            TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()
        ).map { it * 100 }
    }

    private fun initRecDotAnimation() {
        val blinkingAnimation = AlphaAnimation(1f, 0f)
        blinkingAnimation.repeatMode = Animation.REVERSE
        blinkingAnimation.repeatCount = Animation.INFINITE
        blinkingAnimation.duration = 500

        recStartEvents.autoDisposable(getScopeProvider())
            .subscribe {
                iv_recording_indicator.animation = blinkingAnimation
                iv_recording_indicator.visibility = View.VISIBLE
                blinkingAnimation.reset()
                blinkingAnimation.start()
            }
        recStopEvents.autoDisposable(getScopeProvider())
            .subscribe { blinkingAnimation.cancel() }
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
}