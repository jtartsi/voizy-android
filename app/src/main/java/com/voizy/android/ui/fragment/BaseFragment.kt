package com.voizy.android.ui.fragment

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    open fun useCustomBackPress(): Boolean {
        return false
    }

    abstract fun getFragmentTag(): String

    open fun onBackPressed() {
    }
}