package com.voizy.android.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.uber.autodispose.autoDisposable
import com.voizy.android.R
import com.voizy.android.VoizyApp
import com.voizy.android.ui.widget.VoizyWebViewClient
import com.voizy.android.utils.getScopeProvider
import com.voizy.android.viewmodels.YoutubeDownloadViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.youtube_download_fragment.*
import org.koin.android.ext.android.inject

class YoutubeDownloadFragment : BaseFragment() {

    private val viewModel: YoutubeDownloadViewModel by inject()
    private var downloadingOverlay: View? = null

    companion object {
        val TAG = YoutubeDownloadFragment::class.java.simpleName
        private val YOUTUBE_URL = "https://youtube.com"
    }

    override fun getFragmentTag(): String {
        return TAG
    }

    override fun useCustomBackPress(): Boolean {
        return true
    }

    override fun onBackPressed(): Boolean {
        return if (isLoadingOverlayVisible()) {
            viewModel.cancelDownload()
            showLoadingLayout(false)
            true
        } else {
            false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.youtube_download_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        downloadingOverlay = view.findViewById(R.id.layout_downloading_overlay)
    }

    override fun onStart() {
        super.onStart()
        initWebView()
        initVideoDownload()
    }

    fun resetWebView() {
        wv_cloud_video_pull?.loadUrl(YOUTUBE_URL)
        if (isLoadingOverlayVisible()) {
            viewModel.cancelDownload()
            showLoadingLayout(false)
        }
    }

    private fun initWebView() {
        wv_cloud_video_pull.settings.javaScriptEnabled = true
        val webViewClient = VoizyWebViewClient()
        webViewClient.pageChangedListener = { url ->
            if (isResumed) {
                if (url.contains("watch")) {
                    btn_download.visibility = View.VISIBLE
                    tv_download_hint.visibility = View.GONE
                } else {
                    btn_download.visibility = View.GONE
                    tv_download_hint.visibility = View.VISIBLE
                }
            }
        }
        wv_cloud_video_pull.webViewClient = webViewClient
        wv_cloud_video_pull.loadUrl(YOUTUBE_URL)
    }

    private fun initVideoDownload() {
        viewModel.downloadErrors
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(downloadErrorConsumer())

        viewModel.downloadProgressEvents
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe { tv_cloud_video_pull_dl_percentage.text = it.toString().plus("%") }

        viewModel.downloadCompletedEvents
            .observeOn(AndroidSchedulers.mainThread())
            .autoDisposable(getScopeProvider())
            .subscribe(videoDownloadConsumer())

        RxView.clicks(btn_download)
            .autoDisposable(getScopeProvider())
            .subscribe {
                showLoadingLayout(true)
                viewModel.download(wv_cloud_video_pull.url)
            }
    }

    private fun videoDownloadConsumer(): Consumer<String> {
        return Consumer { filePath ->
            wv_cloud_video_pull.loadUrl("")

            val bundle = Bundle()
            bundle.putString(VoizyApp.KEY_DATA, filePath)
            val audioClipFragment = AudioClipFragment()
            audioClipFragment.arguments = bundle

            activity!!.supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container,
                    audioClipFragment,
                    AudioClipFragment.TAG
                )
                .addToBackStack(AudioClipFragment.TAG)
                .commit()
        }
    }

    private fun downloadErrorConsumer(): Consumer<String> {
        return Consumer {
            showLoadingLayout(false)
            Snackbar.make(view!!, it, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showLoadingLayout(show: Boolean) {
        if (show) {
            downloadingOverlay!!.visibility = View.VISIBLE
            setUiEnabled(false)
        } else {
            downloadingOverlay!!.visibility = View.INVISIBLE
            setUiEnabled(true)
        }
    }

    private fun isLoadingOverlayVisible(): Boolean {
        return downloadingOverlay?.visibility == View.VISIBLE
    }

    private fun setUiEnabled(enabled: Boolean) {
        wv_cloud_video_pull.isClickable = enabled
        wv_cloud_video_pull.isEnabled = enabled
        btn_download.isEnabled = enabled
    }
}