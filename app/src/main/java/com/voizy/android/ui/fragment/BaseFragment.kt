package com.voizy.android.ui.fragment

import androidx.fragment.app.Fragment

abstract class BaseFragment : Fragment() {

    open fun useCustomBackPress(): Boolean {
        return false
    }

    abstract fun getFragmentTag(): String

    /**
     * @Return Boolean wether or not the back press was consumed
     */
    open fun onBackPressed(): Boolean {
        return false
    }
}