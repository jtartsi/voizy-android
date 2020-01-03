package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
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
            Timber.d("cloud-pull Url in web view: ${wv_cloud_video_pull.url}")
            viewModel.downloadVideo(wv_cloud_video_pull.url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(showloadingLayout())
                .filter { it >= 100f }
                .autoDisposable(getScopeProvider())
                .subscribe {
                    Timber.d("cloud-pull initLoadAndNavigateNext $it")
                    fragmentManager!!.beginTransaction()
                        .replace(
                            R.id.fragment_container,
                            AudioClipFragment(),
                            AudioClipFragment.TAG
                        )
                        .addToBackStack(AudioClipFragment.TAG)
                        .commit()
                }
        }
    }

    private fun showloadingLayout(): Consumer<Float> {
        return Consumer {
            if (layout_cloud_video_pull_loading_overlay.visibility != View.VISIBLE) {
                layout_cloud_video_pull_loading_overlay.visibility = View.VISIBLE
            }
        }
    }
}