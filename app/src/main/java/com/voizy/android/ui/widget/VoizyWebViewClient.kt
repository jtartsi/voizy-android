package com.voizy.android.ui.widget

import android.webkit.WebView
import android.webkit.WebViewClient

class VoizyWebViewClient : WebViewClient() {

    companion object {
        private val TAG = VoizyWebViewClient::class.java.simpleName
    }

    var pageChangedListener: ((String) -> Unit)? = null

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view!!.loadUrl(url)
        return true
    }

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        super.doUpdateVisitedHistory(view, url, isReload)
        pageChangedListener?.apply {
            url?.let { url ->
                invoke(url)
            }
        }
    }
}