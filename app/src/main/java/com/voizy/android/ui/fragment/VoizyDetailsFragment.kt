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
import com.voizy.android.ui.widget.PlaybackButton
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.VoizyDetailsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.voizy_details_fragment.*
import org.koin.android.ext.android.inject

class VoizyDetailsFragment : BaseFragment() {

    private val viewModel: VoizyDetailsViewModel by inject()

    companion object {
        val TAG = VoizyDetailsFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun useCustomBackPress(): Boolean {
        return true
    }

    override fun onBackPressed() {
        close()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.voizy_details_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
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
                    btn_details_playback.state = PlaybackButton.State.STOP_ICON
                } else if (it.playbackEvent == PlaybackEvent.STOP) {
                    btn_details_playback.state = PlaybackButton.State.PLAY_ICON
                }
            }
    }

    private fun initShare() {
        RxView.clicks(btn_details_share_voizy)
            .switchMap { viewModel.share(context!!) }
            .autoDisposable(getScopeProvider())
            .subscribe()
    }

    private fun initFinish() {
        RxView.clicks(btn_details_finish)
            .autoDisposable(getScopeProvider())
            .subscribe {
                close()
            }
    }

    private fun close() {

        viewModel.deleteLocalFile()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe {
                fragmentManager!!.popBackStack(
                    TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                fragmentManager!!.popBackStack(
                    SaveVoizyFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                fragmentManager!!.popBackStack(
                    AudioClipFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                fragmentManager!!.popBackStack(
                    YoutubeDownloadFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
                fragmentManager!!.popBackStack(
                    RecordVoizyFragment.TAG, FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
    }
}