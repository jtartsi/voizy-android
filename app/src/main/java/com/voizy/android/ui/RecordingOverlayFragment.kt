package com.voizy.android.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import timber.log.Timber

class RecordingOverlayFragment : Fragment() {

    private val viewModel: RecordingOverlayViewModel by inject<RecordingOverlayViewModel>()
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(viewLifecycleOwner) }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 100
        public val TAG = RecordingOverlayFragment::class.java.simpleName
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION &&
            grantResults[0] != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "onRequestPermissionsResult() NO permission")
            fragmentManager!!.popBackStack(TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        } else {
            Log.d(TAG, "onRequestPermissionsResult() permission")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.recording_overlay_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        viewModel.recordingEvents()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe {
                Timber.d("recording event $it")
            }

        Completable
            .fromAction {
                requestPermissions(
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_PERMISSION
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(scopeProvider)
            .subscribe()
    }
}