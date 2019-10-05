package com.voizy.android.ui.fragment

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    open fun doubleBackPressNeeded(): Boolean {
        return false
    }

    abstract fun getFragmentTag(): String

    abstract fun onBackPressed()
}