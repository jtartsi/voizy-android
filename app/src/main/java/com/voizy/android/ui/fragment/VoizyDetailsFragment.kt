package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.widget.PlayPauseButton
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.VoizyDetailsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.voizy_details_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class VoizyDetailsFragment : BaseFragment() {

    private val viewModel: VoizyDetailsViewModel by inject()
    private val shareRequests = PublishSubject.create<Voizy>()

    companion object {
        public val TAG = RecordingFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun onBackPressed() {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.d("voizy-details onCreateView()")
        return inflater.inflate(R.layout.voizy_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Timber.d("voizy-details onViewCreated()")
        initDetails()
        initPlayback()
        initShare()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopPlayback()
            .autoDisposable(getScopeProvider())
            .subscribe()
    }

    private fun initDetails() {
        viewModel.getLastVoizyToBeSaved()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizy ->
                tv_voizy_details_title.text = voizy.name
                tv_voizy_details_tags.text = voizy.hashTags
            }
    }

    private fun initPlayback() {
        RxView.clicks(btn_playback)
            .flatMap { viewModel.togglePlay() }
            .autoDisposable(getScopeProvider())
            .subscribe()

        viewModel.playbackEvents
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.playbackEvent == PlaybackEvent.START) {
                    btn_playback.state = PlayPauseButton.State.STOP_ICON
                } else if (it.playbackEvent == PlaybackEvent.STOP) {
                    btn_playback.state = PlayPauseButton.State.PLAY_ICON
                }
            }
    }

    private fun initShare() {
        shareRequests
            .doOnNext(showSharingToast())
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.share(context!!) }
    }

    private fun showSharingToast(): Consumer<Voizy> {
        return Consumer {
            Snackbar.make(
                view!!,
                getString(R.string.voizy_sharing, it.name),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
}