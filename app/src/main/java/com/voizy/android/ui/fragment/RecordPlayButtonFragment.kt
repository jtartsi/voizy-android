package com.voizy.android.ui.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.VoizyRecorder
import com.voizy.android.ui.widget.RecordPlayButton
import com.voizy.android.ui.widget.RecordPlayButton.Event
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.RecordPlayButtonViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import timber.log.Timber

@SuppressWarnings("ClickableViewAccessibility")
class RecordPlayButtonFragment : Fragment() {

    private val viewModel: RecordPlayButtonViewModel by inject<RecordPlayButtonViewModel>()
    private lateinit var recordPlayButton: RecordPlayButton
    private val stopTimer = Handler()

    companion object {
        public val TAG = RecordPlayButtonFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.record_button_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recordPlayButton = view.findViewById(R.id.button_record)
        recordPlayButton.getButtonEvents()
            .autoDisposable(getScopeProvider())
            .subscribe {
                when (it) {
                    Event.START_RECORD -> {
                        startRecording()
                    }
                    Event.STOP_RECORD -> {
                        stopRecording()
                    }
                    Event.PLAY -> {
                        viewModel.startPreviewVoizyPlayback()
                    }
                }
            }

        mainFragmentLaunched()
            .observeOn(Schedulers.io())
            .autoDisposable(getScopeProvider())
            .subscribe {
                recordPlayButton.state = RecordPlayButton.State.RECORD
            }

        viewModel.getRecordingEvents()
            .filter { it == VoizyRecorder.RecordingEvent.FINISHED }
            .observeOn(Schedulers.io())
            .autoDisposable(getScopeProvider())
            .subscribe {
                recordPlayButton.state = RecordPlayButton.State.PLAY
            }
    }

    private fun mainFragmentLaunched(): Observable<BaseFragment> {
        val backstackSubject = PublishSubject.create<Fragment>()

        fragmentManager!!.addOnBackStackChangedListener {
            val topFragment = fragmentManager!!.findFragmentById(R.id.fragment_container)
            if (topFragment != null) {
                backstackSubject.onNext(topFragment)
            }
        }
        return backstackSubject
            .filter { it is BaseFragment }
            .map { it as BaseFragment }
            .filter { it.getBackstackTag() == MainFragment.TAG }
    }

    private fun startRecording() {
        if (hasAudioRecordPermission()) {
            stopTimer.postDelayed({ stopRecording() }, 15000)
            viewModel.startRecording()

            var recFragment = fragmentManager!!.findFragmentByTag(RecordingFragment.TAG)
            if (recFragment == null) {
                Timber.d("startRecording()")
                addRecordingFragment()
            }
        } else {
            requestAudioRecordingPermission()
        }
    }

    private fun stopRecording() {
        recordPlayButton.handleStopRecording()
        stopTimer.removeCallbacksAndMessages(null)
        val recordingOverMinimumTime = viewModel.stopRecording()
        Timber.d("stopRecording()")
        if (!recordingOverMinimumTime) {
            fragmentManager!!.popBackStackImmediate(
                RecordingFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
            Snackbar.make(this.view!!, R.string.hold_to_record_guide, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun addRecordingFragment() {
        fragmentManager!!.beginTransaction()
            .replace(
                R.id.fragment_container,
                RecordingFragment(),
                RecordingFragment.TAG
            )
            .addToBackStack(RecordingFragment.TAG)
            .commit()
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