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
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordVoizyViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.record_voizy_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class RecordVoizyFragment : BaseFragment() {

    private val viewModel: RecordVoizyViewModel by inject()
    private val stopTimer = Handler()
    private var timerDisposable: Disposable? = null

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
        initRecording()
        initRecordingTime()
        initRecDotAnimation()
        initNavigateToSaveFragment()
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

    private fun initRecording() {
        recStartEvents
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.startRecording() }

        recStopEvents
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.stopRecording() }
    }

    private fun initRecordingTime() {
        val updatePeriodInMs = 100L
        showTimeText(0)
        recStartEvents
            .doOnNext {
                tv_recording_time.visibility = View.VISIBLE
                stopTimer.postDelayed({
                    // TODO voizy-create make this better
                    viewModel.stopRecording()
                    stopTimer.removeCallbacksAndMessages(null)
                }, MAX_RECORDING_TIME_MS)
            }
            .doOnNext { Timber.d("TimeText doOnNext()") }
            .switchMap {
                Observable.intervalRange(
                    0L, 15000, 0L, updatePeriodInMs,
                    TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread()
                )
            }
            .map { it * updatePeriodInMs }
            .autoDisposable(getScopeProvider())
            .subscribe {
                Timber.d("TimeText value $it")
                showTimeText(it)
            }

        recStopEvents
            .autoDisposable(getScopeProvider())
            .subscribe { stopTimer.removeCallbacksAndMessages(null) }
    }

    private fun initRecDotAnimation() {
        val blinkingAnimation = AlphaAnimation(1f, 0f)
        blinkingAnimation.repeatMode = Animation.REVERSE
        blinkingAnimation.repeatCount = Animation.INFINITE
        blinkingAnimation.duration = 500
        iv_recording_indicator.animation = blinkingAnimation

        recStartEvents.autoDisposable(getScopeProvider())
            .subscribe {
                blinkingAnimation.reset()
                blinkingAnimation.start()
            }
        recStopEvents.autoDisposable(getScopeProvider())
            .subscribe { blinkingAnimation.cancel() }
    }

    private fun initNavigateToSaveFragment() {
        viewModel.recordingEvents
            .autoDisposable(getScopeProvider())
            .subscribe {
                when (it) {
                    AudioRecorder.RecordingEvent.STOP -> {
                        timerDisposable?.let { disposable ->
                            disposable.dispose()
                        }
                        navigateToSaveLayoutFragment()
                    }
                    AudioRecorder.RecordingEvent.START_FAILED -> {
                        Timber.e("Failed to start recording")
                    }
                    AudioRecorder.RecordingEvent.STOP_FAILED -> {
                        Timber.e("Failed to close recording")
                        tv_recording_time.visibility = View.GONE
                    }
                    else -> {
                    }
                }
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

    private fun showTimeText(timeInMs: Long) {
        Timber.d("showTimeText() $timeInMs")
        val inMillis = (timeInMs)
        val dateFormatter = SimpleDateFormat("s.S")
        val timeString = dateFormatter.format(Date(inMillis)).plus("s")
        tv_recording_time.text = timeString
    }

    // private fun showTimeText(timeInSeconds: Int) {
    //     Timber.d("showTimeText() $timeInSeconds")
    //     val inMillis = (timeInSeconds).toLong() * 1000
    //     val dateFormatter = SimpleDateFormat("mm:ss")
    //     val timeString = dateFormatter.format(Date(inMillis))
    //     tv_recording_time.text = timeString.plus(" / 00:15")
    // }
}