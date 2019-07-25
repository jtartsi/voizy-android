package com.voizy.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.viewmodels.RecordingOverlayViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import org.koin.android.ext.android.inject
import timber.log.Timber

class RecordingOverlayFragment : Fragment() {

    private val viewModel: RecordingOverlayViewModel by inject<RecordingOverlayViewModel>()
    private val scopeProvider by lazy { AndroidLifecycleScopeProvider.from(viewLifecycleOwner) }

    companion object {

        public val TAG = RecordingOverlayFragment::class.java.simpleName
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
    }
}