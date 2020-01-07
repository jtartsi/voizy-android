package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.ui.widget.VoizyWebViewClient
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.CloudVideoPullViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.cloud_video_pull_fragment.*
import org.koin.android.ext.android.inject
import timber.log.Timber

class CloudVideoPullFragment : BaseFragment() {

    private val viewModel: CloudVideoPullViewModel by inject()

    companion object {
        public val TAG = CloudVideoPullFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun useCustomBackPress(): Boolean {
        return true
    }

    override fun onBackPressed() {
        if (isLoadingOverlayVisible()) {
            viewModel.cancelDownload()
            showLoadingLayout(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.cloud_video_pull_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        initWebView()
        initShortcutButtons()
        initVideoDownload()
    }

    private fun initWebView() {
        wv_cloud_video_pull.settings.javaScriptEnabled = true
        val webViewClient = VoizyWebViewClient()
        webViewClient.pageChangedListener = { url ->
            et_cloud_video_pull_url.setText(url)
        }
        wv_cloud_video_pull.webViewClient = webViewClient
    }

    private fun initShortcutButtons() {
        btn_cloud_video_pull_youtube.setOnClickListener {
            wv_cloud_video_pull.loadUrl("https://youtube.com")
        }
        btn_cloud_video_pull_vimeo.setOnClickListener {
            wv_cloud_video_pull.loadUrl("https://vimeo.com/watch")
        }
        btn_cloud_video_pull_twitch.setOnClickListener {
            wv_cloud_video_pull.loadUrl("https://twitch.com")
        }
    }

    private fun initVideoDownload() {
        btn_cloud_video_pull_next.setOnClickListener {
            showLoadingLayout(true)
            viewModel.downloadVideo(et_cloud_video_pull_url.text.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .autoDisposable(getScopeProvider())
                .subscribe(videoDownloadConsumer())
        }
    }

    private fun videoDownloadConsumer(): Consumer<String> {
        return Consumer { filePath ->
            Timber.d("cloud-pull videoDownloadConsumer progress $filePath")

            val bundle = Bundle()
            bundle.putString(VoizyApp.KEY_DATA, filePath)
            val audioClipFragment = AudioClipFragment()
            audioClipFragment.arguments = bundle

            fragmentManager!!.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    audioClipFragment,
                    AudioClipFragment.TAG
                )
                .addToBackStack(AudioClipFragment.TAG)
                .commit()
        }
    }

    private fun showLoadingLayout(show: Boolean) {
        if (show) {
            layout_cloud_video_pull_loading_overlay.visibility = View.VISIBLE
            setUiEnabled(false)
        } else {
            layout_cloud_video_pull_loading_overlay.visibility = View.GONE
            setUiEnabled(true)
        }
    }

    private fun isLoadingOverlayVisible(): Boolean {
        return layout_cloud_video_pull_loading_overlay.visibility == View.VISIBLE
    }

    private fun setUiEnabled(enabled: Boolean) {
        wv_cloud_video_pull.isClickable = enabled
        wv_cloud_video_pull.isEnabled = enabled
        et_cloud_video_pull_url.isEnabled = enabled
        btn_cloud_video_pull_next.isClickable = enabled
        btn_cloud_video_pull_youtube.isClickable = enabled
        btn_cloud_video_pull_vimeo.isClickable = enabled
        btn_cloud_video_pull_twitch.isClickable = enabled
    }
}