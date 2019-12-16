package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.VoizyDetailsViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.row_item_voizy.*
import kotlinx.android.synthetic.main.voizy_details_fragment.*
import org.koin.android.ext.android.inject

class VoizyDetailsFragment : BaseFragment() {

    private val viewModel: VoizyDetailsViewModel by inject()

    companion object {
        public val TAG = RecordingFragment::class.java.simpleName
        public const val ARGS_KEY_VOIZY = "args_voizy"
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
        return inflater.inflate(R.layout.voizy_details_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initDetails()
        initPlayback()
        initShare()
    }

    private fun initDetails() {
        viewModel.getLastVoizyToBeSaved()
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { voizy ->
                tv_voizy_details_title.text = voizy.name
                tv_voizy_row_tags.text = voizy.hashTags
                viewModel.getAudioFileLengthInSeconds(voizy.localPath)
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDisposable(getScopeProvider())
                    .subscribe { tv_voizy_details_duration.text = it.toString() }
            }
    }

    private fun initPlayback() {
    }

    private fun initShare() {
    }
}