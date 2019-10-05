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
import com.voizy.android.utils.toPair
import com.voizy.android.viewmodels.RecordPlayButtonViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
                    Event.START_RECORD -> startRecording()
                    Event.STOP_RECORD -> stopRecording()
                    Event.PLAY -> viewModel.startPreviewVoizyPlayback()
                }
            }

        fragmentChangeListener()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.getFragmentTag() == MainFragment.TAG) {
                    Timber.d("frag-rec-event set RECORD")
                    recordPlayButton.state = RecordPlayButton.State.RECORD
                }
            }

        viewModel.getRecordingEvents()
            .withLatestFrom(fragmentChangeListener(), toPair())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.first == VoizyRecorder.RecordingEvent.STOP &&
                    it.second.getFragmentTag() == RecordingFragment.TAG
                ) {
                    Timber.d("frag-rec-event set PLAY")
                    recordPlayButton.state = RecordPlayButton.State.PLAY
                } else if (it.first == VoizyRecorder.RecordingEvent.STOP_UNDER_MINIMUM_TIME) {
                    Timber.d("frag-rec-event under minimum time")
                    fragmentManager!!.popBackStackImmediate(
                        RecordingFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    Snackbar.make(
                        this.view!!,
                        R.string.hold_to_record_guide,
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }

        // Observable.combineLatest(
        //     fragmentChangeListener(),
        //     viewModel.getRecordingEvents(),
        //     com.voizy.android.utils.toPair()
        // )
        //     .subscribeOn(Schedulers.io())
        //     .observeOn(AndroidSchedulers.mainThread())
        //     .autoDisposable(getScopeProvider())
        //     .subscribe {
        //         Timber.d("frag-rec-event frag: ${it.first.getFragmentTag()}, event: ${it.second.name}")
        //         if (it.first.getFragmentTag() == MainFragment.TAG) {
        //             Timber.d("frag-rec-event set RECORD")
        //             recordPlayButton.state = RecordPlayButton.State.RECORD
        //         } else if (it.first.getFragmentTag() == RecordingFragment.TAG &&
        //             it.second == VoizyRecorder.RecordingEvent.STOP
        //         ) {
        //             Timber.d("frag-rec-event set PLAY")
        //             recordPlayButton.state = RecordPlayButton.State.PLAY
        //         } else if (it.second == VoizyRecorder.RecordingEvent.STOP_UNDER_MINIMUM_TIME) {
        //             Timber.d("frag-rec-event under minimum time")
        //             fragmentManager!!.popBackStackImmediate(
        //                 RecordingFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
        //             )
        //             Snackbar.make(
        //                 this.view!!,
        //                 R.string.hold_to_record_guide,
        //                 Snackbar.LENGTH_SHORT
        //             ).show()
        //         }
        //     }
    }

    private fun fragmentChangeListener(): Observable<BaseFragment> {
        return Observable.create { emitter ->
            fragmentManager!!.addOnBackStackChangedListener {
                val topFragment = fragmentManager!!.findFragmentById(R.id.fragment_container)
                Timber.d("frag-rec-event topFragment $topFragment")
                if (topFragment != null && topFragment is BaseFragment) {
                    emitter.onNext(topFragment)
                }
            }
        }
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
        viewModel.stopRecording()
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