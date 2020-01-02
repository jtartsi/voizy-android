package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.voizy.android.R
import com.voizy.android.ui.widget.VoizyWebViewClient
import kotlinx.android.synthetic.main.cloud_video_pull_fragment.*
import timber.log.Timber

class CloudVideoPullFragment : BaseFragment() {

    companion object {
        public val TAG = CloudVideoPullFragment::class.java.simpleName
    }

    override fun getFragmentTag(): String {
        return TAG
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
        initLoadAndNavigateNext()
    }

    private fun initWebView() {
        wv_cloud_video_pull.settings.javaScriptEnabled = true
        val webViewClient = VoizyWebViewClient()
        webViewClient.pageChangedListener = { url ->
            et_cloud_video_pull_url.setText(url)
        }
        wv_cloud_video_pull.webViewClient = webViewClient
        wv_cloud_video_pull.loadUrl("https://google.com")
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

    private fun initLoadAndNavigateNext() {
        btn_cloud_video_pull_next.setOnClickListener {
            Timber.d("video-pull Url in web view: ${wv_cloud_video_pull.url}")
        }
    }
}