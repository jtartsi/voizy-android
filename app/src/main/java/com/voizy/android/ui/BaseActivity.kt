package com.voizy.android.ui

import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.FragmentActivity

abstract class BaseActivity : FragmentActivity() {

    protected lateinit var appBarProgressBar: ProgressBar

    fun showProgressBar(visibility: Boolean) {

        appBarProgressBar.visibility = if (visibility) View.VISIBLE else View.GONE
    }
}