package com.voizy.android.ui.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordingViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.recording_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

class RecordingFragment : BaseFragment() {

    private val viewModel: RecordingViewModel by inject()
    private var timerDisposable: Disposable? = null

    companion object {
        val TAG = RecordingFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.recording_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        // TODO audio-editor remove this
        initFileInput()
        initRecordEvents()

        if (!isFileSendAction()) {
            startTimer()
        } else {
            navigateToSaveLayoutFragment()
        }
    }

    private fun initFileInput() {
        Observable.just(isFileSendAction())
            .filter { it }
            .flatMap {
                val fileUri = arguments!!.get(VoizyApp.KEY_DATA) as Uri
                viewModel.saveReceivedFile(fileUri)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { showTimeText(it.durationInSecods.toInt()) }
    }

    private fun initRecordEvents() {
        viewModel.getRecordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(recordingEventConsumer())
    }

    private fun recordingEventConsumer(): Consumer<AudioRecorder.RecordingEvent> {
        return Consumer {
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
                }
                else -> {
                }
            }
        }
    }

    private fun startTimer() {
        showTimeText(0)
        timerDisposable = Observable.intervalRange(
            1L, 15, 1L, 1L,
            TimeUnit.SECONDS, AndroidSchedulers.mainThread()
        )
            .autoDisposable(getScopeProvider())
            .subscribe {
                showTimeText(it.toInt())
            }
    }

    private fun showTimeText(timeInSeconds: Int) {
        val inMillis = (timeInSeconds).toLong() * 1000
        val dateFormatter = SimpleDateFormat("mm:ss")
        val timeString = dateFormatter.format(Date(inMillis))
        tv_recording_time.text = timeString.plus(" / 00:15")
    }

    private fun isFileSendAction(): Boolean {
        return arguments != null &&
            arguments!!.get(VoizyApp.KEY_ACTION) == Intent.ACTION_SEND
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