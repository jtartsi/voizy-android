package com.voizy.android.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordVoizyViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import kotlinx.android.synthetic.main.record_voizy_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class RecordVoizyFragment : BaseFragment() {

    private val viewModel: RecordVoizyViewModel by inject()
    private val stopTimer = Handler()

    private lateinit var recButtonEvents: Observable<MotionEvent>

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
    }

    override fun onStart() {
        super.onStart()
        initRequestMicrophonePermission()
        initRecording()
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
        recButtonEvents
            .doOnNext { Timber.d("initRecording.doOnNext()") }
            .filter { hasMicrophonePermission() }
            .autoDisposable(getScopeProvider())
            .subscribe {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        Timber.d("initRecording down")
                    }
                    MotionEvent.ACTION_UP -> {
                        Timber.d("initRecording up")
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
}