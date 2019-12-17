package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.audio.PlaybackEvent
import com.voizy.android.middleware.firebase.models.Voizy
import com.voizy.android.ui.widget.PlayPauseButton
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.VoizyDetailsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.voizy_details_fragment.*
import org.koin.android.ext.android.inject

class VoizyDetailsFragment : BaseFragment() {

    private val viewModel: VoizyDetailsViewModel by inject()
    private val shareRequests = PublishSubject.create<Voizy>()

    companion object {
        public val TAG = RecordingFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun useCustomBackPress(): Boolean {
        return true
    }

    override fun onBackPressed() {
        fragmentManager!!.popBackStack(
            RecordingFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.voizy_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDetails()
        initPlayback()
        initShare()
        initFinish()
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
        RxView.clicks(btn_details_playback)
            .flatMap { viewModel.togglePlay() }
            .autoDisposable(getScopeProvider())
            .subscribe()

        viewModel.playbackEvents
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                if (it.playbackEvent == PlaybackEvent.START) {
                    btn_details_playback.state = PlayPauseButton.State.STOP_ICON
                } else if (it.playbackEvent == PlaybackEvent.STOP) {
                    btn_details_playback.state = PlayPauseButton.State.PLAY_ICON
                }
            }
    }

    private fun initShare() {
        RxView.clicks(btn_details_share_voizy)
            .autoDisposable(getScopeProvider())
            .subscribe { viewModel.share(context!!) }
    }

    private fun initFinish() {
        RxView.clicks(btn_details_finish)
            .switchMap { viewModel.deleteLocalFile() }
            .autoDisposable(getScopeProvider())
            .subscribe {
                fragmentManager!!.popBackStack(
                    RecordingFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
    }
}