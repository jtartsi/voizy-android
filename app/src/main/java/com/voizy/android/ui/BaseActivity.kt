package com.voizy.android.ui

import androidx.fragment.app.FragmentActivity
import com.voizy.android.R
import com.voizy.android.ui.fragment.BaseFragment

abstract class BaseActivity : FragmentActivity() {

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container)

        if (currentFragment is BaseFragment && currentFragment.useCustomBackPress()) {
            currentFragment.onBackPressed()
        } else {
            super.onBackPressed()
        }
    }
}