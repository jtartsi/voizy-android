package com.voizy.android.ui

import android.app.Activity
import android.os.Bundle
import android.webkit.WebViewClient
import com.voizy.android.R
import kotlinx.android.synthetic.main.webview.*

class WebViewActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        webview.webViewClient = WebViewClient()
        webview.loadUrl("https://voizy-public.s3-eu-west-1.amazonaws.com/voizy_privacy_policy.html")
    }
}