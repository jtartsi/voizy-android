package com.voizy.android.ui.fragment

import android.Manifest
import android.content.Intent
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
import com.voizy.android.audio.AudioRecorder
import com.voizy.android.ui.MainActivity
import com.voizy.android.ui.widget.createoptions.CreateEvent
import com.voizy.android.ui.widget.createoptions.CreateOptionsWidget
import com.voizy.android.utils.SupportedFileTypes
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.utils.toPair
import com.voizy.android.viewmodels.CreateOptionsViewModel
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject

@SuppressWarnings("ClickableViewAccessibility")
class CreateOptionsFragment : Fragment() {

    private val viewModel: CreateOptionsViewModel by inject<CreateOptionsViewModel>()
    private lateinit var createOptions: CreateOptionsWidget
    private val stopTimer = Handler()

    companion object {
        public val TAG = CreateOptionsFragment::class.java.simpleName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.create_options_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createOptions = view.findViewById(R.id.create_options_widget)
    }

    override fun onStart() {
        super.onStart()
        createOptions.getButtonEvents()
            .autoDisposable(getScopeProvider())
            .subscribe {
                when (it) {
                    CreateEvent.START_REC_MIC -> startRecording()
                    CreateEvent.STOP_REC_MIC -> stopRecording()
                    CreateEvent.CHOOSE_FILE -> pickFile()
                }
            }

        framgentChangeEvents()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.getFragmentTag() == LibraryFragment.TAG) {
                    fragmentManager!!.beginTransaction()
                        .show(this)
                        .commit()
                }
            }

        viewModel.getRecordingEvents()
            .withLatestFrom(framgentChangeEvents(), toPair())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (
                    it.first == AudioRecorder.RecordingEvent.STOP ||
                    it.first == AudioRecorder.RecordingEvent.FILE_RECEIVED &&
                    it.second.getFragmentTag() == RecordingFragment.TAG
                ) {
                    fragmentManager!!.beginTransaction()
                        .hide(this)
                        .commit()
                } else if (it.first == AudioRecorder.RecordingEvent.STOP_UNDER_MINIMUM_TIME) {
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
    }

    override fun onResume() {
        super.onResume()
        createOptions.state = CreateOptionsWidget.State.CLOSED
    }

    private fun framgentChangeEvents(): Observable<BaseFragment> {
        return Observable.create { emitter ->
            fragmentManager!!.addOnBackStackChangedListener {
                val topFragment = fragmentManager!!.findFragmentById(R.id.fragment_container)
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

            createOptions.state = CreateOptionsWidget.State.CLOSED
            var recFragment = fragmentManager!!.findFragmentByTag(RecordingFragment.TAG)
            if (recFragment == null) {
                addRecordingFragment()
            }
        } else {
            requestAudioRecordingPermission()
        }
    }

    private fun stopRecording() {
        stopTimer.removeCallbacksAndMessages(null)
        viewModel.stopRecording()
    }

    private fun pickFile() {
        val pickFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "audio/*"
            putExtra(Intent.EXTRA_MIME_TYPES, SupportedFileTypes.toArray())
        }
        activity!!.startActivityForResult(pickFileIntent, MainActivity.PICK_FILE_REQUEST_CODE)
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